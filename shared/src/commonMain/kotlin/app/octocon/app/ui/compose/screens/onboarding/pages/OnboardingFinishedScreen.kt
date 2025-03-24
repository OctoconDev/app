package app.octocon.app.ui.compose.screens.onboarding.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import app.octocon.app.ui.compose.screens.GLOBAL_PADDING
import app.octocon.app.ui.model.onboarding.pages.OnboardingFinishedComponent
import app.octocon.app.utils.compose
import app.octocon.app.utils.composeColorSchemeParams
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.onboarding_card_discord_body
import octoconapp.shared.generated.resources.onboarding_card_discord_button
import octoconapp.shared.generated.resources.onboarding_card_discord_title
import octoconapp.shared.generated.resources.onboarding_card_finish_body
import octoconapp.shared.generated.resources.onboarding_card_finish_button
import octoconapp.shared.generated.resources.onboarding_card_finish_title
import octoconapp.shared.generated.resources.onboarding_card_import_body
import octoconapp.shared.generated.resources.onboarding_card_import_title

@Composable
fun OnboardingFinishedScreen(
  component: OnboardingFinishedComponent
) {
  val colorSchemeParams = composeColorSchemeParams

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(GLOBAL_PADDING),
    verticalArrangement = Arrangement.spacedBy(GLOBAL_PADDING)
  ) {
    item {
      Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(
          modifier = Modifier.padding(GLOBAL_PADDING).fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text(
            Res.string.onboarding_card_finish_title.compose,
            style = MaterialTheme.typography.titleMedium
          )
          Text(
            Res.string.onboarding_card_finish_body.compose,
            style = MaterialTheme.typography.bodyMedium.merge(lineHeight = 1.5.em)
          )
          Button(
            onClick = { component.openURL("https://octocon.app/discord", colorSchemeParams) }
          ) {
            Text(Res.string.onboarding_card_finish_button.compose)
          }
        }
      }
    }

    item {
      Card {
        Column(
          modifier = Modifier.padding(GLOBAL_PADDING).fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text(
            Res.string.onboarding_card_discord_title.compose,
            style = MaterialTheme.typography.titleMedium
          )
          Text(
            Res.string.onboarding_card_discord_body.compose,
            style = MaterialTheme.typography.bodyMedium.merge(lineHeight = 1.5.em)
          )
          Button(
            onClick = { component.openURL("https://octocon.app/invite", colorSchemeParams) }
          ) {
            Text(Res.string.onboarding_card_discord_button.compose)
          }
        }
      }
    }

    item {
      Card {
        Column(
          modifier = Modifier.padding(GLOBAL_PADDING).fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text(
            Res.string.onboarding_card_import_title.compose,
            style = MaterialTheme.typography.titleMedium
          )
          Text(
            Res.string.onboarding_card_import_body.compose,
            style = MaterialTheme.typography.bodyMedium.merge(lineHeight = 1.5.em)
          )
        }
      }
    }
  }
}
