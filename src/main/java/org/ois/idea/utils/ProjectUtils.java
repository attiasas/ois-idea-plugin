package org.ois.idea.utils;

import com.intellij.openapi.project.Project;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.ois.core.project.SimulationManifest;
import org.ois.core.utils.io.data.formats.JsonFormat;
import org.ois.idea.log.Logger;
import org.ois.idea.utils.command.CommandExecutor;
import org.ois.idea.utils.command.CommandResults;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class ProjectUtils {

    public static Path getProjectBasePath(Project project) {
        return project.getBasePath() != null ? Paths.get(project.getBasePath()) : Paths.get(".");
    }

    public static SimulationManifest loadProjectManifest(Project project) throws IOException {
        Logger.getInstance().debug("Checking if OIS Project...");
        Path manifestPath = getProjectBasePath(project).resolve("simulation").resolve(SimulationManifest.DEFAULT_FILE_NAME);
        if (!manifestPath.toFile().exists()) {
            Logger.getInstance().debug(String.format("Project manifest not found at '%s'. Not an OIS Project", manifestPath));
            return null;
        }
        return JsonFormat.compact().load(new SimulationManifest(), Files.readString(manifestPath));
    }

    public static void cloneRepository(String repositoryUrl, String branch, Path destinationFolder) throws IOException, InterruptedException {
        parseCommandResults(
                new CommandExecutor().exeCommand(List.of("git", "clone", "--branch", branch, repositoryUrl), destinationFolder.toAbsolutePath().toFile()),
                String.format("clone %s:%s", repositoryUrl, branch)
        );
    }

    public static void cloneRepository(String repositoryUrl, Path destinationFolder) throws IOException, InterruptedException {
        parseCommandResults(
                new CommandExecutor().exeCommand(List.of("git", "clone", repositoryUrl), destinationFolder.toAbsolutePath().toFile()),
                String.format("clone %s", repositoryUrl)
        );
    }

    private static void parseCommandResults(CommandResults results, String msg) {
        if (results.isOk()) {
            Logger.getInstance().debug(String.format("Finished successfully to %s\n%s", msg, results.getStdOut()));
            return;
        }
        RuntimeException exception = new RuntimeException(results.getStdErr());
        Logger.getInstance().error(String.format("Failed to %s", msg), exception);
        throw exception;
    }

    public static void runGradleTasks(Path workingDir, Map<String, String> environmentVariables, boolean oneByOne, String... gradleTasks) throws IOException, InterruptedException {
        Path gradleExe = workingDir.resolve(SystemUtils.IS_OS_WINDOWS ? "gradlew.bat" : "gradlew").toAbsolutePath();
        String exe = gradleExe.toString();
        if (!gradleExe.toFile().exists()) {
            exe = "";
        }
        CommandExecutor executor = new CommandExecutor(StringUtils.defaultIfBlank(exe, "gradle"), environmentVariables);
        if (!oneByOne) {
            parseCommandResults(
                    executor.exeCommand(List.of(gradleTasks), workingDir.toFile()),
                    String.format("run gradle tasks: %s", String.join(",", gradleTasks))
            );
            return;
        }
        for (String task : gradleTasks) {
            parseCommandResults(
                    executor.exeCommand(List.of(task), workingDir.toFile()),
                    String.format("run gradle task: %s", task)
            );
        }
    }

}
