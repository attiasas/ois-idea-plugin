package org.ois.idea.ui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import org.ois.idea.actions.ViewSelectorAction;
import org.ois.idea.events.ProjectViewEvents;
import org.ois.idea.log.Logger;
import org.ois.idea.project.OisProject;
import org.ois.idea.ui.views.project.CreateProjectView;
import org.ois.idea.ui.views.project.ProjectConfigView;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class OisToolWindow extends SimpleToolWindowPanel implements Disposable {

    public enum View {
        Create, Project, Entities, Inspector;

        public static View toView(String val) {
            switch (val.toLowerCase()) {
                case "create" -> {
                    return Create;
                }
                case "project" -> {
                    return Project;
                }
                case "entities" -> {
                    return Entities;
                }
                case "inspector" -> {
                    return Inspector;
                }
            }
            return null;
        }
    }

    public static String[] getSelectableViews() {
        return new String[]{View.Project.name(), View.Entities.name(), View.Inspector.name()};
    }

    private final MessageBusConnection projectBusConnection;
    private final MessageBusConnection appBusConnection;
    private final Project ideaProject;

    private final Map<View, JPanel> views;
    private View currentView = View.Project;
    private final ViewSelectorAction viewSelectorAction;

    public OisToolWindow(@NotNull Project project) {
        super(false);
        this.projectBusConnection = project.getMessageBus().connect(this);
        this.appBusConnection = ApplicationManager.getApplication().getMessageBus().connect(this);
        this.ideaProject = project;
        this.viewSelectorAction = new ViewSelectorAction(ideaProject);

        views = Map.of(
                View.Create, CreateProjectView.getInstance(project),
                View.Project, ProjectConfigView.getInstance(project),
                View.Entities, getDefaultView(View.Entities),
                View.Inspector, getDefaultView(View.Inspector)
        );

        registerListeners();
    }

    private static JPanel getDefaultView(View type) {
        JPanel contentPanel = new JPanel(new BorderLayout());

        // Create a centered JLabel
        JLabel projectLabel = new JLabel(type.name() + " View, Coming Soon!", SwingConstants.CENTER);
//        projectLabel.setHorizontalAlignment(SwingConstants.CENTER); // Center horizontally
//        projectLabel.setVerticalAlignment(SwingConstants.CENTER);   // Center vertically

        // Add the label to the CENTER of the panel
        contentPanel.add(projectLabel, BorderLayout.CENTER);

        return contentPanel;
    }

    void registerListeners() {
        projectBusConnection.subscribe(ProjectViewEvents.ON_PROJECT_VIEW_SELECT, (ProjectViewEvents) this::setInternalView);
    }

    void initToolWindowContent(@NotNull ToolWindow toolWindow, OisProject project) {
        toolWindow.getComponent().add(this);
        ActionManager actionManager = ActionManager.getInstance();

        if (project == null || !project.exists()) {
            setInternalView(View.Create);
            return;
        }
        toolWindow.setAdditionalGearActions(getGearActions(actionManager));
        toolWindow.setTitleActions(getToolBarActions(actionManager));
    }

    public static OisToolWindow getInstance(@NotNull Project project) {
        return project.getService(OisToolWindow.class);
    }

    private List<AnAction> getToolBarActions(ActionManager actionManager) {
        return List.of(
                this.viewSelectorAction,
                new Separator(),
                actionManager.getAction("OIS.DebugSimulation"),
                actionManager.getAction("OIS.StopDebugSimulation"),
                actionManager.getAction("OIS.ExportSimulation"),
                new Separator(),
                actionManager.getAction("OIS.CreateProject"),
                actionManager.getAction("OIS.DownloadInstallDependencies")
        );
    }

    private ActionGroup getGearActions(ActionManager actionManager) {
        DefaultActionGroup toolbarGroup = new DefaultActionGroup();

//        DefaultActionGroup subGroup = new DefaultActionGroup();
//        subGroup.add(actionManager.getAction("OIS.DownloadInstallDependencies"));
//        subGroup.add(actionManager.getAction("OIS.CreateProject"));

        toolbarGroup.addAll(
                actionManager.getAction("OIS.DebugSimulation"),
                actionManager.getAction("OIS.DebugHtmlSimulation"),
                actionManager.getAction("OIS.DebugAndroidSimulation")//,
//                subGroup
        );

        return toolbarGroup;
    }

    public void setRefreshingProject() {
        SwingUtilities.invokeLater(() -> {
            JPanel contentPanel = new JPanel(new BorderLayout());

            JLabel projectLabel = new JLabel("Loading OIS Project...");
            contentPanel.add(projectLabel, BorderLayout.CENTER);

            setContent(contentPanel);
        });
    }

    public void setView(View view) {
        ViewSelectorAction.selectView(this.viewSelectorAction, view);
    }

    private void setInternalView(View view) {
        if (!views.containsKey(view)) {
            throw new RuntimeException("Unknown view '" + view + "'");
        }
        currentView = view;
        SwingUtilities.invokeLater(() -> {
            setContent(views.get(currentView));
        });
    }

    public View getCurrentView() { return currentView; }

    @Override
    public void dispose() {
        // Disconnect and release resources from the project bus connection
        projectBusConnection.disconnect();
        // Disconnect and release resources from the application bus connection
        appBusConnection.disconnect();
    }
}
