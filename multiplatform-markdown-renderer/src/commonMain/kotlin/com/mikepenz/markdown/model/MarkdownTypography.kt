package com.mikepenz.markdown.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle

interface MarkdownTypography {
    val text: TextStyle
    val code: TextStyle
    val inlineCode: TextStyle
    val h1: TextStyle
    val h2: TextStyle
    val h3: TextStyle
    val h4: TextStyle
    val h5: TextStyle
    val h6: TextStyle
    val quote: TextStyle
    val paragraph: TextStyle
    val ordered: TextStyle
    val bullet: TextStyle
    val list: TextStyle

    @Deprecated("Use textLink instead", ReplaceWith("textLink"))
    val link: TextStyle
    val textLink: TextLinkStyles
}

@Immutable
class DefaultMarkdownTypography(
    override val h1: TextStyle,
    override val h2: TextStyle,
    override val h3: TextStyle,
    override val h4: TextStyle,
    override val h5: TextStyle,
    override val h6: TextStyle,
    override val text: TextStyle,
    override val code: TextStyle,
    override val inlineCode: TextStyle,
    override val quote: TextStyle,
    override val paragraph: TextStyle,
    override val ordered: TextStyle,
    override val bullet: TextStyle,
    override val list: TextStyle,
    @Deprecated("Use textLink instead", replaceWith = ReplaceWith("textLink"))
    override val link: TextStyle,
    override val textLink: TextLinkStyles,
) : MarkdownTypography