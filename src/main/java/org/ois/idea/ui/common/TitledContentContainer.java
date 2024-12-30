package org.ois.idea.ui.common;

import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class TitledContentContainer extends JPanel {

    private final boolean isOptionalContent;

    private final JCheckBox optionalDetailsTitle;
    private final JLabel mandatoryDetailsTitle;

    private final JSeparator divider;

    private final Border plainBorder;
    private final Border emptyBorder;

    protected JComponent component;

    public TitledContentContainer(String title) { this(title, false); }

    public TitledContentContainer(String title, boolean isOptional) {
        setLayout(new BorderLayout());
        this.isOptionalContent = isOptional;
        // Create the title
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        if (isOptional) {
            optionalDetailsTitle = createOptionalTitle(title);
            mandatoryDetailsTitle = null;
            headerPanel.add(optionalDetailsTitle);
        } else {
            optionalDetailsTitle = null;
            mandatoryDetailsTitle = createTitle(title);
            headerPanel.add(mandatoryDetailsTitle);
        }
        // Add components to the panel
        add(headerPanel, BorderLayout.NORTH);
        // Create the divider
        divider = new JSeparator(SwingConstants.HORIZONTAL);
        divider.setVisible(!isOptional);
        add(divider, BorderLayout.CENTER);
        // Content panel
        component = new JPanel();
        component.setVisible(!isOptional);
        add(component);
        // Create the borders
        plainBorder = BorderFactory.createLineBorder(JBColor.GRAY);
        emptyBorder = JBUI.Borders.empty(5);
        // Set the border
        if (isOptional) {
            setBorder(emptyBorder);
        } else {
            setBorder(plainBorder);
        }
    }

    public void setContent(JComponent component) {
        removeAll();
        // Add the components
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        if (this.isOptionalContent) {
            headerPanel.add(optionalDetailsTitle);
        } else {
            headerPanel.add(mandatoryDetailsTitle);
        }
        add(headerPanel, BorderLayout.NORTH);
        add(divider, BorderLayout.CENTER);
        // Add the content
        this.component = component;
        this.component.setVisible(isSelected());
        add(component);
        // Repaint
        revalidate();
        repaint();
    }

    public boolean isSelected() {
        return !this.isOptionalContent || optionalDetailsTitle.isSelected();
    }

    public String getTitle() { return this.isOptionalContent ? optionalDetailsTitle.getText() : mandatoryDetailsTitle.getText(); }

    public void setSelected(boolean selected) {
        if (!this.isOptionalContent) {
            return;
        }
        optionalDetailsTitle.setSelected(selected);
        toggleContentVisibility();
    }

    private JCheckBox createOptionalTitle(String title) {
        JCheckBox checkBox = new JCheckBox(title);
        checkBox.setFocusPainted(false); // Remove focus highlight
        checkBox.setOpaque(false);       // Transparent background
        checkBox.setFont(checkBox.getFont().deriveFont(Font.BOLD));
        checkBox.addActionListener(e -> toggleContentVisibility());
        return checkBox;
    }

    private JLabel createTitle(String title) {
        JLabel label = new JLabel(title);
        label.setOpaque(false);       // Transparent background
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        return label;
    }

    private void toggleContentVisibility() {
        if (!this.isOptionalContent) {
            return;
        }
        boolean isSelected = optionalDetailsTitle.isSelected();
        component.setVisible(isSelected);
        divider.setVisible(isSelected); // Show/hide the divider
        setBorder(isSelected ? plainBorder : emptyBorder); // Update the border dynamically
        revalidate();
        repaint();
    }
}
