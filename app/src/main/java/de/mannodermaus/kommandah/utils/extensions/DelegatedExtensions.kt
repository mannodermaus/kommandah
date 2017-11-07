package de.mannodermaus.kommandah.utils.extensions

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.support.v4.app.FragmentActivity
import de.mannodermaus.kommandah.di.HasViewModelProviderFactory

/**
 * Shorthand Delegated Property to provide a ViewModel
 * to an Activity target.
 *
 * The key requirement refers to the Activity implementing
 * the [HasViewModelProviderFactory] interface, so that
 * a reference to the [ViewModelProvider.Factory] can be used directly.
 *
 * Use it like any other Delegated Property:
 * <code><pre>
 *  private val vm by viewModel<MyActivity, MyViewModel>()
 * </pre></code>
 */
inline fun <A, reified T : ViewModel> A.viewModel(): Lazy<T>
    where A : FragmentActivity, A : HasViewModelProviderFactory =
    lazy { ViewModelProviders.of(this, modelFactory)[T::class.java] }
