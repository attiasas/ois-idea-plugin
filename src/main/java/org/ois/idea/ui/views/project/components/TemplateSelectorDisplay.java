package org.ois.idea.ui.views.project.components;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import org.ois.idea.project.generate.OisProjectTemplate;
import org.ois.idea.project.generate.templates.BasicTemplate;
import org.ois.idea.project.generate.templates.EmptyTemplate;
import org.ois.idea.ui.common.TitledContentContainer;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TemplateSelectorDisplay extends TitledContentContainer {
    private final Map<String, OisProjectTemplate> templates;
    private final JBList<String> itemList;

    public TemplateSelectorDisplay() {
        super("Choose a Template");

        templates = new LinkedHashMap<>();
        templates.put("Empty",new EmptyTemplate());
        templates.put("Basic",new BasicTemplate());

        // Create a JList with the items
        itemList = new JBList<>(List.copyOf(templates.keySet()));
        itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemList.setVisibleRowCount(5); // Number of visible rows before scroll
        itemList.setSelectedIndex(0);

        // Enable tooltips for items
        itemList.setToolTipText(""); // Activate tooltips
        itemList.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int index = itemList.locationToIndex(e.getPoint());
                if (index > -1) {
                    String item = itemList.getModel().getElementAt(index);
                    itemList.setToolTipText(templates.get(item).getDescription());
                }
            }
        });

        // Wrap the JList in a JScrollPane for scrolling
        JBScrollPane scrollPane = new JBScrollPane(itemList);

        // Set the scrollable pane as the main content
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane);

        panel.setBorder(BorderFactory.createLineBorder(JBColor.GRAY));

        setContent(panel);
    }

    public OisProjectTemplate getSelectedTemplate() {
        return templates.get(itemList.getSelectedValue());
    }
}
