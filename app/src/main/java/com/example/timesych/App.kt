package com.example.timesych

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.android.BuildConfig
import com.facebook.stetho.Stetho

class App : Application(), LifecycleObserver {

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        EventBus.getDefault().register(this)

        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this)
        }

        //override
        fun onTerminate() {
            EventBus.getDefault().unregister(this)
            super.onTerminate()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        //private
        fun onAppBackgrounded() {
            /*if (Stopwatch.state == State.RUNNING) {
                startStopwatchService(this)
            }*/
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        //private
        fun onAppForegrounded() {
            /*EventBus.getDefault().post(TimerStopService)
            if (Stopwatch.state == State.RUNNING) {
                EventBus.getDefault().post(StopwatchStopService)
            }*/
        }
    }
}