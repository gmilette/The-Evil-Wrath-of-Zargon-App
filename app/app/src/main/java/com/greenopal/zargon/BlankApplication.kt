package com.greenopal.zargon

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BlankApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
