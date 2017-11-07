package de.mannodermaus.kommandah.views.main

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import de.mannodermaus.kommandah.R
import timber.log.Timber
import javax.inject.Inject

class MainActivity : AppCompatActivity(), HasSupportFragmentInjector {

  @Inject
  lateinit var injector: DispatchingAndroidInjector<Fragment>
  @Inject
  lateinit var modelFactory: ViewModelProvider.Factory

  private val viewModel by lazy { ViewModelProviders.of(this, modelFactory)[MainViewModel::class.java] }

  override fun supportFragmentInjector(): AndroidInjector<Fragment> =
      injector

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    Timber.d("My ViewModel: $viewModel")
  }
}
