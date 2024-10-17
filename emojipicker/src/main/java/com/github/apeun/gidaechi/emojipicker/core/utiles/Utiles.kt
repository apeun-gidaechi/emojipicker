package com.github.apeun.gidaechi.emojipicker.core.utiles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.emoji2.text.EmojiCompat

internal fun isEmojiCharacterRenderable(
    emojiCharacter: String,
): Boolean {
    return EmojiCompat.isConfigured() &&
            EmojiCompat.get().loadState == EmojiCompat.LOAD_STATE_SUCCEEDED &&
            EmojiCompat.get().getEmojiMatch(
                emojiCharacter,
                Int.MAX_VALUE
            ) == EmojiCompat.EMOJI_SUPPORTED
}

@Composable
internal fun rememberTextWidth(
    text: String?,
    fontSize: TextUnit,
): Dp? {
    if (text == null) {
        return null
    }
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    return remember(
        text,
        fontSize,
    ) {
        with(density) {
            textMeasurer.measure(
                text = text,
                style = TextStyle(
                    fontSize = fontSize,
                ),
            ).size.width.toDp()
        }
    }
}

internal fun Density.toDp(sp: TextUnit): Dp =
    sp.toDp()
