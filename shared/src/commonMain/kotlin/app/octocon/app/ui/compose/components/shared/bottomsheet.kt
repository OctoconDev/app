package app.octocon.app.ui.compose.components.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun BottomSheetListItem(
  icon: Painter,
  title: String,
  iconTint: Color = LocalContentColor.current,
  onClick: () -> Unit,
) =
  BottomSheetListItem(
    title = title,
    decorator = {
      Icon(painter = icon, tint = iconTint, contentDescription = null)
    },
    onClick = onClick
  )

@Composable
fun BottomSheetListItem(
  imageVector: ImageVector,
  title: String,
  iconTint: Color = LocalContentColor.current,
  onClick: () -> Unit,
) =
  BottomSheetListItem(
    title = title,
    decorator = {
      Icon(imageVector = imageVector, tint = iconTint, contentDescription = null)
    },
    onClick = onClick
  )

@Composable
fun BottomSheetListItem(
  title: String,
  decorator: @Composable () -> Unit,
  onClick: () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .height(64.dp)
      .padding(start = 16.dp), verticalAlignment = Alignment.CenterVertically
  ) {
    decorator()
    Spacer(modifier = Modifier.width(20.dp))
    Text(text = title)
  }
}