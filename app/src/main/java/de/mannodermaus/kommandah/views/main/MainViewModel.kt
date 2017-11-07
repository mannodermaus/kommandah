package de.mannodermaus.kommandah.views.main

import android.arch.lifecycle.ViewModel
import de.mannodermaus.kommandah.managers.runtime.Interpreter
import de.mannodermaus.kommandah.models.Print
import de.mannodermaus.kommandah.models.Push
import de.mannodermaus.kommandah.models.Stop
import io.reactivex.Observable
import javax.inject.Inject

class MainViewModel
@Inject constructor(private val interpreter: Interpreter) : ViewModel() {

  // TODO Implement properly
  fun listItemChanges(): Observable<List<InstructionData>> = Observable.just(listOf(
      InstructionData(0, Push(1337)),
      InstructionData(1, Print),
      InstructionData(2, Stop)
  ))
}
