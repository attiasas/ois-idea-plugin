package org.ois.idea.utils;

import com.intellij.execution.*;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.application.ApplicationConfigurationType;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.ois.core.project.SimulationManifest;
import org.ois.core.utils.io.data.formats.JsonFormat;
import org.ois.core.utils.log.ILogger;
import org.ois.idea.log.Logger;
import org.ois.idea.utils.command.CommandExecutor;
import org.ois.idea.utils.command.CommandResults;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class ProjectUtils {

    public static Path getProjectBasePath(Project project) {
        return project.getBasePath() != null ? Paths.get(project.getBasePath()) : Paths.get(".");
    }

    public static SimulationManifest loadProjectManifest(Project project) throws IOException {
        Logger.getInstance().debug("Checking if OIS Project...");
        Path manifestPath = getProjectBasePath(project).resolve("simulation").resolve(SimulationManifest.DEFAULT_FILE_NAME);
        if (!manifestPath.toFile().exists()) {
            Logger.getInstance().debug(String.format("Project manifest not found at '%s'. Not an OIS Project", manifestPath));
            return null;
        }
        return JsonFormat.compact().load(new SimulationManifest(), Files.readString(manifestPath));
    }

    public static void cloneRepository(String repositoryUrl, String branch, Path destinationFolder) throws IOException, InterruptedException {
        parseCommandResults(
                new CommandExecutor().exeCommand(List.of("git", "clone", "--branch", branch, repositoryUrl), destinationFolder.toAbsolutePath().toFile()),
                String.format("clone %s:%s", repositoryUrl, branch)
        );
    }

    public static void cloneRepository(String repositoryUrl, Path destinationFolder) throws IOException, InterruptedException {
        parseCommandResults(
                new CommandExecutor().exeCommand(List.of("git", "clone", repositoryUrl), destinationFolder.toAbsolutePath().toFile()),
                String.format("clone %s", repositoryUrl)
        );
    }

    private static void parseCommandResults(CommandResults results, String msg) {
        if (results.isOk()) {
            Logger.getInstance().debug(String.format("Finished successfully to %s\n%s", msg, results.getStdOut()));
            return;
        }
        RuntimeException exception = new RuntimeException(results.getStdErr());
        Logger.getInstance().debug(String.format("Failed to %s", msg), exception);
        throw exception;
    }

    public static void runGradleTasks(Path workingDir, Map<String, String> environmentVariables, boolean oneByOne, String... gradleTasks) throws IOException, InterruptedException {
        Path gradleExe = workingDir.resolve(SystemUtils.IS_OS_WINDOWS ? "gradlew.bat" : "gradlew").toAbsolutePath();
        String exe = gradleExe.toString();
        if (!gradleExe.toFile().exists()) {
            exe = "";
        }
        CommandExecutor executor = new CommandExecutor(StringUtils.defaultIfBlank(exe, "gradle"), environmentVariables);
        if (!oneByOne) {
            parseCommandResults(
                    executor.exeCommand(List.of(gradleTasks), workingDir.toFile()),
                    String.format("run gradle tasks: %s", String.join(",", gradleTasks))
            );
        }
        for (String task : gradleTasks) {
            parseCommandResults(
                    executor.exeCommand(List.of(task), workingDir.toFile()),
                    String.format("run gradle task: %s", task)
            );
        }
    }

    public static void runDebugConfiguration(Project project) {
        // Access the RunManager instance for the project
        RunManager runManager = RunManager.getInstance(project);

        // Create a new runner and configuration settings
        RunnerAndConfigurationSettings settings = runManager.createConfiguration("Dynamic Debug Config", ApplicationConfigurationType.getInstance().getClass());

        // Get the ApplicationConfiguration instance and configure it
        ApplicationConfiguration configuration = (ApplicationConfiguration) settings.getConfiguration();
        configuration.setMainClassName("com.example.Main"); // Replace with your main class
        configuration.setProgramParameters("--example-parameter"); // Optional program arguments
        configuration.setWorkingDirectory(project.getBasePath()); // Set working directory (usually project base)

        // Add the configuration to RunManager
        runManager.addConfiguration(settings);

        // Find the Debug Executor
        Executor debugExecutor = DefaultDebugExecutor.getDebugExecutorInstance();

        // Find the ProgramRunner for the given executor
        ProgramRunner<?> runner = ProgramRunner.getRunner(debugExecutor.getId(), configuration);

        if (runner != null) {
//            try {
//                // Prepare and execute the configuration
//                ExecutionEnvironmentBuilder builder = ExecutionEnvironmentBuilder.create(project, debugExecutor, settings);
//                ExecutionManager.getInstance(project).startRunProfile(builder.build(), null);
//            } catch (ExecutionException e) {
//                e.printStackTrace(); // Log or handle the error appropriately
//            }
        }
    }

}
