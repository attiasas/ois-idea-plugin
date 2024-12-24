package org.ois.idea.project.generate.templates;

import org.ois.idea.project.generate.OisProjectGenerator;
import org.ois.idea.project.generate.OisProjectTemplate;

import java.io.IOException;
import java.util.Map;

public class BasicTemplate implements OisProjectTemplate {

    private final static String TEMPLATE_NAME = "BasicState.java";

    @Override
    public String getDescription() {
        return "Generate a project with a single basic state extending SimpleState class";
    }

    @Override
    public void applyTemplate(OisProjectGenerator.GenerationParams params) throws IOException {
        copyTemplateAndReplaceValues(
                "/generate/project/templates/" + TEMPLATE_NAME,
                params.srcProjectPacakgePath().resolve(TEMPLATE_NAME),
                Map.of("PROJECT_GROUP", params.packageName)
        );
        updateManifestStates(params.manifest, "Basic", Map.of("Basic", String.format("%s.BasicState", params.packageName)));
    }
}
