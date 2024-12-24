package org.ois.idea.ui.common;

import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

public class TitledAttributesContainer extends TitledContentContainer {
    private static final int LABEL_COLUMN = 0;
    private static final int FIELD_COLUMN = 1;

    public TitledAttributesContainer(String title) {
        super(title);
    }

    public TitledAttributesContainer(String title, boolean isOptional) {
        super(title, isOptional);
    }

    protected GridBagConstraints createConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5); // Add padding between components
        gbc.fill = GridBagConstraints.HORIZONTAL; // Default to horizontal stretching
        gbc.weightx = 1.0; // Allow fields to stretch horizontally
        gbc.weighty = 0.0; // Default rows do not consume vertical space
        gbc.anchor = GridBagConstraints.WEST; // Align components to the left by default
        return gbc;
    }

    /**
     * Adds a labeled attribute to the container.
     *
     * @param label       the label text
     * @param placeHolder the placeholder text for the input field
     * @param row         the row index in the layout
     * @param constraints the {@link GridBagConstraints} instance for layout
     * @param content     the parent panel to which components are added
     * @return the created {@link JTextField}
     */
    protected JTextField addAttribute(String label, String placeHolder, int row, GridBagConstraints constraints, JPanel content) {
        // Configure label constraints
        constraints.gridy = row;
        constraints.gridx = LABEL_COLUMN;
        constraints.weightx = 0.0; // Labels don't stretch
        constraints.fill = GridBagConstraints.NONE;
        JLabel fieldLabel = new JLabel(label + ":");
        fieldLabel.setHorizontalAlignment(SwingConstants.LEFT);
        content.add(fieldLabel, constraints);

        // Configure field constraints
        constraints.gridx = FIELD_COLUMN;
        constraints.weightx = 1.0; // Fields stretch horizontally
        constraints.fill = GridBagConstraints.HORIZONTAL;
        JTextField field = new JTextField(placeHolder);
        content.add(field, constraints);

        return field;
    }

    protected <T extends JComponent> void addAttribute(String label, T component, int row, GridBagConstraints constraints, JPanel content) {
        // Configure label constraints
        constraints.gridy = row;
        constraints.gridx = LABEL_COLUMN;
        constraints.weightx = 0.0; // Labels don't stretch
        constraints.fill = GridBagConstraints.NONE;
        JLabel fieldLabel = new JLabel(label + ":");
        fieldLabel.setHorizontalAlignment(SwingConstants.LEFT);
        content.add(fieldLabel, constraints);

        // Configure field constraints
        constraints.gridx = FIELD_COLUMN;
        constraints.weightx = 1.0; // Fields stretch horizontally
        constraints.fill = GridBagConstraints.HORIZONTAL;
        content.add(component, constraints);

    }

    protected <T extends JComponent> T addAttribute(T component, int row, GridBagConstraints constraints, JPanel content) {
        // Configure constraints to span all columns
        constraints.gridy = row; // Set the row index
        constraints.gridx = 0; // Start from the first column
        constraints.gridwidth = GridBagConstraints.REMAINDER; // Span all columns
        constraints.weightx = 1.0; // Allow component to stretch horizontally
        constraints.fill = GridBagConstraints.HORIZONTAL; // Fill horizontally

        // Add the component to the content panel
        content.add(component, constraints);

        // Reset gridwidth to default for subsequent components
        constraints.gridwidth = 1;

        return component;
    }
}
