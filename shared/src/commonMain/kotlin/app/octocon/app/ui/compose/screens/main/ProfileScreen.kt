package app.octocon.app.ui.compose.screens.main

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.octocon.app.ui.compose.LocalNavigationType
import app.octocon.app.ui.compose.NavigationType
import app.octocon.app.ui.compose.components.UpdateUsernameDialog
import app.octocon.app.ui.compose.components.shared.AvatarContextSheet
import app.octocon.app.ui.compose.components.shared.IndeterminateProgressSpinner
import app.octocon.app.ui.compose.components.shared.OctoScaffold
import app.octocon.app.ui.compose.components.shared.OctoTopBar
import app.octocon.app.ui.compose.components.shared.OpenDrawerNavigationButton
import app.octocon.app.ui.compose.components.shared.TitleTextState
import app.octocon.app.ui.compose.components.shared.rememberCollectPressInteractionSource
import app.octocon.app.ui.compose.screens.GLOBAL_PADDING
import app.octocon.app.ui.compose.theme.squareifyShape
import app.octocon.app.ui.compose.utils.MarkdownOutlinedTextField
import app.octocon.app.ui.compose.utils.SpotlightTooltip
import app.octocon.app.ui.model.interfaces.AvatarState
import app.octocon.app.ui.model.main.profile.ProfileComponent
import app.octocon.app.utils.DevicePlatform
import app.octocon.app.utils.compose
import app.octocon.app.utils.cropImageNatively
import app.octocon.app.utils.derive
import app.octocon.app.utils.ioDispatcher
import app.octocon.app.utils.localeFormatNumber
import app.octocon.app.utils.platformFileToImageSrc
import app.octocon.app.utils.state
import com.mikepenz.markdown.compose.LocalMarkdownColors
import com.mikepenz.markdown.m3.markdownColor
import com.mr0xf00.easycrop.core.crop.AspectRatio
import com.mr0xf00.easycrop.core.crop.RectCropShape
import com.mr0xf00.easycrop.core.crop.cropperStyle
import com.mr0xf00.easycrop.ui.BareControls
import com.mr0xf00.easycrop.ui.ImageCropperDialog
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import io.kamel.core.utils.cacheControl
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.alter_count
import octoconapp.shared.generated.resources.count_does_not_include_untracked
import octoconapp.shared.generated.resources.description
import octoconapp.shared.generated.resources.error_loading_avatar
import octoconapp.shared.generated.resources.id
import octoconapp.shared.generated.resources.name_avatar
import octoconapp.shared.generated.resources.no_avatar
import octoconapp.shared.generated.resources.no_description
import octoconapp.shared.generated.resources.no_username
import octoconapp.shared.generated.resources.note
import octoconapp.shared.generated.resources.preparing_avatar
import octoconapp.shared.generated.resources.profile
import octoconapp.shared.generated.resources.tap_to_set_avatar
import octoconapp.shared.generated.resources.tap_to_show
import octoconapp.shared.generated.resources.tooltip_alter_count_desc
import octoconapp.shared.generated.resources.tooltip_alter_count_title
import octoconapp.shared.generated.resources.tooltip_profile_avatar_desc
import octoconapp.shared.generated.resources.tooltip_profile_avatar_title
import octoconapp.shared.generated.resources.tooltip_profile_desc
import octoconapp.shared.generated.resources.uploading_avatar
import octoconapp.shared.generated.resources.username
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileScreen(component: ProfileComponent) {
  OctoScaffold(
    topBar = { topAppBarState, scrollBehavior, showSnackbar ->
      OctoTopBar(
        titleTextState = TitleTextState(
          Res.string.profile.compose,
          spotlightText = Res.string.profile.compose to Res.string.tooltip_profile_desc.compose
        ),
        navigation = {
          val navigationType = LocalNavigationType.current

          if(navigationType != NavigationType.DRAWER) {
            OpenDrawerNavigationButton()
          }
        },
        topAppBarState = topAppBarState,
        scrollBehavior = scrollBehavior
      )
    },
    padContentOnLargerScreens = true
  ) { _, _ ->
    Column {
      ProfileRoot(component)
    }
  }
}

@Composable
private fun ProfileRoot(
  component: ProfileComponent
) {
  val settings by component.settings.collectAsState()

  val cornerStyle by derive { settings.cornerStyle }
  val isSinglet by derive { settings.isSinglet }

  val system by component.system.collectAsState()
  val alters by component.alters.collectAsState()

  if (!system.isSuccess || !alters.isSuccess) {
    Column(
      modifier = Modifier.fillMaxSize().padding(GLOBAL_PADDING),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      IndeterminateProgressSpinner()
    }
    return
  }

  val systemData = system.ensureData

  val markdownColors = markdownColor()

  val coroutineScope = rememberCoroutineScope()
  val imageCropper = component.imageCropper

  val cropState = imageCropper?.imageCropper?.cropState
  val selectedImage = imageCropper?.selectedImage?.collectAsState()
  var avatarState by state(AvatarState.Loaded)

  var avatarSheetOpen by state(false)

  var usernameDialogOpen by state(false)

  var description by state(systemData.description)
  // val descriptionState = rememberTextFieldState(description.orEmpty())

  var showAlterCount by state(false)

  fun updateDescription(newDescription: String): Result<String> {
    if (newDescription.length > 3_000) return Result.failure(IllegalArgumentException("Description too long"))
    description = newDescription
    return Result.success(newDescription)
  }

  fun pushDescription() {
    if (description != systemData.description) {
      component.updateDescription(description)
    }
  }

  LaunchedEffect(selectedImage?.value) {
    if (selectedImage?.value != null) {
      avatarState = AvatarState.Preparing
      val bytes = imageCropper.getCompressedImage()
      avatarState = AvatarState.Loading
      component.setSystemAvatar(bytes, "avatar.webp")
    }
  }

  LaunchedEffect(systemData.avatarUrl) {
    if (avatarState == AvatarState.Loading && !systemData.avatarUrl.isNullOrBlank()) {
      avatarState = AvatarState.Loaded
    }
  }

  LaunchedEffect(systemData.description) {
    description = systemData.description
  }

  val filePickerLauncher = rememberFilePickerLauncher(
    type = PickerType.Image,
    mode = PickerMode.Single,
    onResult = { file ->
      file?.let {
        if(DevicePlatform.usesNativeImageCropper) {
          coroutineScope.launch {
            cropImageNatively(
              file = file,
              platformUtilities = component.platformUtilities,
              onCompressionStart = {
                avatarState = AvatarState.Preparing
              },
              onImageReady = { bytes ->
                avatarState = AvatarState.Loading
                component.setSystemAvatar(bytes, "avatar.webp")
              },
              onCanceled = {
                avatarState = AvatarState.Loaded
              },
              coroutineScope = coroutineScope
            )
          }
        } else {
          coroutineScope.launch {
            platformFileToImageSrc(file, component.platformUtilities)?.let {
              imageCropper!!.setSelectedImage(it)
            }
          }
        }
      }
    }
  )

  CompositionLocalProvider(
    LocalMarkdownColors provides markdownColors
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().imePadding(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      item {
        Spacer(modifier = Modifier.height(8.dp))
      }
      item {
        Box(
          modifier = Modifier.padding(horizontal = GLOBAL_PADDING, vertical = 8.dp)
        ) {
          SpotlightTooltip(
            title = Res.string.tooltip_profile_avatar_title.compose,
            description = Res.string.tooltip_profile_avatar_desc.compose
          ) {
            Box(
              modifier = Modifier.sizeIn(
                maxWidth = 312.dp,
                maxHeight = 312.dp
              ).aspectRatio(1.0F).clip(squareifyShape(cornerStyle) { RoundedCornerShape(96.dp) })
                .combinedClickable(
                  onClick = {
                    if(systemData.avatarUrl.isNullOrBlank()) {
                      filePickerLauncher.launch()
                    } else {
                      avatarSheetOpen = true
                    }
                  },
                ),
              contentAlignment = Alignment.Center
            ) {
              when {
                avatarState != AvatarState.Loaded -> {
                  Box(
                    modifier = Modifier.fillMaxSize().background(
                      MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    contentAlignment = Alignment.Center
                  ) {
                    IndeterminateProgressSpinner(
                      text = when (avatarState) {
                        AvatarState.Preparing -> Res.string.preparing_avatar.compose
                        else -> Res.string.uploading_avatar.compose
                      },
                      modifier = Modifier.fillMaxWidth()
                    )
                  }
                }

                systemData.avatarUrl.isNullOrBlank() -> {
                  Box(
                    modifier = Modifier.fillMaxSize().background(
                      MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    contentAlignment = Alignment.Center
                  ) {
                    Column(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalAlignment = Alignment.CenterHorizontally,
                      verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                      Icon(
                        imageVector = Icons.Rounded.Person,
                        modifier = Modifier.size(48.dp),
                        contentDescription = Res.string.no_avatar.compose
                      )
                      Text(
                        Res.string.tap_to_set_avatar.compose,
                        style = MaterialTheme.typography.labelLarge
                      )
                    }
                  }
                }

                else -> {
                  KamelImage(
                    {
                      asyncPainterResource(systemData.avatarUrl) {
                        coroutineContext = coroutineScope.coroutineContext + ioDispatcher
                        requestBuilder {
                          cacheControl("max-age=31536000, immutable")
                        }
                      }
                    },
                    // onLoading = { PlaceholderImage(isFronting, placeholderPainter) },
                    onFailure = {
                      Text(Res.string.error_loading_avatar.compose)
                    },
                    contentDescription = stringResource(
                      Res.string.name_avatar,
                      systemData.username ?: systemData.id
                    ),
                    modifier = Modifier.fillMaxSize(),
                    animationSpec = tween()
                  )
                }
              }
            }
          }
        }
      }

      item {
        Row(
          modifier = Modifier.fillMaxWidth()
            .padding(horizontal = GLOBAL_PADDING, vertical = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          OutlinedTextField(
            modifier = Modifier.weight(2f),
            value = system.ensureData.username ?: Res.string.no_username.compose,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            interactionSource = rememberCollectPressInteractionSource {
              usernameDialogOpen = true
            },
            label = { Text(Res.string.username.compose) }
          )
          OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = system.ensureData.id,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(Res.string.id.compose) }
          )
        }
      }

      item {
        MarkdownOutlinedTextField(
          value = description,
          onValueChange = ::updateDescription,
          onFinishEditing = ::pushDescription,
          label = Res.string.description.compose,
          placeholder = Res.string.no_description.compose,
          modifier = Modifier.fillMaxWidth().padding(horizontal = GLOBAL_PADDING, vertical = 8.dp)
        )
        /*MarkdownOutlinedTextField(
          internalValue = description,
          state = descriptionState,
          update = ::updateDescription,
          onFinishEditing = ::pushDescription,
          label = Res.string.description.compose,
          placeholder = Res.string.no_description.compose,
          modifier = Modifier.fillMaxWidth().padding(horizontal = GLOBAL_PADDING, vertical = 8.dp)
        )*/
      }

      if(!isSinglet) {
        item {
          Column(
            modifier = Modifier.padding(horizontal = GLOBAL_PADDING, vertical = 8.dp)
          ) {
            SpotlightTooltip(
              title = Res.string.tooltip_alter_count_title.compose,
              description = Res.string.tooltip_alter_count_desc.compose
            ) {
              Card(
                colors = CardDefaults.cardColors(
                  containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
              ) {
                Row(
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.fillMaxWidth().padding(GLOBAL_PADDING)
                ) {
                  Text(
                    text = Res.string.alter_count.compose,
                    style = MaterialTheme.typography.labelLarge
                  )

                  Spacer(modifier = Modifier.size(8.dp))

                  if (showAlterCount) {
                    Text(
                      text = localeFormatNumber(alters.ensureData.filterNot { it.untracked }.size),
                      style = MaterialTheme.typography.bodyMedium.merge(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                      )
                    )
                  } else {
                    FilledTonalButton(
                      onClick = { showAlterCount = true }
                    ) {
                      Text(text = Res.string.tap_to_show.compose)
                    }
                  }
                }
              }
            }
            Column(
              modifier = Modifier.padding(
                top = 11.dp,
                bottom = 16.dp
              ),
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
                Res.string.count_does_not_include_untracked.compose,
                style = MaterialTheme.typography.bodySmall
              )
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.size(16.dp))
      }
    }

    if (usernameDialogOpen) {
      UpdateUsernameDialog(
        initialUsername = systemData.username.orEmpty(),
        onDismissRequest = { usernameDialogOpen = false },
        launchUpdateUsername = { username ->
          component.updateUsername(username)
          usernameDialogOpen = false
        }
      )
    }

    if (avatarSheetOpen) {
      AvatarContextSheet(
        onDismissRequest = { avatarSheetOpen = false },
        avatar = systemData.avatarUrl,
        launchSetAvatar = {
          filePickerLauncher.launch()
        },
        launchRemoveAvatar = {
          component.removeSystemAvatar()
        }
      )
    }

    if (cropState != null) {
      ImageCropperDialog(
        state = cropState,
        style = cropperStyle(
          shapes = listOf(RectCropShape),
          aspects = listOf(AspectRatio(1, 1))
        ),
        dialogShape = squareifyShape(cornerStyle) { RoundedCornerShape(8.dp) },
        cropControls = { BareControls(it) }
      )
    }
  }
}
