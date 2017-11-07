package de.mannodermaus.kommandah.views.main

import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.widget.ScrollView
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import de.mannodermaus.kommandah.R
import de.mannodermaus.kommandah.di.HasViewModelProviderFactory
import de.mannodermaus.kommandah.utils.SimpleTextWatcher
import de.mannodermaus.kommandah.utils.extensions.toolbar
import de.mannodermaus.kommandah.utils.extensions.viewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.main_bottomtoolbar.*
import javax.inject.Inject

/**
 * The main screen of the application.
 * Allows the user to compose & execute their Program.
 */
class MainActivity : AppCompatActivity(), HasSupportFragmentInjector, HasViewModelProviderFactory {

  @Inject lateinit var injector: DispatchingAndroidInjector<Fragment>
  @Inject override lateinit var modelFactory: ViewModelProvider.Factory

  private val viewModel by viewModel<MainActivity, MainViewModel>()
  private val listAdapter = InstructionAdapter()
  private val disposables: CompositeDisposable = CompositeDisposable()

  override fun supportFragmentInjector(): AndroidInjector<Fragment> =
      injector

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main_activity)

    // Setup UI
    toolbar {
      showHome(true)
      setHomeIcon(R.drawable.ic_menu, tint = R.color.text_logo)
      setTitleTextAppearance(R.style.TextAppearance_Kommandah_Logo)
    }

    // Setup RecyclerView
    rvInstructions.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
    rvInstructions.adapter = listAdapter
    applyMoveAwayBehavior(rvInstructions, toolbarBottom)
  }

  override fun onStart() {
    super.onStart()

    // Connect to ViewModel
    setupListAdapter()
    setupExecutionButton()
    setupConsoleWindow()
  }

  override fun onStop() {
    super.onStop()

    // Disconnect from ViewModel
    disposables.clear()
  }

  /* Private */

  private fun setupListAdapter() {
    disposables += viewModel.instructions().subscribe {
      listAdapter.update(it)
    }
  }

  private fun setupExecutionButton() {
    disposables += viewModel.executionStatus().subscribe {
      when (it) {
        ExecutionStatus.PAUSED -> buttonExecute.setImageResource(R.drawable.ic_play)
        ExecutionStatus.RUNNING -> buttonExecute.setImageResource(R.drawable.ic_pause)
        else -> throw IllegalArgumentException("Unexpected ExecutionStatus '$it'")
      }
    }
  }

  private fun setupConsoleWindow() {
    // Automatically scroll to the bottom when new text arrives
    tvConsoleWindow.addTextChangedListener(object : SimpleTextWatcher() {
      override fun afterTextChanged(text: Editable) {
        consoleWindow.fullScroll(ScrollView.FOCUS_DOWN)
      }
    })

    // Connect to the ViewModel
    disposables += viewModel.consoleMessages().subscribe {
      when (it) {
        is ConsoleEvent.Clear -> tvConsoleWindow.text = ""
        is ConsoleEvent.Message -> tvConsoleWindow.append("${it.message}\n")
      }
    }
  }
}
