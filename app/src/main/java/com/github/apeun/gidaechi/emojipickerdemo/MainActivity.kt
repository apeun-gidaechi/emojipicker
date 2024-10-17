package com.github.apeun.gidaechi.emojipickerdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.EmojiSupportMatch
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.apeun.gidaechi.emojipicker.EmojiPicker

class MainActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var isShowEmoji by remember { mutableStateOf(false) }
            var selectEmoji by remember { mutableStateOf("\uD83C\uDDEC") }
            if (!isShowEmoji) {
                Text(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            isShowEmoji = true
                        },
                    text = selectEmoji,
                    style = TextStyle(
                        fontSize = 40.sp,
                        platformStyle = PlatformTextStyle(
                            emojiSupportMatch = EmojiSupportMatch.None
                        ),
                        textAlign = TextAlign.Center,
                    ),
                )
            } else {
                EmojiPicker(
                    onClick = {
                        selectEmoji = it
                        isShowEmoji = false
                    }
                )
            }
        }
    }
}