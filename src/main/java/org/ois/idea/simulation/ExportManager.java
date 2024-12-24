package org.ois.idea.simulation;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.ois.core.utils.Version;
import org.ois.idea.events.ProjectStateEvents;
import org.ois.idea.log.Logger;
import org.ois.idea.project.OisProject;
import org.ois.idea.project.OisProjectManager;
import org.ois.idea.utils.ProjectUtils;
import org.ois.idea.utils.Utils;
import org.ois.idea.utils.command.CommandExecutor;
import org.ois.idea.utils.command.CommandResults;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExportManager {

    private final static String LOG_PREFIX = "Can't export OIS project: ";

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
                    indicator.state("Exporting Simulation");
                    ProjectUtils.runGradleTasks(projectDirectory, new HashMap<>(), true, "export");
                    // Done
                    indicator.done();
                    Logger.getInstance().info("OIS project exported");
                    // NotifyUser.showMsgDialog("Exported!");
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

    public boolean isExportInProgress() { return exporting.get(); }
}
