package de.mannodermaus.kommandah.test.utils

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import de.mannodermaus.kommandah.App
import de.mannodermaus.kommandah.managers.persistence.PersistenceManager
import de.mannodermaus.kommandah.managers.runtime.Interpreter
import de.mannodermaus.kommandah.test.TestApp
import de.mannodermaus.kommandah.test.TestAppComponent
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import org.threeten.bp.Clock
import javax.inject.Inject

/**
 * Very naive implementation of a JUnit 5 Dagger Extension
 * which provides injected dependencies to test methods.
 */
class InjectExtension : BeforeTestExecutionCallback, ParameterResolver {

  private lateinit var app: TestApp

  @Inject
  lateinit var interpreter: Interpreter
  @Inject
  lateinit var clock: Clock
  @Inject
  lateinit var persistence: PersistenceManager
  @Inject
  lateinit var viewModelFactory: ViewModelProvider.Factory

  override fun beforeTestExecution(context: ExtensionContext) {
    app = TestApp()
    (app.component as TestAppComponent).inject(this)
  }

  override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean =
      parameterContext.parameter.isAnnotationPresent(Inject::class.java)

  override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
    val parameter = parameterContext.parameter
    return when (parameter.type) {
      is App -> app
      is Interpreter -> interpreter
      is Clock -> clock
      is ViewModel -> viewModelFactory.create(parameter.type as Class<ViewModel>)
      else -> throw IllegalArgumentException("Can't inject '${parameter.type}', add it to the InjectExtension first")
    }
  }
}
