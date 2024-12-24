package org.ois.idea.project.generate;

import com.intellij.openapi.project.Project;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.UnzipParameters;
import org.jetbrains.annotations.NotNull;
import org.ois.core.project.SimulationManifest;
import org.ois.core.utils.io.FileUtils;
import org.ois.core.utils.io.data.formats.JsonFormat;
import org.ois.idea.log.Logger;
import org.ois.idea.utils.Utils;
import org.ois.idea.utils.command.CommandExecutor;
import org.ois.idea.utils.command.CommandResults;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class OisProjectGenerator {

    public static class GenerationParams {
        public final String name;
        public final String packageName;
        public final Path directory;
        public final OisProjectTemplate template;

        public final SimulationManifest manifest;

        public GenerationParams(String name, String packageName, Path directory, OisProjectTemplate template, SimulationManifest manifest) {
            this.name = name;
            this.packageName = packageName;
            this.directory = directory;
            this.template = template;
            this.manifest = manifest;
        }

        public Path srcMainJavaPath() {
            return this.directory.resolve("src").resolve("main").resolve("java");
        }

        public Path srcProjectPacakgePath() {
            // Split the package name into components and create corresponding directories
            Path packageDir = srcMainJavaPath();
            for (String component : packageName.split("\\.")) {
                packageDir = packageDir.resolve(component);
            }
            return packageDir;
        }
    }

    private final Project ideaProject;
    private final GenerationParams params;

    public OisProjectGenerator(@NotNull Project ideaProject, GenerationParams params) {
        this.ideaProject = ideaProject;
        this.params = params;
    }

    public void generate() {
        if (params.directory.toFile().mkdirs()) {
            Logger.getInstance().debug(String.format("Created %s directory", params.directory));
        }
        Utils.BackgroundTask generateTask = new Utils.BackgroundTask("Generating OIS Project", false) {
            @Override
            public void run(Utils.@NotNull OISProgressIndicator indicator) {
                try {
                    // Gradle project init
                    indicator.state("Generating base Gradle project", 0);
                    initGradleProjectInDirectory();
                    // Apply template
                    indicator.state("Apply template", 0.333);
                    params.template.applyTemplate(params);
                    // General OIS Project init
                    indicator.state("Generating OIS manifest files", 0.666);
                    initSimulationFiles();
                    // Done
                    indicator.done();
                    askToOpenIdeaProject(params.directory);
                } catch (Exception e) {
                    Logger.getInstance().error("Failed to generate OIS project", e);
                    indicator.cancel();
                }
            }
        };
        generateTask.runInBackground();
    }

    private void initGradleProjectInDirectory() throws IOException, InterruptedException {
        extractBaseProjectToDirectory(params.directory);
        generateBaseProjectFiles(params.directory, params.name, params.packageName);
//        ProjectUtils.runGradleTasks(params.directory, new Hashtable<>(), true,
//                "init", "--type", "basic", "--overwrite", "--dsl", "groovy", "--project-name", params.name
//        );
    }

    private void extractBaseProjectToDirectory(Path directory) throws IOException {
        // Create a temporary file
        Path tempFile = Files.createTempFile("base-project-archive", ".zip");
        tempFile.toFile().deleteOnExit(); // Ensure cleanup on JVM exit
        // Copy gradle archive to temp file
        try (InputStream resourceStream = OisProjectGenerator.class.getResourceAsStream("/generate/project/base/base-project.zip")) {
            if (resourceStream == null) {
                throw new IllegalArgumentException("Resource '/generate/project/base/base-project.zip' not found");
            }
            // Write the resource content to the temporary file
            try (OutputStream out = Files.newOutputStream(tempFile)) {
                resourceStream.transferTo(out);
            }
        }
        // Extract temp file archive
        UnzipParameters params = new UnzipParameters();
        params.setExtractSymbolicLinks(false);
        try (ZipFile zip = new ZipFile(tempFile.toFile())) {
            zip.extractAll(directory.toFile().toString(), params);
        } catch (ZipException exception) {
            throw new IOException("An error occurred while trying to unarchived gradle files:\n" + exception.getMessage());
        }
        // Set executable permissions to the downloaded scanner
        if (!directory.resolve("gradlew.bat").toFile().setExecutable(true)) {
            throw new IOException("An error occurred while trying to give access permissions to gradlew.bat");
        }
        if (!directory.resolve("gradlew").toFile().setExecutable(true)) {
            throw new IOException("An error occurred while trying to give access permissions to gradlew");
        }
    }

    private void generateBaseProjectFiles(Path directory, String projectName, String projectGroup) throws IOException {
        // Generate package directories (base on parameters)
        generateDirectoryStructure(directory, projectGroup);
        // Generate build.gradle with resource project/build.gradle.template
        generateBuildGradle(directory, projectGroup);
        // Generate README.md
        generateReadme(directory, projectName);
    }

    /**
     * Generates a directory structure and creates package directories under 'src/main/java'.
     *
     * @param baseDir     The base directory where the structure will be created.
     * @param packageName The package name to create directories for (e.g., "org.ois.example").
     * @throws IOException If an I/O error occurs.
     */
    public static void generateDirectoryStructure(Path baseDir, String packageName) throws IOException {
        // Define the main directory structure
        Files.createDirectories(baseDir.resolve("src").resolve("main").resolve("resources"));
        Path srcMainJava = baseDir.resolve("src").resolve("main").resolve("java");
        Files.createDirectories(srcMainJava);
        // Split the package name into components and create corresponding directories
        Path packageDir = srcMainJava;
        for (String component : packageName.split("\\.")) {
            packageDir = packageDir.resolve(component);
            FileUtils.createDirIfNotExists(packageDir, true);
        }
    }

    /**
     * Generates a build.gradle file from a template, replacing placeholders with dynamic values.
     *
     * @param outputDir    The directory where the build.gradle file will be written.
     * @param projectGroup The project group that will be set in the build.gradle file
     * @throws IOException If an I/O error occurs.
     */
    private void generateBuildGradle(Path outputDir, String projectGroup) throws IOException {
        Utils.generateFromTemplate(
                "/generate/project/base/build.gradle.template",
                outputDir.resolve("build.gradle"),
                Map.of(
                        "PLUGIN_VERSION", Utils.OIS_GRADLE_PLUGIN_GENERATE_PROJECT_VERSION.toString(),
                        "PROJECT_GROUP", projectGroup
                )
        );
    }

    /**
     * Generates a README.md file in the specified directory.
     *
     * @param outputDir   The directory where the README.md file will be created.
     * @param projectName The name of the project to include in the README.
     * @throws IOException If an I/O error occurs.
     */
    private void generateReadme(Path outputDir, String projectName) throws IOException {
        Utils.generateFromTemplate(
                "/generate/project/base/README.md.template",
                outputDir.resolve("README.md"),
                Map.of("PLUGIN_NAME", projectName)
        );
    }

    private void initSimulationFiles() throws IOException {
        Path simulationDirectory = params.directory.resolve("simulation");
        if (simulationDirectory.toFile().mkdirs()) {
            Logger.getInstance().debug("Created simulation directory");
        }
        // Write manifest
        try (FileWriter writer = new FileWriter(simulationDirectory.resolve(SimulationManifest.DEFAULT_FILE_NAME).toFile())) {
            writer.write(JsonFormat.humanReadable().serialize(params.manifest));
        }
    }

    private void askToOpenIdeaProject(Path location) {
        Logger.showActionableBalloon(ideaProject, "OIS Project created successfully.\nClick <a href=\"here\">here</a> to open.", () -> {
            try {
                CommandResults cmdResults = new CommandExecutor("idea").exeCommand(List.of(location.toAbsolutePath().toString()), location.toFile());
                if (!cmdResults.isOk()) {
                    Logger.getInstance().error("Failed to open generated project (exit code " + cmdResults.getExitCode() + "):\n" + cmdResults.getStdErr());
                }
            } catch (IOException | InterruptedException e) {
                Logger.getInstance().error("Can't open generated OIS project at " + location, e);
            }
        });
    }
}
