package org.ois.idea.ui.views.project.components.manifest;

import com.intellij.util.ui.JBUI;
import org.ois.core.project.SimulationManifest;
import org.ois.idea.ui.common.TitledAttributesContainer;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class  SimulationStatesDetails extends TitledAttributesContainer {
    private final JTextField initialStateField;

    public SimulationStatesDetails(SimulationManifest manifest) {
        super("States");

        String initialState = manifest == null ? "myInitial" : manifest.getInitialState();
        Map<String, String> states = manifest == null ? new LinkedHashMap<>() : manifest.getStates();

        // Grid with attributes
        JPanel content = new JPanel(new GridBagLayout());
        GridBagConstraints rbc = createConstraints();

        initialStateField = addAttribute("Initial State", initialState, 0, rbc, content);

        // Display the mapping as a table
        JPanel statesPanel = new JPanel();
        statesPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Add header
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        JLabel stateHeader = new JLabel("State");
        stateHeader.setFont(stateHeader.getFont().deriveFont(Font.BOLD));
        statesPanel.add(stateHeader, gbc);

        gbc.gridx = 1;
        JLabel classHeader = new JLabel("Class");
        classHeader.setFont(classHeader.getFont().deriveFont(Font.BOLD));
        statesPanel.add(classHeader, gbc);

        // Add the state-class mappings
        int row = 1;
        for (Map.Entry<String, String> entry : states.entrySet()) {
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0.5;
            statesPanel.add(new JLabel(entry.getKey()), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            statesPanel.add(new JLabel(entry.getValue()), gbc);

            row++;
        }

        statesPanel.setBorder(BorderFactory.createTitledBorder("States Mapping"));

        addAttribute(statesPanel, 1, rbc, content);

        setContent(content);
    }

    public String getInitialState() {
        return initialStateField.getText();
    }
}
