package org.ois.idea.utils.command;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.ois.idea.log.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CommandExecutor implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final int TIMEOUT_EXIT_VALUE = 124;
    private static final int READER_SHUTDOWN_TIMEOUT_SECONDS = 30;
    private static final int PROCESS_TERMINATION_TIMEOUT_SECONDS = 30;
    private static final int EXECUTION_TIMEOUT_MINUTES = 120;

    private final Map<String, String> env;
    private final String executablePath;

    /**
     * Constructor to initialize using a build in executable in the PATH
     * @throws IllegalArgumentException if the executable is not found in default paths.
     */
    public CommandExecutor() {
        this("");
    }

    /**
     * @param executablePath - Executable path.
     */
    public CommandExecutor(String executablePath) {
        this(executablePath, null);
    }

    /**
     * @param executablePath - Executable path.
     * @param env            - Environment variables to use during execution.
     */
    public CommandExecutor(String executablePath, Map<String, String> env) {
        this.executablePath = resolveExecutablePath(executablePath.trim());
        Map<String, String> finalEnvMap = new HashMap<>(System.getenv());
        if (env != null) {
            Map<String, String> fixedEnvMap = new HashMap<>(env);
            fixPathEnv(fixedEnvMap);
            finalEnvMap.putAll(fixedEnvMap);
        }
        this.env = new HashMap<>(finalEnvMap);
    }

    /**
     * Resolves the full path of an executable by searching in common system paths.
     *
     * @param commandName - The name of the command/executable.
     * @return The resolved full path to the executable.
     * @throws IllegalArgumentException if the executable is not found in the default paths.
     */
    public static String resolveExecutablePath(String commandName) {
        if (commandName.isBlank()) {
            // Local binary provided as first arg, no need to resolve
            return "";
        }
        File commandFile = new File(commandName);
        if (commandFile.exists() && commandFile.canExecute()) {
            // Specify a binary path, No need to resolve
            return commandName;
        }
        List<String> commonPaths = SystemUtils.IS_OS_WINDOWS
                ? Arrays.asList(System.getenv("Path").split(";"))
                : Arrays.asList("/usr/bin", "/bin", "/usr/local/bin", "/sbin");

        for (String path : commonPaths) {
            File file = new File(path, commandName);
            if (file.exists() && file.canExecute()) {
                return file.getAbsolutePath();
            }
            if (SystemUtils.IS_OS_WINDOWS) {
                file = new File(path, commandName + ".bat");
                if (file.exists() && file.canExecute()) {
                    return file.getAbsolutePath();
                }
            }
            if (path.toLowerCase().contains(commandName)) {
                return path;
            }
        }

        throw new IllegalArgumentException("Executable '" + commandName + "' not found in default paths.");
    }

    /**
     * 1. Use correct file separator.
     * 2. In unix, append ":/usr/local/bin" to PATH environment variable.
     *
     * @param env - Environment variables map.
     */
    private void fixPathEnv(Map<String, String> env) {
        String path = env.get("PATH");
        if (path == null) {
            return;
        }
        if (SystemUtils.IS_OS_WINDOWS) {
            path = getFixedWindowsPath(path);
        } else {
            path = path.replaceAll(";", File.pathSeparator) + ":/usr/local/bin";
        }
        env.replace("PATH", path);
    }

    /**
     * Fix the PATH value to be valid for execution on a Windows machine.
     * Take care of a case when either non-Windows or Windows environment-variables are received.
     * Examples:
     * "C:\my\first;Drive:\my\second" returns "C:\my\first;Drive:\my\second"
     * "/Users/my/first:/Users/my/second" returns "/Users/my/first;/Users/my/second"
     *
     * @param path - Value of PATH environment variable.
     * @return Fixed PATH value.
     */
    static String getFixedWindowsPath(String path) {
        String[] pathParts = path.split(";");
        String[] newPathParts = new String[pathParts.length];
        for (int index = 0; index < pathParts.length; index++) {
            String part = pathParts[index];
            int backSlashIndex = part.indexOf('\\');
            if (backSlashIndex < 0) {
                newPathParts[index] = part.replaceAll(":", ";");
                continue;
            }
            String startPart = part.substring(0, backSlashIndex);
            String endPart = part.substring(backSlashIndex);
            String newPart = startPart + endPart.replaceAll(":", ";");
            newPathParts[index] = newPart;
        }
        return String.join(";", newPathParts);
    }

    /**
     * Execute a command in external process.
     *
     * @param execDir     - The execution dir (Usually path to project). Null means current directory.
     * @param args        - Command arguments.
     * @return CommandResults object
     */
    public CommandResults exeCommand(List<String> args, File execDir) throws IOException, InterruptedException {
        return exeCommand(execDir, args, new ArrayList<>());
    }

    /**
     * Execute a command in external process.
     *
     * @param execDir     - The execution dir (Usually path to project). Null means current directory.
     * @param args        - Command arguments.
     * @param credentials - If specified, the credentials will be concatenated to the other commands. The credentials will be masked in the log output.
     * @return CommandResults object
     */
    public CommandResults exeCommand(File execDir, List<String> args, List<String> credentials) throws InterruptedException, IOException {
        return exeCommand(execDir, args, credentials, EXECUTION_TIMEOUT_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * Execute a command in external process.
     *
     * @param execDir     - The execution dir (Usually path to project). Null means current directory.
     * @param args        - Command arguments.
     * @param credentials - If specified, the credentials will be concatenated to the other commands. The credentials will be masked in the log output.
     * @param timeout     - The maximum time to wait for the command execution.
     * @param unit        - The time unit of the timeout argument.
     * @return CommandResults object
     */
    public CommandResults exeCommand(File execDir, List<String> args, List<String> credentials, long timeout, TimeUnit unit) throws InterruptedException, IOException {
        List<String> command = new ArrayList<>(args);
        ExecutorService service = Executors.newFixedThreadPool(2);
        try {
            Process process = runProcess(execDir, executablePath, command, credentials, env);
            // The output stream is not necessary in non-interactive scenarios, therefore we can close it now.
            process.getOutputStream().close();
            try (InputStream inputStream = process.getInputStream(); InputStream errorStream = process.getErrorStream()) {
                StreamReader inputStreamReader = new StreamReader(inputStream);
                StreamReader errorStreamReader = new StreamReader(errorStream);
                service.submit(inputStreamReader);
                service.submit(errorStreamReader);
                boolean executionTerminatedProperly = process.waitFor(timeout, unit);
                service.shutdown();
                boolean outputReaderTerminatedProperly = service.awaitTermination(READER_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                boolean terminatedProperly = executionTerminatedProperly && outputReaderTerminatedProperly;
                return getCommandResults(terminatedProperly, command, inputStreamReader.getOutput(), errorStreamReader.getOutput(), process.exitValue());
            } finally {
                // Ensure termination of the subprocess we have created.
                if (process.isAlive()) {
                    // First try to terminate the process gracefully
                    process.destroy();
                    process.waitFor(PROCESS_TERMINATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    if (process.isAlive()) {
                        // Finally, force kill.
                        process.destroyForcibly();
                    }
                }
            }
        } finally {
            service.shutdownNow();
        }
    }

    private CommandResults getCommandResults(boolean terminatedProperly, List<String> args, String output, String error, int exitValue) {
        CommandResults commandRes = new CommandResults();
        if (!terminatedProperly) {
            error += System.lineSeparator() + String.format("Process '%s' had been terminated forcibly after timeout.", String.join(" ", args));
            exitValue = TIMEOUT_EXIT_VALUE;
        }
        commandRes.setStdOut(output);
        commandRes.setStdErr(error);
        commandRes.setExitCode(exitValue);
        return commandRes;
    }

    private static Process runProcess(File execDir, String executablePath, List<String> args, List<String> credentials, Map<String, String> env) throws IOException {
        // Make sure to copy the environment variables map to avoid changing the original map or in case it is immutable.
        Map<String, String> newEnv = new HashMap<>(env);

        args = formatCommand(args, credentials, executablePath, newEnv);
        logCommand(args);
        ProcessBuilder processBuilder = new ProcessBuilder(args)
                .directory(execDir);
        processBuilder.environment().putAll(newEnv);
        return processBuilder.start();
    }

    /**
     * Formats a command for execution, incorporating credentials and environment variables.
     *
     * @param args           the list of arguments to be included in the command
     * @param credentials    if specified, the credentials will be concatenated to the command
     * @param executablePath the path to the executable to be executed
     * @param env            environment variables map. It might be modified as part of the formatting process
     * @return the formatted command as a list of strings, ready for execution
     */
    private static List<String> formatCommand(List<String> args, List<String> credentials, String executablePath, Map<String, String> env) {
        if (credentials != null) {
            args.addAll(credentials);
        }
        if (executablePath.isEmpty()) {
            return args;
        }
        if (SystemUtils.IS_OS_WINDOWS) {
            formatWindowsCommand(args, executablePath, env);
            return args;
        }
        return formatUnixCommand(args, executablePath);
    }

    /**
     * Formats a Windows command for execution.
     *
     * @param args           the list of arguments to be included in the command
     * @param executablePath the path to the executable to be executed
     * @param env            environment variables map. It might be modified as part of the formatting process
     */
    private static void formatWindowsCommand(List<String> args, String executablePath, Map<String, String> env) {
        Path execPath = Paths.get(executablePath);
        if (execPath.isAbsolute()) {
            addToWindowsPath(env, execPath);
            args.add(0, execPath.getFileName().toString());
        } else {
            args.add(0, executablePath.replaceAll(" ", "^ "));
        }
        args.addAll(0, Arrays.asList("cmd", "/c"));
    }

    private static List<String> formatUnixCommand(List<String> args, String executablePath) {
        args.add(0, executablePath.replaceAll(" ", "\\\\ "));
        String strArgs = String.join(" ", args);
        return new ArrayList<String>() {{
            add("/bin/sh");
            add("-c");
            add(strArgs);
        }};
    }

    /**
     * Inserts the executable directory path at the beginning of the Path environment variable.
     * This is done to handle cases where the executable path contains spaces. In such scenarios, the "cmd" command used
     * to execute this command in Windows may incorrectly parse the path, treating the section after the space as an
     * argument for the command.
     *
     * @param env      environment variables map
     * @param execPath the executable path
     */
    static void addToWindowsPath(Map<String, String> env, Path execPath) {
        String execDirPath = execPath.getParent().toString();

        // Insert the executable directory path to the beginning of the Path environment variable.
        String windowsPathEnvKey = "Path";
        if (env.containsKey(windowsPathEnvKey)) {
            env.put(windowsPathEnvKey, execDirPath + File.pathSeparator + env.get(windowsPathEnvKey));
        } else {
            env.put(windowsPathEnvKey, execDirPath);
        }
    }

    private static void logCommand(List<String> args) {
        String output = String.join(" ", args);
        Logger.getInstance().info("Executing command: " + output);
    }

    private static class StreamReader implements Runnable {

        private final InputStream inputStream;
        private String output;

        StreamReader(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            try {
                output = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        String getOutput() {
            return this.output;
        }
    }
}
