package org.ois.idea.ui.views.project;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;
import org.ois.idea.log.Logger;
import org.ois.idea.project.generate.OisProjectGenerator;
import org.ois.idea.ui.views.project.components.ProjectConfigDisplay;
import org.ois.idea.ui.views.project.components.TemplateSelectorDisplay;
import org.ois.idea.ui.views.project.components.manifest.SimulationManifestDisplay;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class CreateProjectView extends TitledView {

    private final ProjectConfigDisplay projectConfigPanel;
    private final SimulationManifestDisplay manifestDisplay;
    private final TemplateSelectorDisplay templateSelector;

    public static CreateProjectView getInstance(Project project) {
        return project.getService(CreateProjectView.class);
    }

    private CreateProjectView(@NotNull Project project) {
        super(project, "Generate OIS Project");

        // Main panel with vertical layout
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS)); // Vertical stacking
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding

        // Add components to main panel
        projectConfigPanel = new ProjectConfigDisplay();
        projectConfigPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.add(projectConfigPanel);

        templateSelector = new TemplateSelectorDisplay();
        templateSelector.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.add(templateSelector);

        manifestDisplay = new SimulationManifestDisplay(null, true);
        manifestDisplay.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.add(manifestDisplay);

        // Add glue to consume remaining vertical space
        mainPanel.add(Box.createVerticalGlue());

        // Wrap the main panel in a JScrollPane to make it scrollable
        JBScrollPane scrollPane = new JBScrollPane(mainPanel, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Set the scrollable panel as content
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.add(scrollPane, BorderLayout.CENTER);
        setContent(wrapperPanel);

        // Button panel at the bottom
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        JButton createButton = new JButton("Create");
        createButton.addActionListener(e -> generateProject());

        buttonPanel.add(createButton);

        // Add button panel to the bottom of the view
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void generateProject() {
        OisProjectGenerator.GenerationParams params = getGenerationParams();
        // Validate
        File directory = params.directory.toFile();
        if (directory.exists() && directory.isDirectory()) {
            File[] filesInDirectory = directory.listFiles();
            if (filesInDirectory != null && filesInDirectory.length > 0) {
                Logger.getInstance().error("Can't generate OIS project in a non empty directory");
                return;
            }
        }
        if (params.template == null) {
            Logger.getInstance().error("Can't generate OIS project, require a template");
            return;
        }
        // Generate
        new OisProjectGenerator(getIdeaProject(), params).generate();
    }

    private OisProjectGenerator.GenerationParams getGenerationParams() {
        return new OisProjectGenerator.GenerationParams(
                projectConfigPanel.getProjectName(),
                projectConfigPanel.getProjectGroup(),
                projectConfigPanel.getProjectDirectory().resolve(projectConfigPanel.getProjectName()),
                templateSelector.getSelectedTemplate(),
                manifestDisplay.getManifest()
        );
    }
}
