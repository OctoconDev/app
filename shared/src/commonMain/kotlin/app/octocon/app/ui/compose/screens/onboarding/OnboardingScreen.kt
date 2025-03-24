package app.octocon.app.ui.compose.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import app.octocon.app.ui.compose.screens.GLOBAL_PADDING
import app.octocon.app.ui.compose.screens.onboarding.pages.OnboardingFinishedScreen
import app.octocon.app.ui.compose.screens.onboarding.pages.OnboardingFrontTutorialScreen
import app.octocon.app.ui.compose.screens.onboarding.pages.OnboardingSystemOrSingletScreen
import app.octocon.app.ui.compose.screens.onboarding.pages.OnboardingWelcomeScreen
import app.octocon.app.ui.model.onboarding.OnboardingComponent
import app.octocon.app.utils.compose
import app.octocon.app.utils.derive
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.pages.PagesScrollAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.finish
import octoconapp.shared.generated.resources.get_started
import octoconapp.shared.generated.resources.im_a_system
import octoconapp.shared.generated.resources.next
import octoconapp.shared.generated.resources.previous

@Composable
fun OnboardingScreen(
  component: OnboardingComponent
) {
  val settings by component.settings.collectAsState()
  val reduceMotion by derive { settings.reduceMotion }

  val topAppBarState = rememberTopAppBarState()
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)

  val model by component.model.collectAsState()
  val pages by component.pages.subscribeAsState()

  val previousEnabled = model.previousButtonEnabled
  val nextEnabled = model.nextButtonEnabled

  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      CenterAlignedTopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
          Text(Res.string.get_started.compose)
        }
      )
    },
    bottomBar = {
      BottomAppBar {
        Row(
          modifier = Modifier.padding(horizontal = GLOBAL_PADDING),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Spacer(modifier = Modifier.weight(1f))
          FilledTonalButton(
            enabled = previousEnabled,
            onClick = component::navigateToPreviousPage
          ) {
            Text(Res.string.previous.compose)
          }
          if (nextEnabled) {
            Button(
              onClick = component::navigateToNextPage
            ) {
              Text(if(pages.selectedIndex == 1) Res.string.im_a_system.compose else Res.string.next.compose)
            }
          } else {
            Button(
              onClick = component::navigateToMainApp
            ) {
              Text(Res.string.finish.compose)
            }
          }
        }
      }
    },
    content = { innerPadding ->
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .consumeWindowInsets(innerPadding)
      ) {
        ChildPages(
          pages = pages,
          onPageSelected = component::navigateToPage,
          scrollAnimation = if(reduceMotion) PagesScrollAnimation.Disabled else PagesScrollAnimation.Default
        ) { _, page ->
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {
            Column(modifier = Modifier.fillMaxHeight().widthIn(max = 650.dp)) {
              when (page) {
                is OnboardingComponent.Page.WelcomePage -> OnboardingWelcomeScreen(page.component)
                is OnboardingComponent.Page.SystemOrSingletPage -> OnboardingSystemOrSingletScreen(page.component)
                is OnboardingComponent.Page.FrontTutorialPage -> OnboardingFrontTutorialScreen(page.component)
                is OnboardingComponent.Page.FinishedPage -> OnboardingFinishedScreen(page.component)
              }
            }
          }
        }
      }
    }
  )
}