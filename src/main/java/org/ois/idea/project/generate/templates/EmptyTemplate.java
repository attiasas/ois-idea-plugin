package org.ois.idea.project.generate.templates;

import org.ois.idea.project.generate.OisProjectGenerator;
import org.ois.idea.project.generate.OisProjectTemplate;

import java.io.IOException;
import java.util.Map;

public class EmptyTemplate implements OisProjectTemplate {

    private final static String TEMPLATE_NAME = "EmptyState.java";

    @Override
    public String getDescription() {
        return "Generate a project with a single empty IState implementation";
    }

    @Override
    public void applyTemplate(OisProjectGenerator.GenerationParams params) throws IOException {
        copyTemplateAndReplaceValues(
                "/generate/project/templates/" + TEMPLATE_NAME,
                params.srcProjectPacakgePath().resolve(TEMPLATE_NAME),
                Map.of("PROJECT_GROUP", params.packageName)
        );
        updateManifestStates(params.manifest, "Empty", Map.of("Empty", String.format("%s.EmptyState", params.packageName)));
    }
}
