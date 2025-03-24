package app.octocon.app.ui.compose.screens.onboarding.pages

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import app.octocon.app.ChangeFrontMode
import app.octocon.app.ui.compose.components.AlterCard
import app.octocon.app.ui.compose.components.shared.BottomSheetListItem
import app.octocon.app.ui.compose.components.shared.OctoBottomSheet
import app.octocon.app.ui.compose.screens.GLOBAL_PADDING
import app.octocon.app.ui.model.onboarding.pages.OnboardingFrontTutorialComponent
import app.octocon.app.utils.compose
import app.octocon.app.utils.derive
import app.octocon.app.utils.state
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.change_front_mode
import octoconapp.shared.generated.resources.front_setting_bidirectional_swipe_description
import octoconapp.shared.generated.resources.front_setting_button_description
import octoconapp.shared.generated.resources.front_setting_swipe_description
import octoconapp.shared.generated.resources.onboarding_card_front_setting_body
import octoconapp.shared.generated.resources.onboarding_card_front_setting_title
import org.jetbrains.compose.resources.stringResource

@Composable
private fun ChangeFrontModeCard(
  setChangeFrontMode: (ChangeFrontMode) -> Unit,
  changeFrontMode: ChangeFrontMode
) {
  var sheetOpen by state(false)

  Surface(
    modifier = Modifier.clip(MaterialTheme.shapes.medium).clickable(onClick = { sheetOpen = true })
      .fillMaxWidth()
      .height(64.dp),
    color = MaterialTheme.colorScheme.surfaceContainerLow
  ) {
    Row(
      modifier = Modifier.fillMaxSize().padding(horizontal = GLOBAL_PADDING),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Text(
          text = Res.string.change_front_mode.compose,
          style = MaterialTheme.typography.labelLarge
        )
        Text(
          text = changeFrontMode.displayName,
          style = MaterialTheme.typography.bodySmall
        )
      }
      Icon(
        imageVector = changeFrontMode.icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(24.dp)
      )
    }
  }

  if (sheetOpen) {
    OctoBottomSheet(
      onDismissRequest = { sheetOpen = false }
    ) {
      enumValues<ChangeFrontMode>().forEach {
        BottomSheetListItem(
          icon = rememberVectorPainter(it.icon),
          title = it.displayName
        ) { setChangeFrontMode(it); sheetOpen = false }
      }
    }
  }
}

@Composable
fun OnboardingFrontTutorialScreen(
  component: OnboardingFrontTutorialComponent
) {
  val settings by component.settings.collectAsState()
  val changeFrontMode by derive { settings.changeFrontMode }

  val model by component.model.collectAsState()
  val frontingAlters = model.frontingAlters
  val primaryFront = model.primaryFront

  val sortedAlters = remember(frontingAlters, primaryFront) {
    val primary =
      primaryFront?.let { listOf(component.alters.find { it.id == primaryFront }!!) } ?: emptyList()
    val fronting =
      component.alters.filter { it.id in frontingAlters && it.id != primaryFront }.sortedBy { it.id }
    val nonFronting =
      component.alters.filter { it.id !in frontingAlters && it.id != primaryFront }.sortedBy { it.id }

    primary.plus(fronting).plus(nonFronting)
  }

  val imageContext = rememberCoroutineScope().coroutineContext
  val placeholderPainter = rememberVectorPainter(Icons.Rounded.Person)

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(GLOBAL_PADDING),
  ) {
    item {
      Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(
          modifier = Modifier.padding(GLOBAL_PADDING).fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text(
            Res.string.onboarding_card_front_setting_title.compose,
            style = MaterialTheme.typography.titleMedium
          )
          Text(
            Res.string.onboarding_card_front_setting_body.compose,
            style = MaterialTheme.typography.bodyMedium.merge(lineHeight = 1.5.em)
          )
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(GLOBAL_PADDING))
    }

    item {
      Card {
        Column(
          modifier = Modifier.padding(GLOBAL_PADDING).fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          ChangeFrontModeCard(
            setChangeFrontMode = { component.settings.setChangeFrontMode(it) },
            changeFrontMode = changeFrontMode
          )
          Text(
            stringResource(
              when (changeFrontMode) {
                ChangeFrontMode.SWIPE -> Res.string.front_setting_swipe_description
                ChangeFrontMode.BIDIRECTIONAL_SWIPE -> Res.string.front_setting_bidirectional_swipe_description
                ChangeFrontMode.BUTTON -> Res.string.front_setting_button_description
              }
            ),
            style = MaterialTheme.typography.bodyMedium.merge(lineHeight = 1.5.em)
          )
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(4.dp))
    }

    items(sortedAlters, key = { it.id }) { alter ->
      AlterCard(
        imageContext = imageContext,
        placeholderPainter = placeholderPainter,
        alter = alter,
        isFronting = alter.id in frontingAlters,
        isPrimary = primaryFront == alter.id,
        onClick = {},
        changeFrontMode = changeFrontMode,
        launchStartFront = { component.addAlterToFront(alter.id) },
        launchEndFront = {
          component.removeAlterFromFront(alter.id)
        },
        launchSetPrimaryFront = component::setPrimaryFront,
        modifier = Modifier.animateItem().padding(top = 12.dp),
        settings = settings
      )
    }
  }
}