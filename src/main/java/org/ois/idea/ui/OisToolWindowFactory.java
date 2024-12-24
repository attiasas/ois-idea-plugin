package org.ois.idea.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.jetbrains.annotations.NotNull;
import org.ois.idea.project.DependencyManager;
import org.ois.idea.project.OisProjectManager;

public class OisToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // Load OIS Project
        OisProjectManager projectManager = OisProjectManager.getInstance(project);
        projectManager.refresh();
        // Load OIS dependencies if needed
        DependencyManager.getInstance(project).refresh(false);
        // Init UI
        project.getService(OisToolWindow.class).initToolWindowContent(toolWindow, projectManager.getProject());
    }
}
