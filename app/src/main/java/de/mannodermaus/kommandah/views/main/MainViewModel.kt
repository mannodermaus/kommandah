package de.mannodermaus.kommandah.views.main

import android.arch.lifecycle.ViewModel
import de.mannodermaus.kommandah.managers.runtime.Interpreter
import javax.inject.Inject

class MainViewModel
@Inject constructor(val interpreter: Interpreter)
  : ViewModel() {

}
