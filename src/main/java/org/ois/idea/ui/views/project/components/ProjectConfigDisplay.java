package org.ois.idea.ui.views.project.components;

import org.ois.idea.ui.common.TitledAttributesContainer;
import org.ois.idea.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProjectConfigDisplay extends TitledAttributesContainer {

    private final JTextField projectDirectoryField;
    private final JTextField projectNameField;
    private final JTextField projectGroupField;

    public ProjectConfigDisplay() {
        super("Project");
        // Grid with attributes
        JPanel content = new JPanel(new GridBagLayout());
        GridBagConstraints rdGbc = createConstraints();
        // Content
        projectNameField = addAttribute("Name", "example-project", 0, rdGbc, content);
        projectGroupField = addAttribute("Group", "org.ois.example", 1, rdGbc, content);
        projectDirectoryField = addDirectoryAttribute("Directory", Utils.HOME_PATH.resolve("projects").toString(), 2, rdGbc, content);
        setContent(content);
    }

    /**
     * Adds an attribute with a text field and a button to select a directory.
     *
     * @param label       the label for the attribute
     * @param placeholder the placeholder text for the text field
     * @param y           the row position in the grid
     * @param constraints the GridBagConstraints for layout
     * @param content     the parent JPanel to which components are added
     * @return the created JTextField
     */
    private JTextField addDirectoryAttribute(String label, String placeholder, int y, GridBagConstraints constraints, JPanel content) {
        // Add attribute (label and text field)
        JTextField field = addAttribute(label, placeholder, y, constraints, content);

        // Configure constraints for the directory selection button
        constraints.gridx = 2; // Column for the button
        constraints.weightx = 0; // Button doesn't stretch horizontally
        constraints.weighty = 0; // Button doesn't stretch vertically
        constraints.fill = GridBagConstraints.NONE; // Button doesn't fill available space
        constraints.anchor = GridBagConstraints.LINE_START; // Align button to the start of the row

        JButton selectButton = new JButton();
        // Set an icon (uses a default system folder icon)
        Icon folderIcon = UIManager.getIcon("FileView.directoryIcon");
        if (folderIcon != null) {
            selectButton.setIcon(folderIcon);
        } else {
            // Fallback text if icon is not available
            selectButton.setText("...");
        }

        // Add action listener for folder selection
        selectButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = chooser.showOpenDialog(content);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File selectedDirectory = chooser.getSelectedFile();
                field.setText(selectedDirectory.getAbsolutePath());
            }
        });

        // Add the button to the panel
        content.add(selectButton, constraints);

        return field;
    }

    public Path getProjectDirectory() {
        return Paths.get(projectDirectoryField.getText());
    }

    public String getProjectName() {
        return projectNameField.getText();
    }

    public String getProjectGroup() {
        return projectGroupField.getText();
    }
}
