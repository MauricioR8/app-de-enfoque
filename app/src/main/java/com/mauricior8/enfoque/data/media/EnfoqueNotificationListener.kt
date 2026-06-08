package com.mauricior8.enfoque.data.media

import android.service.notification.NotificationListenerService

/**
 * Empty notification listener. Its sole purpose is to exist and be enabled by
 * the user so that [android.media.session.MediaSessionManager.getActiveSessions]
 * is allowed to return the active media controllers (now playing info).
 */
class EnfoqueNotificationListener : NotificationListenerService()
