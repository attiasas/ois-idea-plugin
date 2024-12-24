package org.ois.idea.project;

import com.intellij.openapi.project.Project;
import org.ois.core.project.SimulationManifest;
import org.ois.core.utils.Version;
import org.ois.idea.utils.ProjectUtils;

import java.io.IOException;

public class OisProject {
    public final Project ideaProject;

    private final SimulationManifest manifest;

    private Version pluginVersion;
    private Version coreVersion;

    public OisProject(Project project) throws IOException {
        this.ideaProject = project;
        this.manifest = ProjectUtils.loadProjectManifest(project);
    }

    public boolean exists() { return this.manifest != null; }

    public Version getPluginVersion() {
        return pluginVersion;
    }

    public void setPluginVersion(Version pluginVersion) {
        this.pluginVersion = pluginVersion;
    }

    public Version getCoreVersion() {
        return coreVersion;
    }

    public void setCoreVersion(Version coreVersion) {
        this.coreVersion = coreVersion;
    }

    public SimulationManifest getManifest() {
        return manifest;
    }
}
