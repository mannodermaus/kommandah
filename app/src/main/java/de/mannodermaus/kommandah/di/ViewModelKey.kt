package de.mannodermaus.kommandah.di

import android.arch.lifecycle.ViewModel
import dagger.MapKey
import kotlin.reflect.KClass

/**
 * Used with Dagger to bind a ViewModel into a Map,
 * so we can inject dependencies into it.
 *
 * As seen in the Google Sample for Integration
 * between Architecture Components & Dagger:
 * https://github.com/googlesamples/android-architecture-components/tree/388a10dd5a814ba6aaa9bf8dee8e7c1c5840b3a5/GithubBrowserSample
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)
