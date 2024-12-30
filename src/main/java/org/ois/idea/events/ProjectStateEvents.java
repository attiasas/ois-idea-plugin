package org.ois.idea.events;

import com.intellij.util.messages.Topic;

public interface ProjectStateEvents {

    Topic<ProjectStateEvents> ON_PROJECT_REFRESH_STARTED = Topic.create("Refreshing OIS project", ProjectStateEvents.class);
    Topic<ProjectStateEvents> ON_PROJECT_REFRESH_ENDED = Topic.create("OIS Project updated", ProjectStateEvents.class);

    Topic<ProjectStateEvents> ON_PROJECT_DEPENDENCY_REFRESH_STARTED = Topic.create("Refreshing OIS dependencies", ProjectStateEvents.class);
    Topic<ProjectStateEvents> ON_PROJECT_DEPENDENCY_REFRESH_ENDED = Topic.create("OIS Dependencies updated", ProjectStateEvents.class);

    Topic<ProjectStateEvents> ON_PROJECT_DEBUG_STARTED = Topic.create("Running OIS project", ProjectStateEvents.class);
    Topic<ProjectStateEvents> ON_PROJECT_DEBUG_ENDED = Topic.create("Simulation ended", ProjectStateEvents.class);

    Topic<ProjectStateEvents> ON_PROJECT_EXPORT_STARTED = Topic.create("Exporting OIS Simulation", ProjectStateEvents.class);
    Topic<ProjectStateEvents> ON_PROJECT_EXPORT_ENDED = Topic.create("OIS Simulation exported", ProjectStateEvents.class);

    /**
     * Called when a project state is changed
     */
    void update();
}
