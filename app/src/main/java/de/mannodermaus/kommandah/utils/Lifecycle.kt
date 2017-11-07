package de.mannodermaus.kommandah.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle

open class SimpleActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
  override fun onActivityPaused(activity: Activity) {
  }

  override fun onActivityResumed(activity: Activity) {
  }

  override fun onActivityStarted(activity: Activity) {
  }

  override fun onActivityDestroyed(activity: Activity) {
  }

  override fun onActivitySaveInstanceState(activity: Activity, instanceState: Bundle) {
  }

  override fun onActivityStopped(activity: Activity) {
  }

  override fun onActivityCreated(activity: Activity, instanceState: Bundle?) {
  }
}
