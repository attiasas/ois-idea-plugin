package org.ois.idea.ui.views.project.components.manifest;

import org.ois.core.project.SimulationManifest;
import org.ois.idea.ui.common.TitledAttributesContainer;

import javax.swing.*;
import java.awt.*;

public class SimulationManifestDisplay extends TitledAttributesContainer {

    private final JTextField titleField;
    private final SimulationRunnerDetails runnerDetails;
    private final SimulationStatesDetails statesDetails;

    public SimulationManifestDisplay(SimulationManifest manifest, boolean createView) {
        super(createView ? "Simulation Manifest" : "Manifest", createView);
        // Grid with attributes
        JPanel content = new JPanel(new GridBagLayout());
        GridBagConstraints rbc = createConstraints();

        titleField = addAttribute("Title", manifest == null ? "Simulation Title" : manifest.getTitle(),0, rbc, content);
        runnerDetails = addAttribute(new SimulationRunnerDetails(manifest, createView), 1, rbc, content);
        if (!createView) {
            statesDetails = addAttribute(new SimulationStatesDetails(manifest), 2, rbc, content);
        } else {
            statesDetails = null;
        }

        setContent(content);
    }

    public SimulationManifest getManifest() {
        SimulationManifest manifest = new SimulationManifest();
        // Title
        manifest.setTitle(titleField.getText());
        // Runner Details
        manifest.setScreenWidth(runnerDetails.getScreenWidthValue());
        manifest.setScreenHeight(runnerDetails.getScreenHeightValue());
        manifest.setPlatforms(runnerDetails.getSelectedPlatforms());
        return manifest;
    }
}
