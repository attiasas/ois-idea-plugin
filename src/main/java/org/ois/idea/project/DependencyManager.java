package org.ois.idea.project;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.ois.core.utils.Version;
import org.ois.idea.events.ProjectStateEvents;
import org.ois.idea.log.Logger;
import org.ois.idea.utils.ProjectUtils;
import org.ois.idea.utils.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DependencyManager {

    private final static String PLUGIN_GIT_REPO = "https://github.com/attiasas/ois-gradle-plugin.git";
    private final static String CORE_GIT_REPO = "https://github.com/attiasas/ois-core.git";

    private final static Pattern pluginVersionPattern = Pattern.compile("id\\s+['\"]" + Pattern.quote("org.ois.simulation") + "['\"]\\s+version\\s+['\"]([^'\"]+)['\"]");
    private final static Pattern coreVersionPattern =Pattern.compile("OIS_CORE_VERSION\\s*=\\s*new Version\\(\"([^\"]+)\"\\);");

    private final static String LOG_PREFIX = "Can't update OIS dependencies: ";

    private final Project ideaProject;
    private final AtomicBoolean refreshing = new AtomicBoolean(false);

    private DependencyManager(@NotNull Project ideaProject) {
        this.ideaProject = ideaProject;
    }

    public static DependencyManager getInstance(@NotNull Project ideaProject) {
        return ideaProject.getService(DependencyManager.class);
    }

    public void refresh(boolean forceInstall) {
        if (isRefreshInProgress()) {
            Logger.getInstance().info("OIS Dependencies are loading...");
            return;
        }

        OisProjectManager projectManager = OisProjectManager.getInstance(ideaProject);
        OisProject oisProject = projectManager.getProject();
        if (oisProject == null || !oisProject.exists()) {
            Logger.getInstance().warn(LOG_PREFIX + "Not OIS Project");
            return;
        }

        refreshing.set(true);
        ideaProject.getMessageBus().syncPublisher(ProjectStateEvents.ON_PROJECT_DEPENDENCY_REFRESH_STARTED).update();

        Utils.BackgroundTask refreshTask = new Utils.BackgroundTask(ideaProject, "Loading OIS Dependencies", false) {
            @Override
            public void run(Utils.@NotNull OISProgressIndicator indicator) {
                try {
                    // Resolve and download phase
                    indicator.state("Resolving OIS Plugin version", 0);
                    Version pluginVersion = findOisPluginVersion(ProjectUtils.getProjectBasePath(ideaProject));
                    if (pluginVersion == null) {
                        Logger.getInstance().warn(LOG_PREFIX + "Can't resolve OIS plugin version");
                        return;
                    }
                    projectManager.setPluginVersion(pluginVersion);
                    if (!isOisPluginExistsLocally(pluginVersion)) {
                        indicator.state( "Downloading OIS Plugin: v" + pluginVersion, 0.175);
                        downloadPluginDependency(pluginVersion);
                    }
                    indicator.state( "Resolving OIS Core version", 0.35);
                    Version coreVersion = findOisCoreVersion(pluginVersion);
                    if (coreVersion == null) {
                        Logger.getInstance().warn(LOG_PREFIX + "Can't resolve OIS core version");
                        return;
                    }
                    projectManager.setCoreVersion(coreVersion);
                    if (!isOisCoreExistsLocally(coreVersion)) {
                        indicator.state( "Downloading OIS Core: v" + coreVersion, 0.525);
                        downloadCoreDependency(coreVersion);
                    }
                    // Install phase (optional)
                    if (forceInstall || !pluginExistsInLocalRegistry()) {
                        indicator.state( "Installing OIS Plugin", 0.7);
                        installOisPlugin(pluginVersion);
                    }
                    if (forceInstall || !coreExistsInLocalRegistry()) {
                        indicator.state( "Installing OIS Plugin", 0.875);
                        installOisCore(coreVersion);
                    }
                    // Done
                    indicator.done();
                    Logger.getInstance().info("OIS Dependencies are ready");
                } catch (Exception e) {
                    Logger.getInstance().error(LOG_PREFIX + "Refresh failed", e);
                    indicator.cancel();
                } finally {
                    refreshing.set(false);
                    ideaProject.getMessageBus().syncPublisher(ProjectStateEvents.ON_PROJECT_DEPENDENCY_REFRESH_ENDED).update();
                }
            }
        };
        refreshTask.runInBackground();
    }

    private static Version findOisPluginVersion(Path projectPath) throws IOException {
        Path buildFilePath = projectPath.resolve("build.gradle");
        if (!Files.exists(buildFilePath)) {
            return null;
        }
        // Read the contents of the file
        String buildFileContent = Files.readString(buildFilePath);
        Matcher matcher = pluginVersionPattern.matcher(buildFileContent);
        if (!matcher.find()) {
            return null;
        }
        return new Version(matcher.group(1));
    }

    private static Version findOisCoreVersion(Version pluginVersion) throws IOException {
        // Get File in plugin repository pointing to the core version injected
        Path pluginSrcDirectory = getPluginDirectory(pluginVersion).resolve("ois-gradle-plugin").resolve("src").resolve("main").resolve("java");
        Path pluginConstFile = pluginSrcDirectory.resolve("org").resolve("ois").resolve("plugin").resolve("Const.java");
        // Extract version from file
        String content = new String(Files.readAllBytes(pluginConstFile));
        Matcher matcher = coreVersionPattern.matcher(content);
        if (matcher.find()) {
            return new Version(matcher.group(1));
        }
        return null;
    }

    private static boolean isOisPluginExistsLocally(Version pluginVersion) {
        Path localDirectory = getPluginDirectory(pluginVersion).resolve("ois-gradle-plugin");
        return localDirectory.toFile().exists() && localDirectory.toFile().isDirectory();
    }

    private static boolean isOisCoreExistsLocally(Version coreVersion) {
        Path localDirectory = getCoreDirectory(coreVersion).resolve("ois-core");
        return localDirectory.toFile().exists() && localDirectory.toFile().isDirectory();
    }

    private static boolean pluginExistsInLocalRegistry() {
        return existsInLocalRegistry("org.ois.open-interactive-simulation");
    }

    private static boolean coreExistsInLocalRegistry() {
        return existsInLocalRegistry("org.ois.open-interactive-simulation-core");
    }

    private static boolean existsInLocalRegistry(String locator) {
        Path repository = Utils.USER_HOME_PATH.resolve(".m2").resolve("repository");
        String[] packages = locator.split("\\.");
        Path location = repository;
        for (String p : packages) {
            location = location.resolve(p);
        }
        return location.toFile().exists() && location.toFile().isDirectory();
    }

    private static Path getPluginDirectory(Version pluginVersion) {
        return Utils.HOME_PATH.resolve("dependencies").resolve("plugin").resolve(pluginVersion.toString());
    }

    private static Path getCoreDirectory(Version coreVersion) {
        return Utils.HOME_PATH.resolve("dependencies").resolve("core").resolve(coreVersion.toString());
    }

    private static void downloadDependency(String repository, Version version, Path destination) throws IOException, InterruptedException {
        destination.toFile().mkdirs();
        try {
            ProjectUtils.cloneRepository(repository, version.toString(), destination);
            return;
        } catch (Exception e) {
            Logger.getInstance().warn("Can't download '" + repository + ":" + version + "', using default branch" ,e);
        }
        ProjectUtils.cloneRepository(repository, destination);
    }

    private static void downloadPluginDependency(Version pluginVersion) throws IOException, InterruptedException {
        Path destination = getPluginDirectory(pluginVersion);
        downloadDependency(PLUGIN_GIT_REPO, pluginVersion, destination);
    }

    private static void downloadCoreDependency(Version coreVersion) throws IOException, InterruptedException {
        Path destination = getCoreDirectory(coreVersion);
        downloadDependency(CORE_GIT_REPO, coreVersion, destination);
    }

    private static void installOisPlugin(Version pluginVersion) throws IOException, InterruptedException {
        Path localDirectory = getPluginDirectory(pluginVersion).resolve("ois-gradle-plugin");
        ProjectUtils.runGradleTasks(localDirectory, new Hashtable<>(), true, "publishToMavenLocal");
    }

    private static void installOisCore(Version coreVersion) throws IOException, InterruptedException {
        Path localDirectory = getCoreDirectory(coreVersion).resolve("ois-core");
        ProjectUtils.runGradleTasks(localDirectory, new Hashtable<>(), true, "publishToMavenLocal");
    }

    public boolean isRefreshInProgress() { return refreshing.get(); }
}
