package de.mannodermaus.kommandah.views.main

import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.Editable
import android.widget.ScrollView
import com.jakewharton.rxbinding2.view.clicks
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import de.mannodermaus.kommandah.R
import de.mannodermaus.kommandah.di.HasViewModelProviderFactory
import de.mannodermaus.kommandah.utils.ListItemDragListener
import de.mannodermaus.kommandah.utils.SimpleTextWatcher
import de.mannodermaus.kommandah.utils.extensions.appendLine
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
class MainActivity : AppCompatActivity(),
    HasSupportFragmentInjector,
    HasViewModelProviderFactory,
    ListItemDragListener {

  @Inject lateinit var injector: DispatchingAndroidInjector<Fragment>
  @Inject override lateinit var modelFactory: ViewModelProvider.Factory

  private val viewModel by viewModel<MainActivity, MainViewModel>()
  private val listAdapter = InstructionAdapter(this)
  private lateinit var itemTouchHelper: ItemTouchHelper

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

    // Drag-and-drop & Swipe-to-dismiss
//    val itemTouchHelperCb = ItemTouchHelperDragCallback(listAdapter)
    itemTouchHelper = ItemTouchHelper(ListItemTouchCallback())
    itemTouchHelper.attachToRecyclerView(rvInstructions)
  }

  /* ListItemDragListener */

  override fun startDrag(holder: RecyclerView.ViewHolder) {
    itemTouchHelper.startDrag(holder)
  }

  /* Lifecycle */

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
    // Keep list of instructions up-to-date
    disposables += viewModel.instructions().subscribe { listAdapter.update(it) }
  }

  private fun setupExecutionButton() {
    // Click Listener
    disposables += buttonExecute.clicks().subscribe { viewModel.runProgram() }

    // Icon Change Events
    disposables += viewModel.executionStatus().subscribe { status ->
      when (status) {
        ExecutionStatus.PAUSED -> {
          buttonExecute.setImageResource(R.drawable.ic_play)
          buttonExecute.isEnabled = true
        }
        ExecutionStatus.RUNNING -> {
          buttonExecute.setImageResource(R.drawable.ic_pause)
          buttonExecute.isEnabled = false

          // Show the console window if it isn't showing already
          val behavior = BottomSheetBehavior.from(toolbarBottom)
          behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        else -> throw IllegalArgumentException("Unexpected ExecutionStatus '$status'")
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
        is ConsoleEvent.Clear ->
          tvConsoleWindow.text = ""

        is ConsoleEvent.Started ->
          tvConsoleWindow.appendLine(R.string.main_console_started, it.numLines)
              .appendLine(R.string.main_console_separator)
              .appendLine()

        is ConsoleEvent.Message ->
          tvConsoleWindow.appendLine(R.string.main_console_log, it.line, it.message)

        is ConsoleEvent.Finished ->
          tvConsoleWindow.appendLine()
              .appendLine(R.string.main_console_separator)
              .appendLine(R.string.main_console_finished)

        is ConsoleEvent.Error ->
          tvConsoleWindow
              .appendLine()
              .appendLine(R.string.main_console_separator)
              .appendLine(R.string.main_console_error)
              .appendLine(it.cause.message.toString())
      }
    }
  }

  /**
   * Implementor of list item interactions (drag-and-drop & swipe-to-dismiss)
   */
  private inner class ListItemTouchCallback : ItemTouchHelper.SimpleCallback(
      ItemTouchHelper.UP or ItemTouchHelper.DOWN,
      ItemTouchHelper.START or ItemTouchHelper.END) {

    override fun onMove(recyclerView: RecyclerView, source: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
      viewModel.swapInstructions(source.adapterPosition, target.adapterPosition)
      return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
      viewModel.removeInstruction(viewHolder.adapterPosition)
    }
  }
}
