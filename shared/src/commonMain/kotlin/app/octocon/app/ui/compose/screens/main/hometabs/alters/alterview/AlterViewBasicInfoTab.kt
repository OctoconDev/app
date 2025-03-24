package app.octocon.app.ui.compose.screens.main.hometabs.alters.alterview

import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import app.octocon.app.api.APIState
import app.octocon.app.api.model.MyTag
import app.octocon.app.api.model.SecurityLevel
import app.octocon.app.ui.compose.LocalUpdateLazyListState
import app.octocon.app.ui.compose.components.LocalFieldFocusRequester
import app.octocon.app.ui.compose.components.shared.AttachTagDialog
import app.octocon.app.ui.compose.components.shared.AvatarContextSheet
import app.octocon.app.ui.compose.components.shared.BottomSheetListItem
import app.octocon.app.ui.compose.components.shared.IndeterminateProgressSpinner
import app.octocon.app.ui.compose.components.shared.OctoBottomSheet
import app.octocon.app.ui.compose.components.shared.UpdateColorDialog
import app.octocon.app.ui.compose.components.shared.rememberCollectPressInteractionSource
import app.octocon.app.ui.compose.screens.GLOBAL_PADDING
import app.octocon.app.ui.compose.theme.ThemeFromColor
import app.octocon.app.ui.compose.theme.getSubsectionStyle
import app.octocon.app.ui.compose.theme.hexStringToARGBInt
import app.octocon.app.ui.compose.theme.squareifyShape
import app.octocon.app.ui.compose.utils.MarkdownOutlinedTextField
import app.octocon.app.ui.compose.utils.SpotlightTooltip
import app.octocon.app.ui.model.interfaces.AvatarState
import app.octocon.app.ui.model.main.hometabs.alters.alterview.AlterViewBasicInfoComponent
import app.octocon.app.utils.DevicePlatform
import app.octocon.app.utils.compose
import app.octocon.app.utils.cropImageNatively
import app.octocon.app.utils.derive
import app.octocon.app.utils.ioDispatcher
import app.octocon.app.utils.platformFileToImageSrc
import app.octocon.app.utils.savedState
import app.octocon.app.utils.state
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
import octoconapp.shared.generated.resources.add_tag
import octoconapp.shared.generated.resources.alias
import octoconapp.shared.generated.resources.attach_tag
import octoconapp.shared.generated.resources.color
import octoconapp.shared.generated.resources.description
import octoconapp.shared.generated.resources.discord_help_dialog_body
import octoconapp.shared.generated.resources.discord_info
import octoconapp.shared.generated.resources.error_loading_avatar
import octoconapp.shared.generated.resources.id
import octoconapp.shared.generated.resources.name
import octoconapp.shared.generated.resources.name_avatar
import octoconapp.shared.generated.resources.no_alias
import octoconapp.shared.generated.resources.no_avatar
import octoconapp.shared.generated.resources.no_color
import octoconapp.shared.generated.resources.no_description
import octoconapp.shared.generated.resources.no_pronouns
import octoconapp.shared.generated.resources.no_proxy_name
import octoconapp.shared.generated.resources.no_tags_to_attach
import octoconapp.shared.generated.resources.ok
import octoconapp.shared.generated.resources.open_tag
import octoconapp.shared.generated.resources.preparing_avatar
import octoconapp.shared.generated.resources.pronouns
import octoconapp.shared.generated.resources.proxy_name
import octoconapp.shared.generated.resources.remove_alter_from_tag
import octoconapp.shared.generated.resources.security_level
import octoconapp.shared.generated.resources.tags
import octoconapp.shared.generated.resources.tap_to_set_avatar
import octoconapp.shared.generated.resources.tooltip_alter_avatars_desc
import octoconapp.shared.generated.resources.tooltip_alter_avatars_title
import octoconapp.shared.generated.resources.tooltip_tags_desc
import octoconapp.shared.generated.resources.unnamed_alter
import octoconapp.shared.generated.resources.untracked
import octoconapp.shared.generated.resources.uploading_avatar
import octoconapp.shared.generated.resources.what_does_this_mean
import org.jetbrains.compose.resources.stringResource

@Composable
fun AlterViewBasicInfoTab(
  component: AlterViewBasicInfoComponent
) {
  val api = component.api
  val model = component.model

  val updateLazyListState = LocalUpdateLazyListState.current
  val platformUtilities = component.platformUtilities
  val system by api.systemMe.collectAsState()

  val haptics = LocalHapticFeedback.current

  val settings by component.settings.collectAsState()
  val showAlterIds by derive { settings.showAlterIds }

  val name by model.name.collectAsState()
  val description by model.description.collectAsState()
  val pronouns by model.pronouns.collectAsState()
  val alias by model.alias.collectAsState()
  val proxyName by model.proxyName.collectAsState()
  val color by model.color.collectAsState()
  val securityLevel by model.securityLevel.collectAsState()
  val untracked by model.untracked.collectAsState()

  var avatarSheetOpen by savedState(false)
  var colorDialogOpen by savedState(false)
  var attachTagDialogOpen by savedState(false)
  var discordHelpDialogOpen by savedState(false)

  var selectedTag by state<MyTag?>(null)

  val lazyListState = rememberLazyListState()

  LaunchedEffect(true) {
    updateLazyListState(lazyListState)
  }

  val internalTags by api.tags.collectAsState()

  val allTags by derive {
    when (internalTags) {
      is APIState.Success -> internalTags.ensureData
      else -> emptyList()
    }
  }

  val alterTags by derive {
    allTags.filter { tag -> tag.alters.contains(model.id) }
  }

  val allSecurityLevels = remember { SecurityLevel.entries }
  val folderPainter = rememberVectorPainter(Icons.Rounded.Folder)

  val focusRequester = remember { FocusRequester() }

  val coroutineScope = rememberCoroutineScope()
  val imageCropper = component.imageCropper

  val cropState = imageCropper?.imageCropper?.cropState
  val selectedImage = imageCropper?.selectedImage?.collectAsState()
  var avatarState by state(AvatarState.Loaded)

  val discordLinked by derive {
    system.ensureData.discordID != null
  }

  @Suppress("LocalVariableName")
  val unnamed_alter = Res.string.unnamed_alter.compose

  val apiAlter by model.apiAlter.collectAsState()

  LaunchedEffect(apiAlter) {
    if (apiAlter != null && apiAlter!!.isSuccess && avatarState == AvatarState.Loading && !apiAlter!!.ensureData.avatarUrl.isNullOrBlank()) {
      avatarState = AvatarState.Loaded
    }
  }

  LaunchedEffect(selectedImage?.value) {
    if (selectedImage?.value != null) {
      avatarState = AvatarState.Preparing
      val bytes = imageCropper.getCompressedImage()
      avatarState = AvatarState.Loading
      api.setAlterAvatar(model.id, bytes, "avatar.webp")
    }
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
              platformUtilities = platformUtilities,
              onCompressionStart = {
                avatarState = AvatarState.Preparing
              },
              onImageReady = { bytes ->
                avatarState = AvatarState.Loading
                api.setAlterAvatar(model.id, bytes, "avatar.webp")
              },
              onCanceled = {
                avatarState = AvatarState.Loaded
              },
              coroutineScope = coroutineScope
            )
          }
        } else {
          coroutineScope.launch {
            platformFileToImageSrc(file, platformUtilities)?.let {
              imageCropper!!.setSelectedImage(it)
            }
          }
        }
      }
    }
  )

  // val nameState = rememberTextFieldState(model.name.value.orEmpty())
  // val pronounsState = rememberTextFieldState(model.pronouns.value.orEmpty())
  // val descriptionState = rememberTextFieldState()
  // val aliasState = rememberTextFieldState()
  // val proxyNameState = rememberTextFieldState()

  /*UpdateTextFieldStatesOnLoad(
    model.isLoaded,
    descriptionState to model.description,
    aliasState to model.alias,
    proxyNameState to model.proxyName
  )*/

  CompositionLocalProvider(
    LocalFieldFocusRequester provides focusRequester,
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().imePadding(),
      state = lazyListState,
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      if(apiAlter == null) {
        item {
          IndeterminateProgressSpinner()
        }
        return@LazyColumn
      }

      item {
        Spacer(modifier = Modifier.size(0.dp))
      }
      item {
        Box(
          modifier = Modifier.padding(horizontal = GLOBAL_PADDING)
        ) {
          SpotlightTooltip(
            title = Res.string.tooltip_alter_avatars_title.compose,
            description = Res.string.tooltip_alter_avatars_desc.compose
          ) {
            Box(
              modifier = Modifier.sizeIn(
                maxWidth = 312.dp,
                maxHeight = 312.dp
              ).aspectRatio(1.0F).clip(squareifyShape(settings.cornerStyle) { RoundedCornerShape(96.dp) })
                .combinedClickable(
                  onClick = {
                    if(apiAlter!!.ensureData.avatarUrl.isNullOrBlank()) {
                      filePickerLauncher.launch()
                    } else {
                      avatarSheetOpen = true
                    }
                  },
                  // onLongClick = { avatarSheetOpen = true }
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
                      text = when(avatarState) {
                        AvatarState.Preparing -> Res.string.preparing_avatar.compose
                        else  -> Res.string.uploading_avatar.compose
                      },
                      modifier = Modifier.fillMaxWidth()
                    )
                  }
                }

                apiAlter!!.ensureData.avatarUrl.isNullOrBlank() -> {
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
                      asyncPainterResource(apiAlter!!.ensureData.avatarUrl!!) {
                        coroutineContext =
                          coroutineScope.coroutineContext + ioDispatcher
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
                      name ?: unnamed_alter
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
        TextField(
          value = name.orEmpty(),
          onValueChange = model::updateName,
          // state = nameState,
          // inputTransformation = ModelTransformation(model::updateName),
          placeholder = { Text(unnamed_alter) },
          label = { Text(Res.string.name.compose) },
          singleLine = true,
          // lineLimits = TextFieldLineLimits.SingleLine,
          modifier = Modifier.fillMaxWidth().padding(horizontal = GLOBAL_PADDING)
        )
      }

      item {
        OutlinedTextField(
          value = pronouns.orEmpty(),
          onValueChange = model::updatePronouns,
          // state = pronounsState,
          // inputTransformation = ModelTransformation(model::updatePronouns),
          placeholder = { Text(Res.string.no_pronouns.compose) },
          label = { Text(Res.string.pronouns.compose) },
          singleLine = true,
          // lineLimits = TextFieldLineLimits.SingleLine,
          modifier = Modifier.fillMaxWidth().padding(horizontal = GLOBAL_PADDING)
        )
      }

      item {
        MarkdownOutlinedTextField(
          value = description,
          onValueChange = model::updateDescription,
          label = Res.string.description.compose,
          placeholder = Res.string.no_description.compose
        )
        /*MarkdownOutlinedTextField(
          internalValue = description,
          state = descriptionState,
          update = model::updateDescription,
          label = Res.string.description.compose,
          placeholder = Res.string.no_description.compose
        )*/
      }

      item {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = GLOBAL_PADDING),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          if (showAlterIds) {
            OutlinedTextField(
              // state = rememberTextFieldState(model.id.toString()),
              value = model.id.toString(),
              onValueChange = {},
              modifier = Modifier.weight(1f),
              readOnly = true,
              singleLine = true,
              // lineLimits = TextFieldLineLimits.SingleLine,
              label = { Text(Res.string.id.compose) }
            )
          }
          OutlinedTextField(
            value = color ?: Res.string.no_color.compose,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            interactionSource = rememberCollectPressInteractionSource {
              colorDialogOpen = true
            },
            modifier = Modifier.weight(1f),
            suffix = if (color != null) {
              {
                Box(
                  modifier = Modifier
                    .size(16.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(Color(hexStringToARGBInt(color!!)))
                )
              }
            } else null,
            label = { Text(Res.string.color.compose) }
          )
        }
      }

      item {
        var expanded by state(false)

        ExposedDropdownMenuBox(
          expanded = expanded,
          onExpandedChange = { expanded = !expanded },
        ) {
          OutlinedTextField(
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
              .padding(horizontal = GLOBAL_PADDING),
            readOnly = true,
            value = securityLevel.displayName,
            onValueChange = {},
            label = { Text(Res.string.security_level.compose) },
            trailingIcon = {
              Icon(
                securityLevel.icon,
                contentDescription = null // securityLevel.description("alter")
              )
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
          )
          ExposedDropdownMenu(
            modifier = Modifier.padding(horizontal = GLOBAL_PADDING),
            expanded = expanded,
            onDismissRequest = { expanded = false },
          ) {
            allSecurityLevels.forEach { level ->
              DropdownMenuItem(
                text = { Text(level.displayName) },
                trailingIcon = {
                  Icon(
                    level.icon,
                    contentDescription = null, // level.description("alter"),
                    modifier = Modifier.offset(x = 4.dp)
                  )
                },
                onClick = {
                  model.updateSecurityLevel(level)
                  expanded = false
                },
                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
              )
            }
          }
        }
      }

      item {
        OutlinedCard(
          shape = MaterialTheme.shapes.extraSmall,
          // Use same outline color as text fields
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
          modifier = Modifier.fillMaxWidth().padding(horizontal = GLOBAL_PADDING)
        ) {
          Row(
            modifier = Modifier.fillMaxSize()
              .padding(horizontal = GLOBAL_PADDING, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              text = Res.string.untracked.compose,
              style = MaterialTheme.typography.labelLarge,
              modifier = Modifier.weight(1f)
            )

            Switch(
              checked = untracked,
              onCheckedChange = {
                model.updateUntracked(it)
                haptics.performHapticFeedback(
                  if (it) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff
                )
              }
            )
          }
        }
      }

      /*item {
        Divider(modifier = Modifier.padding(horizontal = 16.dp))
      }*/

      if (discordLinked) {
        item {
          Row(
            modifier = Modifier.fillMaxWidth()
              .padding(horizontal = GLOBAL_PADDING),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              Res.string.discord_info.compose,
              style = getSubsectionStyle(settings.fontSizeScalar)
            )
            Spacer(modifier = Modifier.size(8.dp))
            IconButton(
              onClick = {
                discordHelpDialogOpen = true
              }
            ) {
              Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = Res.string.what_does_this_mean.compose
              )
            }
          }
        }

        item {
          OutlinedTextField(
            // state = aliasState,
            value = alias.orEmpty(),
            onValueChange = model::updateAlias,
            // inputTransformation = ModelTransformation(model::updateAlias),
            placeholder = { Text(Res.string.no_alias.compose) },
            label = { Text(Res.string.alias.compose) },
            singleLine = true,
            // lineLimits = TextFieldLineLimits.SingleLine,
            modifier = Modifier.fillMaxWidth().padding(horizontal = GLOBAL_PADDING)
          )
        }

        item {
          OutlinedTextField(
            value = proxyName.orEmpty(),
            onValueChange = model::updateProxyName,
            // state = proxyNameState,
            // inputTransformation = ModelTransformation(model::updateProxyName),
            placeholder = { Text(Res.string.no_proxy_name.compose) },
            label = { Text(Res.string.proxy_name.compose) },
            singleLine = true,
            // lineLimits = TextFieldLineLimits.SingleLine,
            modifier = Modifier.fillMaxWidth().padding(horizontal = GLOBAL_PADDING)
          )
        }
      }

      item {
        SpotlightTooltip(
          title = Res.string.tags.compose,
          description = Res.string.tooltip_tags_desc.compose
        ) {
          Row(
            modifier = Modifier.fillMaxWidth()
              .padding(
                start = GLOBAL_PADDING, end = GLOBAL_PADDING, bottom =
                  if (alterTags.isEmpty()) 0.dp else 8.dp
              ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              Res.string.tags.compose,
              style = getSubsectionStyle(settings.fontSizeScalar)
            )
            Spacer(modifier = Modifier.size(8.dp))
            FilledTonalIconButton(
              onClick = {
                attachTagDialogOpen = true
              }
            ) {
              Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = Res.string.add_tag.compose
              )
            }
          }
        }

        FlowRow(
          modifier = Modifier.fillMaxWidth().padding(horizontal = GLOBAL_PADDING),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          alterTags.forEach {
            ThemeFromColor(
              color = it.color,
              colorMode = settings.colorMode,
              dynamicColorType = settings.dynamicColorType,
              colorContrastLevel = settings.colorContrastLevel,
              amoledMode = settings.amoledMode
            ) {
              InputChip(
                selected = true,
                label = { Text(it.name) },
                onClick = {
                  selectedTag = it
                  // apiViewModel.detachAlterFromTag(it.id, id)
                }
              )
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
      }
    }

    if (avatarSheetOpen) {
      AvatarContextSheet(
        onDismissRequest = { avatarSheetOpen = false },
        avatar = apiAlter!!.ensureData.avatarUrl,
        launchSetAvatar = { filePickerLauncher.launch() },
        launchRemoveAvatar = { api.removeAlterAvatar(model.id) }
      )
    }

    if (selectedTag != null) {
      AlterTagContextSheet(
        onDismissRequest = { selectedTag = null },
        selectedTag = selectedTag!!,
        launchOpenTag = {
          component.navigateToTagView(it)
        },
        launchDetachAlter = { tagID ->
          api.detachAlterFromTag(
            tagID,
            model.id
          )
        }
      )
    }

    if (colorDialogOpen) {
      UpdateColorDialog(
        initialColor = color,
        updateColor = model::updateColor,
        onDismissRequest = { colorDialogOpen = false },
        settings = settings
      )
    }

    if (attachTagDialogOpen) {
      AttachTagDialog(
        existingTags = alterTags.map { it.id },
        tags = allTags,
        folderPainter = folderPainter,
        onDismissRequest = { attachTagDialogOpen = false },
        launchAttachTag = { tagID ->
          api.attachAlterToTag(
            tagID,
            model.id
          )
        },
        attachText = Res.string.attach_tag.compose,
        noTagsText = Res.string.no_tags_to_attach.compose,
        settings = settings
      )
    }

    if (discordHelpDialogOpen) {
      DiscordHelpDialog(
        onDismissRequest = { discordHelpDialogOpen = false }
      )
    }

    if (cropState != null) {
      ImageCropperDialog(
        state = cropState,
        style = cropperStyle(
          shapes = listOf(RectCropShape),
          aspects = listOf(AspectRatio(1, 1))
        ),
        dialogShape = squareifyShape(settings.cornerStyle) { RoundedCornerShape(8.dp) },
        cropControls = { BareControls(it) }
      )
    }
  }

}

@Composable
private fun DiscordHelpDialog(
  onDismissRequest: () -> Unit
) {
  AlertDialog(
    icon = {
      Icon(
        Icons.Rounded.Info,
        contentDescription = null
      )
    },
    title = {
      Text(text = Res.string.discord_info.compose)
    },
    text = {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        item {
          Text(Res.string.discord_help_dialog_body.compose)
        }
      }
    },
    onDismissRequest = onDismissRequest,
    confirmButton = {
      TextButton(
        onClick = onDismissRequest
      ) {
        Text(Res.string.ok.compose)
      }
    }
  )
}


@Composable
fun AlterTagContextSheet(
  onDismissRequest: () -> Unit,
  selectedTag: MyTag,
  launchOpenTag: (String) -> Unit,
  launchDetachAlter: (String) -> Unit
) {
  OctoBottomSheet(
    onDismissRequest = onDismissRequest
  ) {
    BottomSheetListItem(
      imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
      title = Res.string.open_tag.compose
    ) {
      launchOpenTag(selectedTag.id)
      onDismissRequest()
    }
    BottomSheetListItem(
      imageVector = Icons.Rounded.Delete,
      iconTint = MaterialTheme.colorScheme.error,
      title = Res.string.remove_alter_from_tag.compose
    ) {
      launchDetachAlter(selectedTag.id)
      onDismissRequest()
    }
  }
}