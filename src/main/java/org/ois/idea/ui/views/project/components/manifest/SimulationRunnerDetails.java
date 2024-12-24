package org.ois.idea.ui.views.project.components.manifest;

import org.ois.core.project.SimulationManifest;
import org.ois.core.runner.RunnerConfiguration;
import org.ois.idea.ui.common.TitledAttributesContainer;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class SimulationRunnerDetails extends TitledAttributesContainer {

    private static class ScreenDimsDetails extends TitledAttributesContainer {
        public final JTextField heightField;
        public final JTextField widthField;

        public ScreenDimsDetails() {
            super("Screen Dimensions");
            // Grid with attributes
            JPanel content = new JPanel(new GridBagLayout());
            GridBagConstraints rdGbc = createConstraints();

            widthField = addAttribute("Width", "0", 0, rdGbc, content);
            heightField = addAttribute("Height", "0", 1, rdGbc, content);

            setContent(content);
        }
    }

    // Screen dims + platforms
    private final ScreenDimsDetails screenDims;
    private final JCheckBox desktopCheckBox;
    private final JCheckBox htmlCheckBox;
    private final JCheckBox androidCheckBox;

    public SimulationRunnerDetails(SimulationManifest manifest, boolean isOptional) {
        super("Runner Details", isOptional);

        // Grid with attributes
        JPanel content = new JPanel(new GridBagLayout());
        GridBagConstraints rdGbc = createConstraints();

        screenDims = addAttribute(new ScreenDimsDetails(), 0, rdGbc, content);

        desktopCheckBox = new JCheckBox("Desktop", manifest == null || manifest.getPlatforms().contains(RunnerConfiguration.RunnerType.Desktop));
        htmlCheckBox = new JCheckBox("HTML", manifest == null || manifest.getPlatforms().contains(RunnerConfiguration.RunnerType.Html));
        androidCheckBox = new JCheckBox("Android", manifest == null || manifest.getPlatforms().contains(RunnerConfiguration.RunnerType.Android));

        JPanel platformsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        platformsPanel.add(desktopCheckBox);
        platformsPanel.add(htmlCheckBox);
        platformsPanel.add(androidCheckBox);

        addAttribute("Platforms", platformsPanel, 1, rdGbc, content);

        setContent(content);
    }

    public int getScreenWidthValue() {
        return Integer.parseInt(screenDims.widthField.getText());
    }

    public int getScreenHeightValue() {
        return Integer.parseInt(screenDims.heightField.getText());
    }

    public Set<RunnerConfiguration.RunnerType> getSelectedPlatforms() {
        Set<RunnerConfiguration.RunnerType> selected = new HashSet<>();

        if (desktopCheckBox.isSelected()) {
            selected.add(RunnerConfiguration.RunnerType.Desktop);
        }
        if (htmlCheckBox.isSelected()) {
            selected.add(RunnerConfiguration.RunnerType.Html);
        }
        if (androidCheckBox.isSelected()) {
            selected.add(RunnerConfiguration.RunnerType.Android);
        }

        return selected;
    }
}
