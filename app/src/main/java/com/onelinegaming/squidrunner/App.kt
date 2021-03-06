package com.onelinegaming.squidrunner

import android.app.Application
import android.content.Context
import com.onelinegaming.squidrunner.utils.PrefsHelper


class App : Application() {

    companion object {
        private lateinit var instance: App

        val context: Context
            get() = instance.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        PrefsHelper.init(this)
        instance = this
    }
}