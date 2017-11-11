package de.mannodermaus.kommandah.managers.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.mannodermaus.kommandah.utils.di.KommandahViewModelFactory
import de.mannodermaus.kommandah.utils.di.ViewModelKey
import de.mannodermaus.kommandah.views.main.MainViewModel

@Module
abstract class ViewModelModule {

  @Binds
  @IntoMap
  @ViewModelKey(MainViewModel::class)
  abstract fun bindViewModel(viewModel: MainViewModel): ViewModel

  @Binds
  abstract fun bindViewModelFactory(factory: KommandahViewModelFactory): ViewModelProvider.Factory
}
