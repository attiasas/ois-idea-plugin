package org.ois.idea.utils;

import com.google.common.collect.Maps;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import java.util.Map;

public class IconUtils {
    private static final Icon defaultIcon = IconLoader.findIcon("AllIcons.Actions.Colors", IconUtils.class);
    private static final Map<String, Icon> icons = Maps.newHashMap();

    public static Icon load(String icon) {
        if (!icons.containsKey(icon)) {
            try {
                icons.put(icon, getIcon(icon));
            } catch (Exception ignored) {
                return defaultIcon;
            }
        }
        return icons.get(icon);
    }

    private static Icon getIcon(String icon) {
        return IconLoader.findIcon("/icons/" + icon.toLowerCase() + ".svg", IconUtils.class);
    }
}
