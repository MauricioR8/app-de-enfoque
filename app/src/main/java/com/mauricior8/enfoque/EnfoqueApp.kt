package com.mauricior8.enfoque

import android.app.Application
import com.mauricior8.enfoque.data.AppRepository
import com.mauricior8.enfoque.data.PreferencesManager
import com.mauricior8.enfoque.data.WebShortcutStore

/**
 * Application class hosting a tiny manual dependency container. Keeps the app
 * lightweight (no DI framework) while still sharing single instances.
 */
class EnfoqueApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

class AppContainer(app: Application) {
    val preferences = PreferencesManager(app)
    val appRepository = AppRepository(app)
    val webShortcutStore = WebShortcutStore(app)
}
