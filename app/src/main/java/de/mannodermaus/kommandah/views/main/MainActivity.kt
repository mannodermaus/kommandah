package de.mannodermaus.kommandah.views.main

import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Menu
import android.view.View
import android.widget.TextView
import com.jakewharton.rxbinding2.view.clicks
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import de.mannodermaus.kommandah.R
import de.mannodermaus.kommandah.utils.ListItemClickListener
import de.mannodermaus.kommandah.utils.ListItemDragListener
import de.mannodermaus.kommandah.utils.di.HasViewModelProviderFactory
import de.mannodermaus.kommandah.utils.extensions.format
import de.mannodermaus.kommandah.utils.extensions.longToast
import de.mannodermaus.kommandah.utils.extensions.setVisibleIf
import de.mannodermaus.kommandah.utils.extensions.toggleDrawer
import de.mannodermaus.kommandah.utils.extensions.viewModel
import de.mannodermaus.kommandah.utils.widgets.StickToBottomSheetBehavior
import de.mannodermaus.kommandah.views.main.models.InstructionItem
import de.mannodermaus.kommandah.views.main.models.ProgramEvent
import de.mannodermaus.kommandah.views.main.ui.ConsoleWindow
import de.mannodermaus.kommandah.views.main.ui.InstructionAdapter
import de.mannodermaus.kommandah.views.main.ui.showCreateInstructionDialog
import de.mannodermaus.kommandah.views.main.ui.showEditInstructionDialog
import de.mannodermaus.kommandah.views.main.ui.showEditTitleDialog
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.main_bottomtoolbar.*
import org.threeten.bp.format.DateTimeFormatter
import javax.inject.Inject

private const val NUM_RECENT_PROGRAMS = 16

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

  /* Navigation Drawer */
  private val tvDrawerHeaderProgram by lazy {
    // Requires lazy lookup from the NavigationView,
    // kotlin-android-extensions won't work here
    navigation.getHeaderView(0).findViewById<TextView>(R.id.tvDrawerHeaderProgram)
  }

  /* Other */
  private val instantFormatter by lazy { DateTimeFormatter.ofPattern(getString(R.string.format_datetime)) }
  private val disposables: CompositeDisposable = CompositeDisposable()

  override fun supportFragmentInjector(): AndroidInjector<Fragment> =
      injector

  /* Lifecycle */

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main_activity)

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

    setupDrawer()
    setupListAdapter()
    setupExecutionButton()
    setupAddButton()
    setupExpandButton()
    setupConsoleWindow()
    setupToasts()
  }

  override fun onStop() {
    super.onStop()

    // Disconnect from ViewModel
    disposables.clear()
  }

  /* Interactions */

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
          super.getSwipeDirs(recyclerView, viewHolder)
        } else {
          0
        }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
      viewModel.removeInstruction(viewHolder.adapterPosition)
    }
  }

  /* Private */

  private fun setupDrawer() {
    // Connect to ViewModel
    disposables += viewModel.executionState().subscribe {
      tvDrawerHeaderProgram.text = it.programTitle ?: getString(R.string.main_untitledprogram)
    }
    disposables += tvDrawerHeaderProgram.clicks().subscribe {
      showEditTitleDialog(this, tvDrawerHeaderProgram.text) {
        viewModel.updateProgramTitle(it)
      }
    }

    // Dynamically update the list of recently opened Programs
    disposables += viewModel.listRecentPrograms(NUM_RECENT_PROGRAMS)
        .subscribe { savedPrograms ->
          // Dynamically update the list of recently opened Programs
          val groupId = R.id.main_drawer_section_recentprograms
          navigation.menu.removeGroup(groupId)
          val group = navigation.menu.addSubMenu(
              groupId,
              Menu.NONE,
              Menu.NONE,
              R.string.main_drawer_section_recentprograms)

          savedPrograms.forEach { program ->
            val label = "${program.title} (${program.updated.format(instantFormatter)})"
            val item = group.add(groupId, Menu.NONE, Menu.NONE, label)
            item.setOnMenuItemClickListener {
              viewModel.loadProgram(program)
              true
            }
          }
        }

    // Click listeners
    buttonDrawer.setOnClickListener { drawer.toggleDrawer(GravityCompat.START) }
    navigation.setNavigationItemSelectedListener {
      when (it.itemId) {
        R.id.actionNewProgram -> {
          viewModel.newProgram()
          true
        }
        R.id.actionSaveProgram -> {
          viewModel.saveProgram()
          true
        }
        else -> false
      }
    }
  }

  private fun setupListAdapter() {
    // Keep list of instructions up-to-date
    disposables += viewModel.instructions().subscribe {
      listAdapter.update(it)
      tvEmptyList.setVisibleIf(it.isEmpty())
      rvInstructions.setVisibleIf(it.isNotEmpty())
    }
  }

  private fun setupExecutionButton() {
    // Click Listener
    disposables += buttonExecute.clicks().subscribe { viewModel.runProgram() }

    // Enabled-State Events
    disposables += viewModel.instructions().map { it.isNotEmpty() }.subscribe { hasItems ->
      buttonExecute.isEnabled = hasItems
    }

    // Icon Change Events
    disposables += viewModel.executionState().subscribe { status ->
      if (status.running) {
        buttonExecute.setImageResource(R.drawable.ic_pause)

        // Show the console window if it isn't showing already
        bottomToolbarBehavior.state = BottomSheetBehavior.STATE_EXPANDED

      } else {
        buttonExecute.setImageResource(R.drawable.bt_play)
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
    disposables += viewModel.executionState()
        .subscribe { progressBar.setVisibleIf(it.running) }
  }

  private fun setupToasts() {
    // Connect to the ViewModel
    disposables += viewModel.programEvents().subscribe {
      val message = when (it) {
        ProgramEvent.New -> getString(R.string.main_toast_newprogram)
        is ProgramEvent.Loaded -> getString(R.string.main_toast_loadedprogram, it.title)
        is ProgramEvent.Saved -> getString(R.string.main_toast_savedprogram, it.title)
      }

      longToast(message)
    }
  }
}
