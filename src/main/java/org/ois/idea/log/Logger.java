package org.ois.idea.log;

import com.intellij.notification.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.awt.RelativePoint;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.util.ExceptionUtils;
import org.ois.core.utils.log.ILogger;
import org.ois.idea.utils.IconUtils;

import java.io.OutputStream;

import static javax.swing.event.HyperlinkEvent.EventType.ACTIVATED;

public class Logger {

    private static final NotificationGroup EVENT_LOG_NOTIFIER = NotificationGroupManager.getInstance().getNotificationGroup("OIS Log");
    private static final NotificationGroup BALLOON_NOTIFIER = NotificationGroupManager.getInstance().getNotificationGroup("OIS Errors");
    private static Notification lastNotification;

    private static final com.intellij.openapi.diagnostic.Logger ideaLogger = com.intellij.openapi.diagnostic.Logger.getInstance(Logger.class);

    private static final String INFO_TITLE = "OIS";
    private static final String ERROR_TITLE = "OIS Error";

    public static Logger getInstance() { return ApplicationManager.getApplication().getService(Logger.class); }

    private Logger(){}

    public void debug(String message, Throwable throwable) {
        debug(message + System.lineSeparator() + ExceptionUtils.getStackTrace(throwable));
    }

    public void debug(String message) { ideaLogger.debug(message); }

    public void info(String message) {
        ideaLogger.info(message);
        log(INFO_TITLE, message, NotificationType.INFORMATION);
    }

    public void warn(String message) {
        ideaLogger.warn(message);
        log(INFO_TITLE, message, NotificationType.WARNING);
    }

    public void warn(String message, Throwable throwable) {
        warn(message + ": " + throwable.getMessage());
    }

    public void error(String message) {
        // We log to IntelliJ log in "warn" log level to avoid popup annoying fatal errors
        ideaLogger.warn(message);
        NotificationType notificationType = NotificationType.ERROR;
        popupBalloon(message, notificationType);
        log(ERROR_TITLE, message, notificationType);
    }

    public void error(String message, Throwable throwable) {
        NotificationType notificationType = NotificationType.ERROR;
        popupBalloon(message, notificationType);
        String title = StringUtils.defaultIfBlank(throwable.getMessage(), ERROR_TITLE);
        log(title, message + System.lineSeparator() + ExceptionUtils.getStackTrace(throwable), notificationType);
    }

    public OutputStream getLogOutputStream(ILogger.Level level) {
        return new OutputStream() {
            private final StringBuilder buffer = new StringBuilder();
            @Override
            public void write(int b) {
                if (b == '\n') {
                    switch (level) {
                        case Info -> info(buffer.toString());
                        case Debug -> debug(buffer.toString());
                        case Warn, Error -> warn(buffer.toString());
                    }
                    buffer.setLength(0);
                } else {
                    buffer.append((char) b);
                }
            }
        };
    }

    private static void log(String title, String details, NotificationType notificationType) {
        if (StringUtils.isBlank(details)) {
            details = title;
        }
        Notifications.Bus.notify(EVENT_LOG_NOTIFIER.createNotification(title, prependPrefix(details, notificationType), notificationType));
    }

    private static String prependPrefix(String message, NotificationType notificationType) {
        return switch (notificationType) {
            case WARNING -> "[WARN] " + message;
            case ERROR -> "[ERROR] " + message;
            default -> "[INFO] " + message;
        };
    }

    private static void popupBalloon(String content, NotificationType notificationType) {
        if (lastNotification != null) {
            lastNotification.hideBalloon();
        }
        if (StringUtils.isBlank(content)) {
            content = ERROR_TITLE;
        }
        Notification notification = BALLOON_NOTIFIER.createNotification(ERROR_TITLE, content, notificationType);
        lastNotification = notification;
        Notifications.Bus.notify(notification);
    }

    public static void showActionableBalloon(Project project, String content, Runnable action) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(content,
                        IconUtils.load("AllIcons.Actions.Colors"),
                        JBColor.foreground(),
                        JBColor.background(),
                        event -> {
                            if (event.getEventType() != ACTIVATED) {
                                return;
                            }
                            action.run();
                        })
                .setCloseButtonEnabled(true)
                .setHideOnAction(true)
                .setHideOnClickOutside(true)
                .setHideOnLinkClick(true)
                .setHideOnKeyOutside(true)
                .setDialogMode(true)
                .createBalloon()
                .show(RelativePoint.getNorthEastOf(statusBar.getComponent()), Balloon.Position.atRight);
    }
}
