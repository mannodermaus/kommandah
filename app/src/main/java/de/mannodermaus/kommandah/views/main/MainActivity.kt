package de.mannodermaus.kommandah.views.main

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import de.mannodermaus.kommandah.R
import de.mannodermaus.kommandah.utils.getDrawableCompat
import de.mannodermaus.kommandah.utils.setTitleTextAppearance
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

/**
 * The main screen of the application.
 * Allows for
 */
class MainActivity : AppCompatActivity(), HasSupportFragmentInjector {

  @Inject lateinit var injector: DispatchingAndroidInjector<Fragment>
  @Inject lateinit var modelFactory: ViewModelProvider.Factory
  private val viewModel by lazy { ViewModelProviders.of(this, modelFactory)[MainViewModel::class.java] }
  private val listAdapter = InstructionAdapter()
  private val disposables: CompositeDisposable = CompositeDisposable()

  override fun supportFragmentInjector(): AndroidInjector<Fragment> =
      injector

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // Stylize toolbar
    supportActionBar?.apply {
      setDisplayShowHomeEnabled(true)
      setDisplayHomeAsUpEnabled(true)
      setHomeAsUpIndicator(getDrawableCompat(R.drawable.ic_menu, tint = R.color.text_logo))
    }
    findViewById<Toolbar>(R.id.action_bar).apply {
      setTitleTextAppearance(R.style.TextAppearance_Kommandah_Logo)
    }

    // Setup RecyclerView
    rvInstructions.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    rvInstructions.adapter = listAdapter

    // Connect to ViewModel
    disposables += viewModel.listItemChanges().subscribe { listAdapter.update(it) }
  }

  override fun onDestroy() {
    super.onDestroy()

    // Disconnect from ViewModel
    disposables.clear()
  }
}
