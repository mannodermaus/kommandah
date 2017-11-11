package de.mannodermaus.kommandah.utils.di

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import dagger.android.AndroidInjection
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.HasSupportFragmentInjector
import de.mannodermaus.kommandah.App
import de.mannodermaus.kommandah.utils.SimpleActivityLifecycleCallbacks

/**
 * Hook in dependency injection for Activity and Fragment classes automatically,
 * with the help of dagger-android.
 *
 * This function is inspired by the Google example for Architecture Components with Dagger:
 * https://github.com/googlesamples/android-architecture-components/blob/388a10dd5a814ba6aaa9bf8dee8e7c1c5840b3a5/GithubBrowserSample/app/src/main/java/com/android/example/github/di/AppInjector.java
 */
fun App.setupInjectors() {
  // One-Time Initialization of Dependencies,
  // for Activities and Fragments as well
  component.inject(this)

  this.registerActivityLifecycleCallbacks(object : SimpleActivityLifecycleCallbacks() {
    override fun onActivityCreated(activity: Activity, instanceState: Bundle?) {
      // Automatically inject as necessary
      when (activity) {
        is HasSupportFragmentInjector -> AndroidInjection.inject(activity)
        is FragmentActivity -> {
          activity.supportFragmentManager
              .registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
                  if (f is Injectable) {
                    AndroidSupportInjection.inject(f)
                  }
                }
              }, true)
        }
      }
    }
  })
}
