package app.octocon.app.ui.compose.screens.onboarding.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import app.octocon.app.ui.compose.screens.GLOBAL_PADDING
import app.octocon.app.ui.model.onboarding.pages.OnboardingSystemOrSingletComponent
import app.octocon.app.utils.compose
import octoconapp.shared.resources.Res
import octoconapp.shared.resources.im_a_singlet
import octoconapp.shared.resources.system_or_singlet_body
import octoconapp.shared.resources.system_or_singlet_title

@Composable
fun OnboardingSystemOrSingletScreen(
  component: OnboardingSystemOrSingletComponent
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(GLOBAL_PADDING),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    item {
      Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
        Column(
          modifier = Modifier.padding(GLOBAL_PADDING).fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text(
            Res.string.system_or_singlet_title.compose,
            style = MaterialTheme.typography.titleMedium
          )
          Text(
            Res.string.system_or_singlet_body.compose,
            style = MaterialTheme.typography.bodyMedium.merge(lineHeight = 1.5.em)
          )
          FilledTonalButton(
            onClick = component::navigateToMainApp
          ) {
            Text(Res.string.im_a_singlet.compose)
          }
        }
      }
    }
  }
}
