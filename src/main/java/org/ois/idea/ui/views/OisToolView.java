package org.ois.idea.ui.views;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import org.ois.idea.project.OisProject;

public class OisToolView extends SimpleToolWindowPanel implements Disposable {
    protected final MessageBusConnection projectBusConnection;
    protected final Project project;

    public OisToolView(@NotNull Project project) {
        super(false);
        this.project = project;
        this.projectBusConnection = project.getMessageBus().connect(this);
    }

    public Project getIdeaProject() { return project; }

    @Override
    public void dispose() {
        // Disconnect and release resources from the project bus connection
        projectBusConnection.disconnect();
    }
}
