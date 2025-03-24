package app.octocon.app.ui.compose.screens.main.settings.pages

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import app.octocon.app.ui.compose.LocalChildPanelsMode
import app.octocon.app.ui.compose.components.FixedLibrariesContainer
import app.octocon.app.ui.compose.components.shared.BackNavigationButton
import app.octocon.app.ui.compose.components.shared.OctoScaffold
import app.octocon.app.ui.compose.components.shared.OctoTopBar
import app.octocon.app.ui.compose.components.shared.TitleTextState
import app.octocon.app.ui.model.main.settings.SettingsOpenSourceLicensesComponent
import app.octocon.app.utils.compose
import app.octocon.app.utils.rememberLibraries
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.open_source_licenses

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun SettingsOpenSourceLicensesScreen(
  component: SettingsOpenSourceLicensesComponent
) {
  // val platformUtilities: PlatformUtilities = component.platformUtilities

  /*val updateTopBarNavigation = LocalUpdateTopBarNavigationComposable.current
  val updateTitleText = LocalUpdateTitleText.current

  LaunchedEffect(true) {
    updateTitleText(TitleTextState("Open source licenses"))
    updateTopBarNavigation {
      IconButton(onClick = navigator::pop) {
        Icon(
          imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
          contentDescription = "Back"
        )
      }
    }
  }*/

  val libraries by rememberLibraries()

  OctoScaffold(
    topBar = { topAppBarState, scrollBehavior, showSnackbar ->
      OctoTopBar(
        navigation = {
          val childPanelsMode = LocalChildPanelsMode.current

          if(childPanelsMode == ChildPanelsMode.SINGLE) {
            BackNavigationButton(component::navigateBack)
          }
        },
        titleTextState = TitleTextState(Res.string.open_source_licenses.compose),
        topAppBarState = topAppBarState,
        scrollBehavior = scrollBehavior
      )
    },
    content = { _, _ ->
      FixedLibrariesContainer(
        libraries = libraries,
        /*licenseDialogBody = { library ->
            Text(library.licenses.filter { !it.licenseContent.isNullOrEmpty() }
              .joinToString(separator = "\n\n") { license -> license.licenseContent!! })
          },
          licenseDialogConfirmText = "Ok",*/
        modifier = Modifier.fillMaxSize()
      )
    }
  )
}
