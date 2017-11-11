package de.mannodermaus.kommandah.utils.di

import android.arch.lifecycle.ViewModelProvider

interface HasViewModelProviderFactory {
  val modelFactory: ViewModelProvider.Factory
}

interface Injectable
