package de.mannodermaus.kommandah.views.main

import android.arch.lifecycle.ViewModel
import de.mannodermaus.kommandah.managers.runtime.Interpreter
import de.mannodermaus.kommandah.models.Print
import de.mannodermaus.kommandah.models.Push
import de.mannodermaus.kommandah.models.Stop
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainViewModel
@Inject constructor(private val interpreter: Interpreter) : ViewModel() {

  // TODO Implement properly

  /**
   * Stream of events related to the Instructions that make up the current Program
   */
  fun instructions(): Observable<List<InstructionData>> = Observable.just(listOf(
      InstructionData(0, Push(1337)),
      InstructionData(1, Print),
      InstructionData(2, Stop)
  ))

  /**
   * Stream of events related to the current execution status of the current Program
   */
  fun executionStatus(): Observable<ExecutionStatus> = Observable.just(ExecutionStatus.PAUSED)

  /**
   * Stream of events related to handling console messages during execution
   */
  fun consoleMessages(): Observable<ConsoleEvent> =
      Observable.just<ConsoleEvent>(ConsoleEvent.Clear)
          .concatWith(
              Observable.interval(500, TimeUnit.MILLISECONDS)
                  .map { ConsoleEvent.Message("Interval $it") })
          .observeOn(AndroidSchedulers.mainThread())
}
