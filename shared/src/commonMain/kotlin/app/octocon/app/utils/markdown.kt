package app.octocon.app.utils

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.Density
import app.octocon.app.ui.compose.LocalMarkdownComponents
import com.mikepenz.markdown.compose.LocalMarkdownColors
import com.mikepenz.markdown.compose.components.MarkdownComponents
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.ImageData
import com.mikepenz.markdown.model.ImageTransformer
import com.mikepenz.markdown.model.MarkdownColors
import com.mikepenz.markdown.model.MarkdownTypography
import com.mikepenz.markdown.model.PlaceholderConfig
import com.mikepenz.markdown.model.markdownAnimations
import org.intellij.markdown.IElementType
import org.intellij.markdown.ast.ASTNode

/*object KamelImageTransformerImpl : ImageTransformer {

  @Composable
  override fun transform(link: String): ImageData? {
    *//*return rememberAsyncImagePainter(
      model = ImageRequest.Builder(LocalPlatformContext.current)
        .data(link)
        .size(coil3.size.Size.ORIGINAL)
        .build()
    ).let { ImageData(it) }*//*

    return null
  }

  @Composable
  override fun intrinsicSize(painter: Painter): Size {
    return painter.intrinsicSize
    *//*var size by remember(painter) { mutableStateOf(painter.intrinsicSize) }

    if (painter is AsyncImagePainter) {
      LaunchedEffect(painter.state) {
        painter.state.painter?.let {
          size = it.intrinsicSize
        }
      }
    }

    return size*//*
  }
}*/

/*@Suppress("FunctionName")
tailrec fun ASTNode.HOISTED_findChildOfTypeRecursive(type: IElementType, nodesToProcess: List<ASTNode> = listOf(this)): ASTNode? {
  if (nodesToProcess.isEmpty()) return null

  val node = nodesToProcess.first()
  val remainingNodes = nodesToProcess.drop(1)

  return if (node.type == type) {
    node
  } else {
    HOISTED_findChildOfTypeRecursive(type, remainingNodes + node.children)
  }
}*/

@Suppress("FunctionName")
fun ASTNode.HOISTED_findChildOfTypeRecursive(type: IElementType): ASTNode? {
  children.forEach {
    if (it.type == type) {
      return it
    } else {
      val found = it.HOISTED_findChildOfTypeRecursive(type)
      if (found != null) {
        return found
      }
    }
  }
  return null
}

/*@Composable
fun KamelMarkdownImage(content: String, node: ASTNode) {
  val link = node.HOISTED_findChildOfTypeRecursive(MarkdownElementTypes.LINK_DESTINATION)
    ?.getTextInNode(content)?.toString() ?: return

  KamelImage(
    { asyncPainterResource(link) },
    contentDescription = null
  )
}*/


@Composable
fun generateMarkdownComponents(): MarkdownComponents = markdownComponents(
  /*image = {
    KamelMarkdownImage(it.content, it.node)
  }*/
)

/*object KamelImageTransformer : ImageTransformer {
  @Composable
  override fun transform(link: String): ImageData? {
    val sanitizedUrl = runCatching { Url(link) }.getOrNull() ?: return null
    return when (val resource = asyncPainterResource(sanitizedUrl)) {
      is Resource.Failure -> null
      is Resource.Loading -> null
      is Resource.Success -> ImageData(resource.value)
    }
  }
}*/

object NoOpImageTransformer : ImageTransformer {
  @Composable
  override fun transform(link: String): ImageData? = null

  @Composable
  override fun intrinsicSize(painter: Painter): Size {
    return Size(0f, 0f)
  }

  override fun placeholderConfig(density: Density, containerSize: Size, intrinsicImageSize: Size): PlaceholderConfig {
    return PlaceholderConfig(Size(0f, 0f))
  }
}

@Composable
fun MarkdownRenderer(
  content: String?,
  colors: MarkdownColors = LocalMarkdownColors.current,
  components: MarkdownComponents = LocalMarkdownComponents.current ?: generateMarkdownComponents(),
  typography: MarkdownTypography = generateMarkdownTypography(),
  modifier: Modifier = Modifier
) {
  Markdown(
    content = content.orEmpty(),
    components = components,
    colors = colors,
    imageTransformer = NoOpImageTransformer,
    typography = typography,
    modifier = modifier,
    animations = markdownAnimations(
      animateTextSize = { this }
    )
  )
}

@Composable
fun generateMarkdownTypography(
  baseStyle: TextStyle = MaterialTheme.typography.bodyMedium,
  color: Color = LocalContentColor.current
) =
  markdownTypography(
    h1 = MaterialTheme.typography.titleLarge.copy(color = color),
    h2 = MaterialTheme.typography.titleMedium.copy(color = color),
    h3 = MaterialTheme.typography.titleSmall.copy(color = color),
    h4 = MaterialTheme.typography.titleSmall.copy(color = color),
    h5 = MaterialTheme.typography.titleSmall.copy(color = color),
    h6 = MaterialTheme.typography.titleSmall.copy(color = color),
    text = baseStyle.copy(color = color),
    code = baseStyle.copy(fontFamily = FontFamily.Monospace, color = color),
    quote = baseStyle.plus(SpanStyle(fontStyle = FontStyle.Italic, color = color)),
    paragraph = baseStyle.copy(color = color),
    ordered = baseStyle.copy(color = color),
    bullet = baseStyle.copy(color = color),
    list = baseStyle.copy(color = color)
  )