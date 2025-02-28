package org.ois.idea.simulation;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.ois.idea.events.ProjectStateEvents;
import org.ois.idea.log.Logger;
import org.ois.idea.project.OisProject;
import org.ois.idea.project.OisProjectManager;
import org.ois.idea.utils.ProjectUtils;
import org.ois.idea.utils.Utils;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExportManager {

    private final static String LOG_PREFIX = "Can't export OIS project: ";

    private final static Pattern debugModePattern = Pattern.compile("(?m)^\\s*(?!//)\\s*debugMode\\s*=\\s*(\\w+)");


    private final Project ideaProject;

    private final AtomicBoolean exporting = new AtomicBoolean(false);

    private ExportManager(@NotNull Project ideaProject) {
        this.ideaProject = ideaProject;
    }

    public static ExportManager getInstance(@NotNull Project ideaProject) {
        return ideaProject.getService(ExportManager.class);
    }

    public void exportSimulation() {
        if (isExportInProgress()) {
            Logger.getInstance().info("project export already in progress...");
            return;
        }

        OisProjectManager projectManager = OisProjectManager.getInstance(ideaProject);
        OisProject oisProject = projectManager.getProject();
        if (oisProject == null || !oisProject.exists()) {
            Logger.getInstance().warn(LOG_PREFIX + "Not OIS Project");
            return;
        }

        Path projectDirectory = ProjectUtils.getProjectBasePath(ideaProject);

        exporting.set(true);
        ideaProject.getMessageBus().syncPublisher(ProjectStateEvents.ON_PROJECT_EXPORT_STARTED).update();

        Utils.BackgroundTask exportTask = new Utils.BackgroundTask(ideaProject, "Exporting OIS Project", false) {
            @Override
            public void run(Utils.@NotNull OISProgressIndicator indicator) {
                try {
                    boolean debugMode = findOisProjectDebugMode(ProjectUtils.getProjectBasePath(ideaProject));
                    if (debugMode) {
                        indicator.state("Exporting Simulation (Debug mode)");
                    } else {
                        indicator.state("Exporting Simulation");
                    }
                    ProjectUtils.runGradleTasks(projectDirectory, new HashMap<>(), false, "export", "-D", String.format("org.ois.runner.debugMode=%b", debugMode));
                    // Done
                    indicator.done();
                    Logger.getInstance().info("OIS project exported");
                    askToOpenDistributionDirectory(projectDirectory.resolve("build").resolve("ois").resolve("distribution"));
                } catch (Exception e) {
                    Logger.getInstance().error(LOG_PREFIX + "export failed", e);
                    indicator.cancel();
                } finally {
                    exporting.set(false);
                    ideaProject.getMessageBus().syncPublisher(ProjectStateEvents.ON_PROJECT_EXPORT_ENDED).update();
                }
            }
        };
        exportTask.runInBackground();
    }

    private void askToOpenDistributionDirectory(Path location) {
        Logger.showActionableBalloon(ideaProject, "OIS Project exported successfully.\nClick <a href=\"here\">here</a> to open the distribution directory.", () -> {
            try {
                Desktop desktop = Desktop.getDesktop();
                desktop.open(location.toFile());
            } catch (IOException e) {
                Logger.getInstance().error("Can't open project OIS distribution directory at " + location, e);
            }
        });
    }

    private static boolean findOisProjectDebugMode(Path projectPath) throws IOException {
        Path buildFilePath = projectPath.resolve("build.gradle");
        if (!Files.exists(buildFilePath)) {
            return false;
        }
        // Read the contents of the file
        String buildFileContent = Files.readString(buildFilePath);
        Matcher matcher = debugModePattern.matcher(buildFileContent);
        if (!matcher.find()) {
            return false;
        }
        return Boolean.parseBoolean(matcher.group(1));
    }

    public boolean isExportInProgress() { return exporting.get(); }
}
