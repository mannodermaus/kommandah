package de.mannodermaus.kommandah.test.utils.junit5

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import de.mannodermaus.kommandah.App
import de.mannodermaus.kommandah.managers.persistence.PersistenceManager
import de.mannodermaus.kommandah.managers.runtime.Interpreter
import de.mannodermaus.kommandah.test.TestApp
import de.mannodermaus.kommandah.test.TestAppComponent
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import org.threeten.bp.Clock
import javax.inject.Inject

/**
 * Very naive implementation of a JUnit 5 Dagger Extension
 * which provides injected dependencies to test methods.
 *
 * TODO Actually this would be a nice idea for an "actual" JUnit 5 Extension
 * if we got rid of the awful doubling of the Component's contents. Mhh...
 */
class InjectExtension : BeforeEachCallback, ParameterResolver {

  private lateinit var app: TestApp

  @Inject
  lateinit var interpreter: Interpreter
  @Inject
  lateinit var clock: Clock
  @Inject
  lateinit var persistence: PersistenceManager
  @Inject
  lateinit var viewModelFactory: ViewModelProvider.Factory

  override fun beforeEach(context: ExtensionContext) {
    // Provide Dagger context by instantiating a Test App
    app = TestApp()
    (app.component as TestAppComponent).inject(this)
  }

  override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean =
      parameterContext.parameter.isAnnotationPresent(Injected::class.java)

  override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
    val parameter = parameterContext.parameter

    // Utilize generic ViewModelFactory for those types of dependencies
    val viewModel = ifSubtype<ViewModel>(parameter.type) { viewModelFactory.create(it) }
    if (viewModel != null) return viewModel

    // Otherwise, check simple object mappings
    return ifSubtype<App>(parameter.type) { app }
        ?: ifSubtype<Interpreter>(parameter.type) { interpreter }
        ?: ifSubtype<PersistenceManager>(parameter.type) { persistence }
        ?: ifSubtype<Clock>(parameter.type) { clock }
        ?: throw IllegalArgumentException("Can't inject '${parameter.type}', add it to the InjectExtension first")
  }


  @Suppress("UNCHECKED_CAST")
  private inline fun <reified T : Any> ifSubtype(cls: Class<*>, creator: (Class<T>) -> T): T? {
    return if (T::class.java.isAssignableFrom(cls)) {
      creator(cls as Class<T>)
    } else {
      null
    }
  }
}
