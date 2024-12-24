package org.ois.idea.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import org.jetbrains.annotations.NotNull;
import org.ois.idea.events.ProjectViewEvents;
import org.ois.idea.log.Logger;
import org.ois.idea.ui.OisToolWindow;

import javax.swing.*;
import java.awt.*;

public class ViewSelectorAction extends AnAction implements CustomComponentAction {

    public static class StateDataKeys {
        public static final DataKey<String> VIEW_KEY = DataKey.create("org.ois.idea.VIEW_KEY");
    }

    private ComboBox<String> comboBox;
    private Project ideaProject;

    public ViewSelectorAction(){
        super("Select View");
    }

    public ViewSelectorAction(@NotNull Project project) {
        this();
        this.ideaProject = project;
        String[] options = OisToolWindow.getSelectableViews();
        comboBox = new ComboBox<>(options);
        comboBox.setSelectedItem(options[0]);

        // Update selected view when a new item is selected
        comboBox.addActionListener(e -> {
            Object selectedItem = comboBox.getSelectedItem();
            if (selectedItem == null) {
                return;
            }
            ideaProject.getMessageBus().syncPublisher(ProjectViewEvents.ON_PROJECT_VIEW_SELECT).setView(OisToolWindow.View.toView(selectedItem.toString()));
        });
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (ideaProject == null || comboBox == null) {
            Logger.getInstance().warn("Action can't change view on null project");
            return;
        }
        // Get the state passed via the event's data context
        String view = e.getData(StateDataKeys.VIEW_KEY);
        if (view == null) {
            return;
        }
        comboBox.setSelectedItem(view);
    }

    @Override
    public @NotNull JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
        // Create a panel to hold the ComboBox
        JPanel panel = new JPanel(new BorderLayout());

        // Create ComboBox with the available states


        // Add the ComboBox to the panel
        panel.add(comboBox, BorderLayout.CENTER);

        return panel;
    }

    public static void selectView(ViewSelectorAction action, OisToolWindow.View type) {
        // Create DataContext with the new view
        DataContext dataContext = dataId -> {
            if (StateDataKeys.VIEW_KEY.getName().equals(dataId)) {
                return type.name();
            }
            return null;
        };
        // Create a AnActionEvent to invoke
        AnActionEvent event = AnActionEvent.createFromDataContext(
                ActionPlaces.UNKNOWN,
                new Presentation(),
                dataContext
        );
        // Invoke the action, executed safely on the EDT without unnecessary delays.
        if (SwingUtilities.isEventDispatchThread()) {
            action.actionPerformed(event);
        } else {
            SwingUtilities.invokeLater(() -> action.actionPerformed(event));
        }
    }
}
