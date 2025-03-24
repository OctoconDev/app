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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Emergency
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material.icons.rounded.Workspaces
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import app.octocon.app.FontSizeScalar
import app.octocon.app.ui.compose.LocalNavigationType
import app.octocon.app.ui.compose.NavigationType
import app.octocon.app.ui.compose.components.octoconLogoVectorPainter
import app.octocon.app.ui.compose.components.shared.OctoScaffold
import app.octocon.app.ui.compose.components.shared.OctoTopBar
import app.octocon.app.ui.compose.components.shared.OpenDrawerNavigationButton
import app.octocon.app.ui.compose.components.shared.TitleTextState
import app.octocon.app.ui.compose.screens.GLOBAL_PADDING
import app.octocon.app.ui.compose.theme.getSubsectionStyle
import app.octocon.app.ui.model.main.resources.ResourcesComponent
import app.octocon.app.utils.DevicePlatform
import app.octocon.app.utils.compose
import app.octocon.app.utils.composeColorSchemeParams
import app.octocon.app.utils.derive
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.did_research_org
import octoconapp.shared.generated.resources.did_research_org_causes_development_body
import octoconapp.shared.generated.resources.did_research_org_causes_development_title
import octoconapp.shared.generated.resources.did_research_org_comorbidity_body
import octoconapp.shared.generated.resources.did_research_org_comorbidity_title
import octoconapp.shared.generated.resources.did_research_org_home_body
import octoconapp.shared.generated.resources.did_research_org_home_title
import octoconapp.shared.generated.resources.did_research_org_treatment_body
import octoconapp.shared.generated.resources.did_research_org_treatment_title
import octoconapp.shared.generated.resources.did_research_org_what_is_did_body
import octoconapp.shared.generated.resources.did_research_org_what_is_did_title
import octoconapp.shared.generated.resources.discord_card_body
import octoconapp.shared.generated.resources.discord_card_title
import octoconapp.shared.generated.resources.discord_logo
import octoconapp.shared.generated.resources.emergency_crisis_hotlines_body
import octoconapp.shared.generated.resources.emergency_crisis_hotlines_title
import octoconapp.shared.generated.resources.external_resources_disclaimer
import octoconapp.shared.generated.resources.hotlines
import octoconapp.shared.generated.resources.invite_bot_card_body
import octoconapp.shared.generated.resources.invite_bot_card_title
import octoconapp.shared.generated.resources.mental_health_hotlines_body
import octoconapp.shared.generated.resources.mental_health_hotlines_title
import octoconapp.shared.generated.resources.note
import octoconapp.shared.generated.resources.open
import octoconapp.shared.generated.resources.our_community
import octoconapp.shared.generated.resources.resources
import octoconapp.shared.generated.resources.tooltip_resources_desc
import octoconapp.shared.generated.resources.website_card_body
import octoconapp.shared.generated.resources.website_card_title
import org.jetbrains.compose.resources.painterResource

@Suppress("LocalVariableName")
@Composable
fun ResourcesScreen(
  component: ResourcesComponent
) {
  val settingsData by component.settings.collectAsState()
  val fontSizeScalar by derive { settingsData.fontSizeScalar }

  val our_community = Res.string.our_community.compose
  val hotlines = Res.string.hotlines.compose
  val did_research_org = Res.string.did_research_org.compose

  val colorSchemeParams = composeColorSchemeParams

  val openURL: (String) -> Unit = remember(colorSchemeParams) { { component.openResource(it, colorSchemeParams) } }

  OctoScaffold(
    topBar = { topAppBarState, scrollBehavior, showSnackbar ->
      OctoTopBar(
        titleTextState = TitleTextState(
          Res.string.resources.compose,
          spotlightText = Res.string.resources.compose to Res.string.tooltip_resources_desc.compose
        ),
        navigation = {
          val navigationType = LocalNavigationType.current

          if (navigationType != NavigationType.DRAWER) {
            OpenDrawerNavigationButton()
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
        contentPadding = PaddingValues(horizontal = GLOBAL_PADDING)
      ) {
        ResourcesSection(
          our_community,
          fontSizeScalar,
          { OctoconDiscordCard(openURL) },
          { OctoconInviteBotCard(openURL) },
          { OctoconWebsiteCard(openURL) }
        )

        if(!DevicePlatform.isiOS) {
          ResourcesSection(
            hotlines,
            fontSizeScalar,
            { EmergencyCrisisHotlinesCard(openURL) },
            { MentalHealthHotlinesCard(openURL) }
          )

          ResourcesSection(
            did_research_org,
            fontSizeScalar,
            { DidResearchHomeCard(openURL) },
            { DidResearchWhatIsDidCard(openURL) },
            { DidResearchCauseAndDevelopmentCard(openURL) },
            { DidResearchComorbidityCard(openURL) },
            { DidResearchTreatmentCard(openURL) }
          )

          item {
            Column(
              modifier = Modifier.padding(vertical = 16.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = Icons.Rounded.Info,
                  modifier = Modifier.size(16.dp),
                  contentDescription = null
                )
                Text(
                  Res.string.note.compose,
                  style = MaterialTheme.typography.labelMedium
                )
              }
              Text(
                text = Res.string.external_resources_disclaimer.compose,
                style = MaterialTheme.typography.bodySmall
              )
            }
          }
        }
      }
    },
  )
}

@Suppress("FunctionName")
private fun LazyListScope.ResourcesSection(
  title: String,
  fontSizeScalar: FontSizeScalar,
  vararg contents: @Composable () -> Unit
) {
  item {
    Row(
      modifier = Modifier.padding(vertical = 24.dp).fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        title,
        style = getSubsectionStyle(fontSizeScalar = fontSizeScalar)
      )
      /*Icon(
        imageVector = icon,
        contentDescription = title,
        tint = MaterialTheme.colorScheme.tertiary
      )*/
    }
  }

  contents.forEachIndexed { index, content ->
    if (index != 0) {
      item {
        Spacer(modifier = Modifier.height(16.dp))
      }
    }
    item {
      content()
    }
  }
}

@Composable
private fun BasicResourceCard(
  title: String,
  description: String,
  uri: String,
  icon: ImageVector,
  openURL: (String) -> Unit
) =
  BasicResourceCard(
    title = title,
    description = description,
    uri = uri,
    iconContent = {
      Icon(
        imageVector = icon,
        contentDescription = null
      )
    },
    openURL = openURL
  )

@Composable
private fun BasicResourceCard(
  title: String,
  description: String,
  uri: String,
  iconContent: (@Composable () -> Unit)?,
  openURL: (String) -> Unit
) {
  Card(
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ),
    onClick = { openURL(uri) },
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
          if (iconContent != null) {
            iconContent()
          }
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
        ),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
      )
    }
  }
}

@Composable
private fun OctoconDiscordCard(openURL: (String) -> Unit) =
  BasicResourceCard(
    title = Res.string.discord_card_title.compose,
    description = Res.string.discord_card_body.compose,
    uri = "https://octocon.app/discord",
    iconContent = {
      Image(
        painter = painterResource(Res.drawable.discord_logo),
        contentDescription = null,
        modifier = Modifier.size(24.dp)
      )
    },
    openURL = openURL
  )

@Composable
private fun OctoconInviteBotCard(openURL: (String) -> Unit) =
  BasicResourceCard(
    title = Res.string.invite_bot_card_title.compose,
    description = Res.string.invite_bot_card_body.compose,
    uri = "https://octocon.app/invite",
    iconContent = {
      Image(
        painter = painterResource(Res.drawable.discord_logo),
        contentDescription = null,
        modifier = Modifier.size(24.dp)
      )
    },
    openURL = openURL
  )

@Composable
private fun OctoconWebsiteCard(openURL: (String) -> Unit) =
  BasicResourceCard(
    title = Res.string.website_card_title.compose,
    description = Res.string.website_card_body.compose,
    uri = "https://octocon.app",
    iconContent = {
      Image(
        painter = octoconLogoVectorPainter(),
        // painter = painterResource(Res.drawable.octocon_logo),
        contentDescription = null,
        modifier = Modifier.size(24.dp)
      )
    },
    openURL = openURL
  )

@Composable
private fun EmergencyCrisisHotlinesCard(openURL: (String) -> Unit) =
  BasicResourceCard(
    title = Res.string.emergency_crisis_hotlines_title.compose,
    description = Res.string.emergency_crisis_hotlines_body.compose,
    uri = "https://faq.whatsapp.com/1417269125743673",
    icon = Icons.Rounded.Emergency,
    openURL = openURL
  )

@Composable
private fun MentalHealthHotlinesCard(openURL: (String) -> Unit) =
  BasicResourceCard(
    title = Res.string.mental_health_hotlines_title.compose,
    description = Res.string.mental_health_hotlines_body.compose,
    uri = "https://www.helpguide.org/find-help.htm",
    icon = Icons.Rounded.Phone,
    openURL = openURL
  )

@Composable
private fun DidResearchHomeCard(openURL: (String) -> Unit) =
  BasicResourceCard(
    title = Res.string.did_research_org_home_title.compose,
    description = Res.string.did_research_org_home_body.compose,
    uri = "https://did-research.org",
    icon = Icons.Rounded.Home,
    openURL = openURL
  )

@Composable
private fun DidResearchWhatIsDidCard(openURL: (String) -> Unit) =
  BasicResourceCard(
    title = Res.string.did_research_org_what_is_did_title.compose,
    description = Res.string.did_research_org_what_is_did_body.compose,
    uri = "https://did-research.org/did/",
    icon = Icons.Rounded.QuestionMark,
    openURL = openURL
  )

@Composable
private fun DidResearchCauseAndDevelopmentCard(openURL: (String) -> Unit) =
  BasicResourceCard(
    title = Res.string.did_research_org_causes_development_title.compose,
    description = Res.string.did_research_org_causes_development_body.compose,
    uri = "https://did-research.org/origin/",
    icon = Icons.Rounded.Psychology,
    openURL = openURL
  )

@Composable
private fun DidResearchComorbidityCard(openURL: (String) -> Unit) =
  BasicResourceCard(
    title = Res.string.did_research_org_comorbidity_title.compose,
    description = Res.string.did_research_org_comorbidity_body.compose,
    uri = "https://did-research.org/comorbid/",
    icon = Icons.Rounded.Workspaces,
    openURL = openURL
  )

@Composable
private fun DidResearchTreatmentCard(openURL: (String) -> Unit) =
  BasicResourceCard(
    title = Res.string.did_research_org_treatment_title.compose,
    description = Res.string.did_research_org_treatment_body.compose,
    uri = "https://did-research.org/treatment/",
    icon = Icons.Rounded.LocalHospital,
    openURL = openURL
  )