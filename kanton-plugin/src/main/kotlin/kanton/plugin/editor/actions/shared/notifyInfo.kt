package kanton.plugin.editor.actions.shared

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

fun notifyInfo(project: Project, message: String) {
    NotificationGroupManager.getInstance()
        .getNotificationGroup("Kanton")
        .createNotification(message, NotificationType.INFORMATION)
        .notify(project)
}
