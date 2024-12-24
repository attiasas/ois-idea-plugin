package org.ois.idea.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.ois.idea.events.ProjectViewEvents;
import org.ois.idea.ui.OisToolWindow;

public class CreateProjectAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        project.getMessageBus().syncPublisher(ProjectViewEvents.ON_PROJECT_VIEW_SELECT).setView(OisToolWindow.View.Create);
    }
}
