package org.ois.idea.project.generate;

import org.ois.core.project.SimulationManifest;
import org.ois.core.utils.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public interface OisProjectTemplate {
    String getDescription();
    void applyTemplate(OisProjectGenerator.GenerationParams params) throws IOException;

    default void updateManifestStates(SimulationManifest manifest, String initialState, Map<String, String> states) {
        manifest.setStates(states);
        manifest.setInitialState(initialState);
    }

    default void copyTemplateAndReplaceValues(String templateResourceLocation, Path destination) throws IOException {
        copyTemplateAndReplaceValues(templateResourceLocation, destination, new HashMap<>());
    }

    // Given a location to a resource in the plugin read its content, search for templateValues where the keys should be found in the content
    // of the resource in the following way: %KEY%, replace it with the value attach it and than save the edited content to the given destination
    default void copyTemplateAndReplaceValues(String templateResourceLocation, Path destination, Map<String, String> templateValues) throws IOException {
        // Create the destination file or directory if it does not exist
        FileUtils.createDirIfNotExists(destination.getParent(), true);
        String content = "";
        try (InputStream resourceStream = getClass().getResourceAsStream(templateResourceLocation)) {
            if (resourceStream == null) {
                throw new RuntimeException("Can't locate template at: " + templateResourceLocation);
            }
            content = readResource(resourceStream);
        }
        // Replace placeholders in the content
        for (Map.Entry<String, String> entry : templateValues.entrySet()) {
            String placeholder = "%" + entry.getKey() + "%";
            content = content.replace(placeholder, entry.getValue());
        }

        // Write the updated content to the destination
        try (BufferedWriter writer = Files.newBufferedWriter(destination, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write(content);
        }
    }

    default String readResource(InputStream resourceStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceStream))) {
            // Read the resource content
            StringBuilder contentBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append(System.lineSeparator());
            }
            // Replace placeholders in the content
            return contentBuilder.toString();
        }
    }
}
