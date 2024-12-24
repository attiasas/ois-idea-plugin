package org.ois.idea.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ois.core.utils.Version;
import org.ois.idea.project.generate.OisProjectGenerator;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class Utils {

    public static final String PRODUCT_ID = "ois-idea-plugin";
    public static final String PLUGIN_ID = "ois.idea.plugin";
    public static final Path USER_HOME_PATH = Paths.get(System.getProperty("user.home"));
    public static final Path HOME_PATH = USER_HOME_PATH.resolve("." + PRODUCT_ID);

    public static final Version OIS_GRADLE_PLUGIN_GENERATE_PROJECT_VERSION = new Version("0.1-SNAPSHOT");

    public static void generateFromTemplate(String resourceLocation, Path destination, Map<String, String> replaceParameters) throws IOException {
        InputStream resourceStream = OisProjectGenerator.class.getResourceAsStream(resourceLocation);
        if (resourceStream == null) {
            throw new IllegalArgumentException(String.format("Resource Template '%s' not found", resourceLocation));
        }
        // Read the template content into a string
        String templateContent;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceStream))) {
            StringBuilder contentBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append(System.lineSeparator());
            }
            templateContent = contentBuilder.toString();
        }

        // Replace placeholders with values from the parameters map
        for (Map.Entry<String, String> entry : replaceParameters.entrySet()) {
            String placeholder = "%" + entry.getKey() + "%";
            templateContent = templateContent.replace(placeholder, entry.getValue());
        }

        // Write the modified content to a build.gradle file in the output directory
        try (BufferedWriter writer = Files.newBufferedWriter(destination)) {
            writer.write(templateContent);
        }
    }

    public static void focusOnTool(Project project) {
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("OIS");
        if (toolWindow != null) {
            toolWindow.activate(null);
        }
    }

    public static class OISProgressIndicator {

        private final ProgressIndicator indicator;

        public OISProgressIndicator(@NotNull ProgressIndicator indicator) {
            this.indicator = indicator;
        }

        public void state(String text) {
            this.indicator.checkCanceled();
            this.indicator.setText(text);
        }

        public void state(String text, double fraction) {
            state(text);
            this.indicator.setFraction(fraction);
        }

        public void done() {
            this.indicator.setFraction(1);
        }

        public void cancel() {
            this.indicator.cancel();
        }
    }

    public abstract static class BackgroundTask extends Task.Backgroundable {
        private final boolean knownTimeToFinish;

        public BackgroundTask(@NlsContexts.ProgressTitle @NotNull String title, boolean knownTimeToFinish) {
            super(null, title);
            this.knownTimeToFinish = knownTimeToFinish;
        }

        public BackgroundTask(@Nullable Project project, @NlsContexts.ProgressTitle @NotNull String title, boolean knownTimeToFinish) {
            super(project, title);
            this.knownTimeToFinish = knownTimeToFinish;
        }

        @Override
        public void run(@NotNull ProgressIndicator indicator) {
            indicator.setIndeterminate(this.knownTimeToFinish);
            run(new OISProgressIndicator(indicator));
        }

        public abstract void run(@NotNull OISProgressIndicator indicator);

        public void runInBackground() {
            new Thread(() -> {
                // The progress manager is only good for foreground threads.
                if (SwingUtilities.isEventDispatchThread()) {
                    queue();
                } else {
                    // Run the refresh task when the thread is in the foreground.
                    ApplicationManager.getApplication().invokeLater(this::queue);
                }
            }).start();
        }
    }
}
