package org.ois.idea.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.ois.idea.log.NotifyUser;
import org.ois.idea.simulation.SimulationManager;

public class DebugHtmlSimulationAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        NotifyUser.showMsgDialog("Debugging Html Simulation coming soon!");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        boolean isInProgress = SimulationManager.getInstance(project).isSimulationRunning();
        e.getPresentation().setVisible(!isInProgress);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
