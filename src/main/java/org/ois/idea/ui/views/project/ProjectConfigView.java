package org.ois.idea.ui.views.project;

import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.SeparatorComponent;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.ois.idea.events.ProjectStateEvents;
import org.ois.idea.project.OisProjectManager;
import org.ois.idea.ui.views.project.components.manifest.SimulationManifestDisplay;

import javax.swing.*;
import java.awt.*;

public class ProjectConfigView extends TitledView {

    private JLabel pluginVersion;
    private JLabel coreVersion;

    private ProjectConfigView(@NotNull Project project) {
        super(project, "Simulation Configuration");

        // Main panel with vertical layout
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS)); // Vertical stacking
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding


//        JPanel content = new JPanel(new BorderLayout());


        JPanel versionsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints rdGbc = new GridBagConstraints();
        rdGbc.insets = JBUI.insets(5);
        rdGbc.fill = GridBagConstraints.HORIZONTAL;
        rdGbc.gridy = 0;

        rdGbc.gridx = 0;
        versionsPanel.add(new JLabel("Plugin:"), rdGbc);
        rdGbc.gridx = 1;
        pluginVersion = new JLabel("Loading...");
        versionsPanel.add(pluginVersion, rdGbc);
        rdGbc.gridx = 2;
        versionsPanel.add(new SeparatorComponent(), rdGbc);
        rdGbc.gridx = 3;
        versionsPanel.add(new JLabel("Core:"), rdGbc);
        rdGbc.gridx = 4;
        coreVersion = new JLabel("Loading...");
        versionsPanel.add(coreVersion, rdGbc);

        mainPanel.add(versionsPanel);//, BorderLayout.NORTH);

        SimulationManifestDisplay manifestDisplay = new SimulationManifestDisplay(OisProjectManager.getInstance(getIdeaProject()).getProject().getManifest(), false);
        manifestDisplay.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5)); // Top, Left, Bottom, Right
        mainPanel.add(manifestDisplay);

        // Add glue to consume remaining vertical space
//        mainPanel.add(Box.createVerticalGlue());

        // Wrap the main panel in a JBScrollPane to make it scrollable


        // Set the scrollable panel as content, add it to the center of the wrapperPanel
//        JPanel wrapperPanel = new JPanel(new BorderLayout());
//        wrapperPanel.add(mainPanel);  // Corrected: Use BorderLayout.CENTER

        // Set wrapperPanel as the content
        setContent(new JBScrollPane(mainPanel, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

        // Register listeners
        projectBusConnection.subscribe(ProjectStateEvents.ON_PROJECT_DEPENDENCY_REFRESH_STARTED, (ProjectStateEvents) ()-> {
            pluginVersion.setText("Loading...");
            coreVersion.setText("Loading...");
        });
        projectBusConnection.subscribe(ProjectStateEvents.ON_PROJECT_DEPENDENCY_REFRESH_ENDED, (ProjectStateEvents) ()-> {
            OisProjectManager manager = OisProjectManager.getInstance(project);
            pluginVersion.setText(manager.getPluginVersion().toString());
            coreVersion.setText(manager.getCoreVersion().toString());
        });
    }

    public static ProjectConfigView getInstance(Project project) {
        return project.getService(ProjectConfigView.class);
    }

}
