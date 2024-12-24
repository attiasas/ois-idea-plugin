package org.ois.idea.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.ois.core.runner.RunnerConfiguration;
import org.ois.idea.log.NotifyUser;
import org.ois.idea.simulation.SimulationManager;

/**
 * Also add
 * <group id="JFrog.Floating">
 *             <action id="JFrog.FloatingStartLocalScan"
 *                     class="com.jfrog.ide.idea.actions.StartLocalScanAction"
 *                     text="Trigger Scan"
 *                     description="Trigger JFrog scan"
 *                     icon="/icons/jfrog_icon.svg"/>
 *         </group>
 *
 */
public class DebugSimulationAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        SimulationManager.getInstance(project).run(RunnerConfiguration.RunnerType.Desktop);

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
