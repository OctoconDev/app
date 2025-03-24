package app.octocon.app.ui.compose.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.octocon.app.ui.compose.LocalMarkdownComponents
import app.octocon.app.ui.compose.LocalSetShowPushNotifications
import app.octocon.app.ui.compose.LocalSpotlightLongPressTimeout
import app.octocon.app.ui.compose.LocalSpotlightTooltipsEnabled
import app.octocon.app.ui.compose.screens.main.MainAppScreen
import app.octocon.app.ui.compose.screens.onboarding.OnboardingScreen
import app.octocon.app.ui.compose.theme.OctoconTheme
import app.octocon.app.ui.model.RootComponent
import app.octocon.app.utils.DevicePlatform
import app.octocon.app.utils.InitPushNotifications
import app.octocon.app.utils.PlatformEvent
import app.octocon.app.utils.abifix.fixedABIEmptyStackAnimation
import app.octocon.app.utils.derive
import app.octocon.app.utils.generateMarkdownComponents
import app.octocon.app.utils.kamelConfig
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.experimental.stack.ChildStack
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.LocalStackAnimationProvider
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.StackAnimation
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.StackAnimationProvider
import io.kamel.image.config.LocalKamelConfig

// If only value classes could be `const`...
val GLOBAL_PADDING = 16.dp

const val VERSION_CODE = "69"
const val APP_VERSION = "1.3.0 \"Ammonite\""
const val IS_BETA = true

/*@Composable
fun App(
  initialSettings: Settings,
  platformUtilities: PlatformUtilities,
  platformEventFlow: Flow<PlatformEvent>
) {
  val scope = rememberCoroutineScope()
  val apiViewModel = apiViewModel(scope, platformUtilities)

  *//*setSingletonImageLoaderFactory { context ->
    newImageLoader(context, true)
  }*//*

  val permissionsFactory: PermissionsControllerFactory =
    rememberPermissionsControllerFactory()

  val permissionsController: PermissionsController =
    remember(permissionsFactory) { permissionsFactory.createPermissionsController() }

  val permissionsScope: CoroutineScope = rememberCoroutineScope()

  BindEffect(permissionsController)

  val settingsViewModel = settingsViewModel(
    initialSettings,
    platformUtilities::saveSettings,
    platformUtilities,
    permissionsController,
    permissionsScope
  )

  val settings by settingsViewModel.settings.collectAsState()

  val vmToken by derive { settings.token }
  val tokenIsProtected = initialSettings.tokenIsProtected
  var stealthModeActive by savedState(initialSettings.stealthModeEnabled)

  var pinCheckPassed by savedState(false)

  val markdownComponents: MarkdownComponents = generateMarkdownComponents()

  fun loadClient(realToken: String) =
    apiViewModel.loadClient(realToken, settingsViewModel)

  LaunchedEffect(vmToken) {
    if (settings.tokenIsProtected || vmToken == null) return@LaunchedEffect
    loadClient(vmToken!!)
  }

  LaunchedEffect(platformEventFlow, platformUtilities) {
    platformEventFlow.collect { event ->
      when (event) {
        is PlatformEvent.Handleable -> {
          event.handle(platformUtilities)
        }

        is PlatformEvent.LoginTokenReceived -> {
          if (vmToken != null) return@collect
          settingsViewModel.setToken(event.token)
        }

        is PlatformEvent.PushNotificationTokenReceived -> {
          if (settings.tokenIsProtected || vmToken == null) return@collect
          apiViewModel.tryInitPushNotifications(
            event.token,
            settingsViewModel,
            permissionsController,
            permissionsScope,
            platformUtilities::showAlert
          )
        }
      }
    }
  }

  CompositionLocalProvider(
    LocalPlatformUtilities provides platformUtilities,
    LocalKamelConfig provides kamelConfig,
    LocalSettingsViewModel provides settingsViewModel,
    LocalApiViewModel provides apiViewModel,
    LocalMarkdownComponents provides markdownComponents
  ) {
    OctoconTheme {
      // Gate 1: Stealth mode
      if (stealthModeActive) {
        StealthApp(
          settingsViewModel = settingsViewModel,
          disableStealthMode = { stealthModeActive = false }
        )
        return@OctoconTheme
      }

      // Gate 2: PIN screen
      if (tokenIsProtected && !pinCheckPassed) {
        PINScreen(
          settingsViewModel = settingsViewModel,
          setPinCheckPassed = {
            loadClient(it)
            pinCheckPassed = true
          },
        )
        return@OctoconTheme
      }

      // Gate 3: Auth screen
      if (vmToken == null) {
        AuthScreen()
        return@OctoconTheme
      }

      // Gate 4: Onboarding screen
      if (!settings.hasViewedOnboarding) {
        OnboardingScreen()
        return@OctoconTheme
      }

      // Main app

      val drawerState = rememberDrawerState(DrawerValue.Closed)

      val drawerScope = rememberCoroutineScope()
      val toggleDrawer: (Boolean) -> Unit = {
        drawerScope.launch {
          if (it) drawerState.open() else drawerState.close()
        }
      }

      Navigator(TabsScreen) {
        OctoconNavigationDrawer(
          drawerState = drawerState,
          toggleDrawer = toggleDrawer
        ) {
          CompositionLocalProvider(
            LocalModalDrawerToggler provides toggleDrawer
          ) {
            Box(
              modifier = Modifier.fillMaxSize().let {
                if (settings.quickExitEnabled) {
                  it.pointerInput(Unit) {
                    detectTapGestures(
                      onDoubleTap = { platformUtilities.exitApplication() }
                    )
                  }
                } else {
                  it
                }
              }
            ) {
              CurrentScreen()
            }
          }
        }
      }
    }
  }
}*/

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun RootScreen(
  component: RootComponent
) {
  val settings by component.settings.collectAsState()

  val spotlightEnabled by derive { settings.spotlightEnabled }
  val spotlightLongPressTimeout by derive { settings.spotlightLongPressTimeout }

  val (tryInitPushNotifications, setShowPushNotifications) = InitPushNotifications()

  val markdownComponents = generateMarkdownComponents()

  // This has to be handed in Compose-land
  if(DevicePlatform.hasPushNotifications) {
    LaunchedEffect(component.platformEventFlow) {
      component.platformEventFlow.collect { event ->
        when (event) {
          is PlatformEvent.PushNotificationTokenReceived -> {
            if (settings.tokenIsProtected || settings.token == null) return@collect
            tryInitPushNotifications(
              event.token,
              component.api,
              component.settings,
              component.platformUtilities
            )
          }
          // Ignore everything else
          else -> Unit
        }
      }
    }
  }

  CompositionLocalProvider(
    LocalStackAnimationProvider provides remember { object : StackAnimationProvider {
      override fun <C : Any, T : Any> provide(): StackAnimation<C, T>? = fixedABIEmptyStackAnimation()
    } },
    LocalKamelConfig provides kamelConfig,
    LocalMarkdownComponents provides markdownComponents,
    LocalSetShowPushNotifications provides { enabled ->
      setShowPushNotifications(
        enabled,
        component.api,
        component.settings,
        component.platformUtilities
      )
    },
    LocalSpotlightTooltipsEnabled provides spotlightEnabled,
    LocalSpotlightLongPressTimeout provides spotlightLongPressTimeout
  ) {
    OctoconTheme(
      fontChoice = settings.fontChoice,
      fontSizeScalar = settings.fontSizeScalar,
      cornerStyle = settings.cornerStyle,
      themeColor = settings.themeColor,
      colorMode = settings.colorMode,
      dynamicColorType = settings.dynamicColorType,
      colorContrastLevel = settings.colorContrastLevel,
      amoledMode = settings.amoledMode
    ) {
      Surface(modifier = Modifier.fillMaxSize()) {
        ChildStack(component.stack) {
          when (val child = it.instance) {
            is RootComponent.Child.LoginChild -> LoginScreen(child.component)
            is RootComponent.Child.PINEntryChild -> PINScreen(child.component)
            is RootComponent.Child.StealthAppChild -> StealthAppScreen(child.component)
            is RootComponent.Child.MainAppChild -> MainAppScreen(child.component)
            is RootComponent.Child.OnboardingChild -> OnboardingScreen(child.component)
          }
        }
      }
    }
  }
}