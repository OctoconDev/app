package app.octocon.app.ui.compose.components.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.octocon.app.ui.compose.screens.GLOBAL_PADDING
import app.octocon.app.utils.compose
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.loading

@Composable
fun IndeterminateProgressSpinner(
  padding: Dp = GLOBAL_PADDING,
  text: String? = null,
  modifier: Modifier = Modifier.fillMaxSize()
) {
  val realText = text ?: Res.string.loading.compose
  Column(
    modifier = modifier.padding(padding),
    verticalArrangement = Arrangement.spacedBy(10.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    CircularProgressIndicator()
    Text(realText, style = MaterialTheme.typography.labelLarge)
  }
}