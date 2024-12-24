package org.ois.idea.events;

import com.intellij.util.messages.Topic;
import org.ois.idea.ui.OisToolWindow;

public interface ProjectViewEvents {
    // Do not call this topic!, use ViewSelectorAction.selectView instead so the checkbox at the action bar will be updated as well
    Topic<ProjectViewEvents> ON_PROJECT_VIEW_SELECT = Topic.create("Project View selected", ProjectViewEvents.class);

    void setView(OisToolWindow.View view);
}
