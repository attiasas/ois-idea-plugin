package org.ois.idea.ui.views.project;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.ois.idea.ui.views.OisToolView;

import javax.swing.*;
import java.awt.*;

public class TitledView extends OisToolView {

    public TitledView(@NotNull Project project, String title) {
        super(project);
        // Title Panel
        add(getTitle(title), BorderLayout.NORTH);
    }

    protected JPanel getTitle(String title) {
        JPanel titlePanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        // Add padding to the top of the label
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // Top, Left, Bottom, Right
        // Add it to a panel
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        return titlePanel;
    }
}
