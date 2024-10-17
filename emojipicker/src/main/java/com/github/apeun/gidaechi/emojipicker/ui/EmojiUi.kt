package com.github.apeun.gidaechi.emojipicker.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.EmojiSupportMatch
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.github.apeun.gidaechi.emojipicker.defaultEmojiFontSize

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun EmojiUi(
    modifier: Modifier = Modifier,
    emojiCharacter: String,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emojiCharacter,
            style = TextStyle(
                fontSize = defaultEmojiFontSize,
                platformStyle = PlatformTextStyle(
                    emojiSupportMatch = EmojiSupportMatch.Default
                ),
                textAlign = TextAlign.Center,
            ),
        )
    }
}