package org.ois.idea.project;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.ois.core.utils.Version;
import org.ois.idea.actions.ViewSelectorAction;
import org.ois.idea.events.ProjectStateEvents;
import org.ois.idea.log.Logger;
import org.ois.idea.ui.OisToolWindow;
import org.ois.idea.utils.Utils;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class OisProjectManager {

    private final static String LOG_PREFIX = "Can't load OIS project: ";
    
    private final Project ideaProject;
    private final AtomicBoolean refreshing = new AtomicBoolean(false);

    private OisProject project;

    private OisProjectManager(@NotNull Project ideaProject) {
        this.ideaProject = ideaProject;
    }

    public static OisProjectManager getInstance(@NotNull Project ideaProject) {
        return ideaProject.getService(OisProjectManager.class);
    }

    public OisProject getProject() { return this.project; }

    public void setPluginVersion(Version version) {
        if (project == null) {
            return;
        }
        project.setPluginVersion(version);
    }

    public void setCoreVersion(Version version) {
        if (project == null) {
            return;
        }
        project.setCoreVersion(version);
    }

    public Version getPluginVersion() {
        return project.getPluginVersion();
    }

    public Version getCoreVersion() {
        return project.getCoreVersion();
    }

    public void refresh() {
        if (isRefreshInProgress()) {
            Logger.getInstance().info("OIS Project is loading...");
            return;
        }
        Logger.getInstance().info("Loading OIS Project");
        refreshing.set(true);
        ideaProject.getMessageBus().syncPublisher(ProjectStateEvents.ON_PROJECT_REFRESH_STARTED).update();
        
        try {
            // Load Manifest on the current thread
            project = new OisProject(this.ideaProject);
            if (!project.exists()) {
                Logger.getInstance().info("Not an OIS Project");
                return;
            }
        } catch (IOException e) {
            Logger.getInstance().error(LOG_PREFIX + "Loading project manifest failed", e);
        } finally {
            refreshing.set(false);
            ideaProject.getMessageBus().syncPublisher(ProjectStateEvents.ON_PROJECT_REFRESH_ENDED).update();
        }
        
        OisToolWindow oisToolWindow = OisToolWindow.getInstance(ideaProject);
        oisToolWindow.setRefreshingProject();
        Thread projectRefreshThread = new Thread(() -> {
            try {
                Logger.getInstance().info("Loading OIS Project content");
                
                // Finally, set UI to show project information
                oisToolWindow.setView(OisToolWindow.View.Project);
                Logger.getInstance().info("OIS Project loaded");
            } catch (Exception e) {
                Logger.getInstance().error(LOG_PREFIX + "Refresh failed", e);
            } finally {
                refreshing.set(false);
                ideaProject.getMessageBus().syncPublisher(ProjectStateEvents.ON_PROJECT_REFRESH_ENDED).update();
            }
        });
        projectRefreshThread.start();
    }

    public boolean isRefreshInProgress() { return refreshing.get(); }
}
