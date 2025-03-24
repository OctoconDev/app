package app.octocon.app.ui.compose.screens.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import app.octocon.app.ui.compose.LocalChildPanelsMode
import app.octocon.app.ui.compose.components.shared.BackNavigationButton
import app.octocon.app.ui.compose.components.shared.OctoScaffold
import app.octocon.app.ui.compose.components.shared.OctoTopBar
import app.octocon.app.ui.compose.components.shared.TitleTextState
import app.octocon.app.ui.compose.screens.GLOBAL_PADDING
import app.octocon.app.ui.compose.theme.getSubsectionStyle
import app.octocon.app.ui.model.main.supportus.SupportUsComponent
import app.octocon.app.utils.compose
import app.octocon.app.utils.composeColorSchemeParams
import app.octocon.app.utils.derive
import app.octocon.app.utils.generateMarkdownTypography
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.model.markdownAnimations
import com.mikepenz.markdown.model.markdownPadding
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.kofi_logo
import octoconapp.shared.generated.resources.open
import octoconapp.shared.generated.resources.patreon_logo
import octoconapp.shared.generated.resources.support_us
import octoconapp.shared.generated.resources.support_us_kofi_card_body
import octoconapp.shared.generated.resources.support_us_kofi_card_title
import octoconapp.shared.generated.resources.support_us_patreon_card_body
import octoconapp.shared.generated.resources.support_us_patreon_card_title
import octoconapp.shared.generated.resources.tooltip_support_us_desc
import octoconapp.shared.generated.resources.transparency
import octoconapp.shared.generated.resources.transparency_text
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun SupportUsScreen(
  component: SupportUsComponent
) {
  val colorSchemeParams = composeColorSchemeParams
  val settings by component.settings.collectAsState()

  val fontSizeScalar by derive { settings.fontSizeScalar }

  val openPatreon: () -> Unit =
    remember(colorSchemeParams) { { component.openPatreon(colorSchemeParams) } }
  val openKofi: () -> Unit =
    remember(colorSchemeParams) { { component.openKofi(colorSchemeParams) } }

  OctoScaffold(
    topBar = { topAppBarState, scrollBehavior, showSnackbar ->
      OctoTopBar(
        titleTextState = TitleTextState(
          Res.string.support_us.compose,
          spotlightText = Res.string.support_us.compose to Res.string.tooltip_support_us_desc.compose
        ),
        navigation = {
          val childPanelsMode = LocalChildPanelsMode.current

          if(childPanelsMode == ChildPanelsMode.SINGLE) {
            BackNavigationButton(component::navigateBack)
          }
        },
        topAppBarState = topAppBarState,
        scrollBehavior = scrollBehavior
      )
    },
    padContentOnLargerScreens = true,
    content = { _, _ ->
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(GLOBAL_PADDING)
      ) {
        item {
          SupportUsCard(
            onClick = openPatreon,
            title = Res.string.support_us_patreon_card_title.compose,
            description = Res.string.support_us_patreon_card_body.compose,
            iconContent = {
              Image(
                painter = painterResource(Res.drawable.patreon_logo),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
              )
            },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
          )
        }
        item { Spacer(modifier = Modifier.size(16.dp)) }
        item {
          SupportUsCard(
            onClick = openKofi,
            title = Res.string.support_us_kofi_card_title.compose,
            description = Res.string.support_us_kofi_card_body.compose,
            iconContent = {
              Image(
                painter = painterResource(Res.drawable.kofi_logo),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
              )
            },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
          )
        }
        item {
          Row(
            modifier = Modifier.padding(top = 24.dp, bottom = 16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              Res.string.transparency.compose,
              style = getSubsectionStyle(fontSizeScalar = fontSizeScalar)
            )
          }
        }
        item {
          Markdown(
            Res.string.transparency_text.compose,
            colors = markdownColor(),
            padding = markdownPadding(block = 8.dp),
            typography = generateMarkdownTypography(MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)),
            animations = markdownAnimations(
              animateTextSize = { this }
            )
          )
        }
      }
    }
  )
}

@Composable
private fun SupportUsCard(
  onClick: () -> Unit,
  title: String,
  description: String,
  iconContent: (@Composable () -> Unit),
  colors: CardColors
) {
  Card(
    colors = colors,
    onClick = onClick,
  ) {
    Column(
      modifier = Modifier.padding(16.dp).fillMaxWidth()
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          iconContent()
          Text(
            title,
            style = MaterialTheme.typography.titleMedium
          )
        }
        Icon(
          imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
          contentDescription = Res.string.open.compose,
          tint = MaterialTheme.colorScheme.tertiary
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        description,
        style = MaterialTheme.typography.bodyMedium.merge(
          lineHeight = 1.5.em
        )
      )
    }
  }
}