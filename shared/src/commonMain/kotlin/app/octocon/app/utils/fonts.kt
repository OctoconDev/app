package app.octocon.app.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.lexiereadable_bold
import octoconapp.shared.generated.resources.lexiereadable_regular
import octoconapp.shared.generated.resources.opendyslexic_bold
import octoconapp.shared.generated.resources.opendyslexic_regular
import octoconapp.shared.generated.resources.ubuntu_medium
import octoconapp.shared.generated.resources.ubuntu_regular
import org.jetbrains.compose.resources.Font


object Fonts {
  val ubuntu
    @Composable
    get() = FontFamily(
      Font(
        Res.font.ubuntu_regular,
        FontWeight.Normal,
        FontStyle.Normal
      ),

      Font(
        Res.font.ubuntu_medium,
        FontWeight.Medium,
        FontStyle.Normal
      )
    )

  val lexieReadable
    @Composable
    get() = FontFamily(
      Font(
        Res.font.lexiereadable_regular,
        FontWeight.Normal,
        FontStyle.Normal
      ),

      Font(
        Res.font.lexiereadable_bold,
        FontWeight.Medium,
        FontStyle.Normal
      )
    )

  val openDyslexic
    @Composable
    get() = FontFamily(
      Font(
        Res.font.opendyslexic_regular,
        FontWeight.Normal,
        FontStyle.Normal
      ),

      Font(
        Res.font.opendyslexic_bold,
        FontWeight.Medium,
        FontStyle.Normal
      )
    )
}