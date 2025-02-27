package org.ois.idea.simulation;

import com.intellij.execution.*;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.gradle.service.execution.GradleExternalTaskConfigurationType;
import org.jetbrains.plugins.gradle.service.execution.GradleRunConfiguration;
import org.ois.core.runner.RunnerConfiguration;
import org.ois.idea.events.ProjectStateEvents;
import org.ois.idea.log.Logger;
import org.ois.idea.utils.ProjectUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimulationManager {

    private final static String LOG_PREFIX = "Simulation failed: ";

    private final Project ideaProject;

    private final AtomicBoolean running = new AtomicBoolean(false);

    private ExecutorService executor;
    private static ProcessHandler currentProcessHandler;

    private SimulationManager(@NotNull Project ideaProject) {
        this.ideaProject = ideaProject;
    }

    public static SimulationManager getInstance(@NotNull Project ideaProject) {
        return ideaProject.getService(SimulationManager.class);
    }

    public void run(RunnerConfiguration.RunnerType platform) {
        runWithStrategy((latch) -> {
            Logger.getInstance().info(String.format("Running '%s' Simulation...", platform));
            addGradleTaskAndRun(ideaProject, "runDesktop --info -D org.ois.runner.debugMode=true", ProjectUtils.getProjectBasePath(ideaProject).toString(), false, latch);
        });
    }

    public void runDevMode() {
        runWithStrategy(latch -> {
            Logger.getInstance().info("Running DevMode Simulation...");
            Path devModeDir = Files.createTempDirectory(String.format("ois_%s_dev", ideaProject.getName()));
            ProjectUtils.runGradleTasks(ProjectUtils.getProjectBasePath(ideaProject), new Hashtable<>(), false,"runDesktop", "--info", "-D", String.format("org.ois.runner.devModeDir='%s'", devModeDir));
            latch.countDown();
        });
    }

    private interface SimulationRunStrategy {
        void run(CountDownLatch latch) throws ExecutionException, IOException, InterruptedException;
    }

    private void runWithStrategy(SimulationRunStrategy strategy) {
        if (DumbService.isDumb(ideaProject)) {
            Logger.getInstance().warn("Intellij is still indexing the project, can't run simulation...");
            return;
        }
        if (isSimulationRunning()) {
            Logger.getInstance().warn("Simulation already running, please exit the current before rerunning...");
            return;
        }

        running.set(true);
        ideaProject.getMessageBus().syncPublisher(ProjectStateEvents.ON_PROJECT_DEBUG_STARTED).update();

        Thread runSimulationThread = new Thread(() -> {
            executor = Executors.newFixedThreadPool(3);
            try {
                ProgressManager.checkCanceled();

                // Wait for the task to complete
                CountDownLatch latch = new CountDownLatch(1);
                strategy.run(latch);
                latch.await(); // Wait for the strategy to finish

                executor.shutdown();
                if (!executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES)) {
                    Logger.getInstance().error(LOG_PREFIX + "Simulation timeout elapsed.");
                }
            } catch (IOException | InterruptedException | ExecutionException e) {
                Logger.getInstance().error(LOG_PREFIX, e);
            } finally {
                Logger.getInstance().info("Done");
                executor.shutdownNow();
                running.set(false);
                ideaProject.getMessageBus().syncPublisher(ProjectStateEvents.ON_PROJECT_DEBUG_ENDED).update();
            }
        });
        runSimulationThread.start();
    }

    public void stop() {
        if (currentProcessHandler != null && !currentProcessHandler.isProcessTerminated()) {
            Logger.getInstance().info("Stopping the simulation process...");
            currentProcessHandler.destroyProcess();
        } else {
            Logger.getInstance().warn("No running process to stop.");
        }
    }

    public boolean isSimulationRunning() { return running.get(); }

    public static void addGradleTaskAndRun(Project project, String gradleTaskName, String workingDirectory, boolean debug, CountDownLatch latch) throws ExecutionException {
        // Access the RunManager for the current project
        RunManager runManager = RunManager.getInstance(project);

        // Get the Gradle configuration type
        GradleExternalTaskConfigurationType gradleType = GradleExternalTaskConfigurationType.getInstance();

        // Create a new RunnerAndConfigurationSettings instance
        RunnerAndConfigurationSettings settings = runManager.createConfiguration(gradleTaskName, gradleType.getFactory());

        // Set up the Gradle run configuration
        var gradleRunConfig = (GradleRunConfiguration) settings.getConfiguration();
        gradleRunConfig.getSettings().setTaskNames(List.of(gradleTaskName));
        gradleRunConfig.getSettings().setExternalProjectPath(workingDirectory);

        // Add the configuration to the RunManager
        runManager.addConfiguration(settings);

        // Optionally, set the newly created configuration as the selected one
        runManager.setSelectedConfiguration(settings);

        // Choose the executor (run or debug)
        Executor executor = debug ? DefaultDebugExecutor.getDebugExecutorInstance() : DefaultRunExecutor.getRunExecutorInstance();

        // Build the execution environment
        ExecutionEnvironment environment = ExecutionEnvironmentBuilder.create(executor, settings).build();

        // Attach a listener to monitor the execution lifecycle
        environment.getProject().getMessageBus().connect().subscribe(ExecutionManager.EXECUTION_TOPIC, new ExecutionListener() {
            @Override
            public void processStarted(@NotNull String executorId, @NotNull ExecutionEnvironment env, @NotNull ProcessHandler handler) {
                if (env.equals(environment)) {
                    Logger.getInstance().info("Gradle task started: " + gradleTaskName);
                    currentProcessHandler = handler;

                    // Attach a process terminated listener
                    ProcessTerminatedListener.attach(handler);
                }
            }

            @Override
            public void processTerminated(@NotNull String executorId, @NotNull ExecutionEnvironment env, @NotNull ProcessHandler handler, int exitCode) {
                if (env.equals(environment)) {
                    Logger.getInstance().info("Gradle task finished with exit code: " + exitCode);
                    currentProcessHandler = null; // Clear the handler reference
                    latch.countDown(); // Notify that the process is finished
                }
            }
        });

        // Execute the run/debug configuration
        ExecutionManager.getInstance(project).restartRunProfile(environment);
    }

}
