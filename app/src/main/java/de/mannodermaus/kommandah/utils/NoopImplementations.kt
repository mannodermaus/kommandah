package de.mannodermaus.kommandah.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher

// Some Adapter-like empty implementations
// for interfaces with a ton of methods
// that usually stay empty most of the time.

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

open class SimpleTextWatcher : TextWatcher {
  override fun afterTextChanged(text: Editable) {
  }

  override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
  }

  override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
  }
}
