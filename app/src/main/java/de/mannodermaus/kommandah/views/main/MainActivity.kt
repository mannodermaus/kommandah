package de.mannodermaus.kommandah.views.main

import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import com.jakewharton.rxbinding2.view.clicks
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import de.mannodermaus.kommandah.R
import de.mannodermaus.kommandah.utils.ListItemClickListener
import de.mannodermaus.kommandah.utils.ListItemDragListener
import de.mannodermaus.kommandah.utils.di.HasViewModelProviderFactory
import de.mannodermaus.kommandah.utils.extensions.setVisibleIf
import de.mannodermaus.kommandah.utils.extensions.toolbar
import de.mannodermaus.kommandah.utils.extensions.viewModel
import de.mannodermaus.kommandah.utils.widgets.StickToBottomSheetBehavior
import de.mannodermaus.kommandah.views.main.models.ExecutionStatus
import de.mannodermaus.kommandah.views.main.models.InstructionItem
import de.mannodermaus.kommandah.views.main.ui.ConsoleWindow
import de.mannodermaus.kommandah.views.main.ui.InstructionAdapter
import de.mannodermaus.kommandah.views.main.ui.showCreateInstructionDialog
import de.mannodermaus.kommandah.views.main.ui.showEditInstructionDialog
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
    ListItemClickListener<InstructionItem>,
    ListItemDragListener {

  /* Injected Dependencies & Architecture  */
  @Inject lateinit var injector: DispatchingAndroidInjector<Fragment>
  @Inject override lateinit var modelFactory: ViewModelProvider.Factory
  private val viewModel by viewModel<MainActivity, MainViewModel>()

  /* List of Instructions */
  private val listAdapter = InstructionAdapter(this, this)
  private lateinit var itemTouchHelper: ItemTouchHelper

  /* Console Handling */
  private val bottomToolbarBehavior by lazy { StickToBottomSheetBehavior.from(toolbarBottom) }
  private val console by lazy { ConsoleWindow(tvConsoleWindow) }

  /* Other */
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
    rvInstructions.itemAnimator = null
    bottomToolbarBehavior.attachToRecyclerView(toolbarBottom, rvInstructions)

    // Drag-and-drop & Swipe-to-dismiss
    itemTouchHelper = ItemTouchHelper(ListItemTouchCallback())
    itemTouchHelper.attachToRecyclerView(rvInstructions)
  }

  /* Lifecycle */

  override fun onStart() {
    super.onStart()

    // Connect to ViewModel
    setupListAdapter()
    setupExecutionButton()
    setupAddButton()
    setupExpandButton()
    setupConsoleWindow()
  }

  override fun onStop() {
    super.onStop()

    // Disconnect from ViewModel
    disposables.clear()
  }

  /* List Item Interactions */

  override fun handleListItemClick(holder: RecyclerView.ViewHolder, item: InstructionItem?) {
    item?.let {
      // TODO Create dialog when clicking on "non-item"
      showEditInstructionDialog(this, holder.adapterPosition, item.instruction) { newItem, old, new ->
        viewModel.updateInstruction(newItem, old, new)
      }
    }
  }

  override fun startListItemDrag(holder: RecyclerView.ViewHolder) {
    itemTouchHelper.startDrag(holder)
  }

  /* Private */

  private fun setupListAdapter() {
    // Keep list of instructions up-to-date
    disposables += viewModel.instructions().subscribe { listAdapter.update(it) }
  }

  private fun setupExecutionButton() {
    // Click Listener
    disposables += buttonExecute.clicks().subscribe { viewModel.runProgram() }

    // Enabled-State Events
    disposables += viewModel.instructions().map { it.isNotEmpty() }.subscribe { hasItems ->
      buttonExecute.isEnabled = hasItems
    }

    // Icon Change Events
    disposables += viewModel.executionStatus().subscribe { status ->
      when (status) {
        ExecutionStatus.PAUSED -> buttonExecute.setImageResource(R.drawable.bt_play)
        ExecutionStatus.RUNNING -> {
          buttonExecute.setImageResource(R.drawable.ic_pause)

          // Show the console window if it isn't showing already
          bottomToolbarBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        else -> throw IllegalArgumentException("Unexpected ExecutionStatus '$status'")
      }
    }
  }

  private fun setupAddButton() {
    // Click Listener
    disposables += buttonAdd.clicks().subscribe {
      showCreateInstructionDialog(this) { viewModel.addInstruction(it) }
    }
  }

  private fun setupExpandButton() {
    // Click listener
    disposables += buttonExpand.clicks().subscribe {
      // Toggle between collapsed and expanded
      bottomToolbarBehavior.state = when (bottomToolbarBehavior.state) {
        BottomSheetBehavior.STATE_COLLAPSED -> BottomSheetBehavior.STATE_EXPANDED
        BottomSheetBehavior.STATE_EXPANDED -> BottomSheetBehavior.STATE_COLLAPSED
        else -> bottomToolbarBehavior.state
      }
    }

    // React to manual sliding
    bottomToolbarBehavior.addCallback(object : BottomSheetBehavior.BottomSheetCallback() {
      override fun onSlide(bottomSheet: View, slideOffset: Float) {
      }

      override fun onStateChanged(bottomSheet: View, newState: Int) {
        val newIcon = when (newState) {
          BottomSheetBehavior.STATE_EXPANDED -> R.drawable.ic_chevron_down
          else -> R.drawable.ic_chevron_up
        }
        buttonExpand.setImageResource(newIcon)
      }
    })
  }

  private fun setupConsoleWindow() {
    // Connect to the ViewModel
    disposables += viewModel.consoleMessages().subscribe { console.handle(it) }
    disposables += viewModel.executionStatus()
        .map { it == ExecutionStatus.RUNNING }
        .subscribe { isRunning -> progressBar.setVisibleIf(isRunning) }
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

    override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int =
        if (viewModel.hasInstructionAt(viewHolder.adapterPosition)) {
          ItemTouchHelper.START or ItemTouchHelper.END
        } else {
          0
        }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
      viewModel.removeInstruction(viewHolder.adapterPosition)
    }
  }
}
