package app.octocon.app.ui.compose.screens.main.hometabs.alters.alterview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import app.octocon.app.api.model.CustomField
import app.octocon.app.ui.compose.LocalUpdateLazyListState
import app.octocon.app.ui.compose.components.AlterCustomFieldItem
import app.octocon.app.ui.compose.components.LocalFieldFocusRequester
import app.octocon.app.ui.compose.components.shared.IndeterminateProgressSpinner
import app.octocon.app.ui.compose.screens.GLOBAL_PADDING
import app.octocon.app.ui.model.main.hometabs.alters.alterview.AlterViewFieldsComponent
import app.octocon.app.utils.compose
import app.octocon.app.utils.derive
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.no_custom_fields_card_body
import octoconapp.shared.generated.resources.no_custom_fields_card_button
import octoconapp.shared.generated.resources.no_custom_fields_card_title

@Composable
fun AlterViewFieldsTab(
  component: AlterViewFieldsComponent
) {
  val api = component.api
  val model = component.model
  val updateLazyListState = LocalUpdateLazyListState.current
  val system by api.systemMe.collectAsState()

  val systemFields by derive { system.ensureData.fields }
  val fields by model.fields.collectAsState()

  val lazyListState = rememberLazyListState()

  LaunchedEffect(true) {
    updateLazyListState(lazyListState)
  }

  val focusRequester = remember { FocusRequester() }

  CompositionLocalProvider(
    LocalFieldFocusRequester provides focusRequester,
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().imePadding(),
      state = lazyListState,
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      if(!model.isLoaded.value) {
        item {
          IndeterminateProgressSpinner()
        }
        return@LazyColumn
      }
      item {
        Spacer(modifier = Modifier.size(0.dp))
      }
      if (systemFields.isEmpty()) {
        item {
          Card(
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            elevation = CardDefaults.cardElevation(
              defaultElevation = 1.0.dp
            ),
            modifier = Modifier.padding(horizontal = GLOBAL_PADDING)
          ) {
            Column(
              modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
              Text(
                Res.string.no_custom_fields_card_title.compose,
                style = MaterialTheme.typography.titleMedium
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                Res.string.no_custom_fields_card_body.compose,
                style = MaterialTheme.typography.bodyMedium.merge(
                  lineHeight = 1.5.em
                )
              )
              Spacer(modifier = Modifier.height(12.dp))
              Button(
                onClick = {
                  component.navigateToCustomFields()
                },
                colors = ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.primary,
                  contentColor = MaterialTheme.colorScheme.onPrimary
                )
              ) {
                Text(Res.string.no_custom_fields_card_button.compose)
              }
            }
          }
        }
      } else {
        items(systemFields, key = CustomField::id) { systemField ->
          val value =
            fields.find { alterField -> alterField.id == systemField.id }?.value
          AlterCustomFieldItem(
            field = systemField,
            value = value,
            updateValue = { model.updateFieldValue(systemField.id, it) }
          )
        }
      }

      item {
        Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
      }
    }
  }
}