package com.github.apeun.gidaechi.emojipicker

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMapIndexed
import androidx.emoji2.text.EmojiCompat
import com.github.apeun.gidaechi.emojipicker.core.EmojiLoader
import com.github.apeun.gidaechi.emojipicker.core.model.EmojiModel
import com.github.apeun.gidaechi.emojipicker.core.model.Result
import com.github.apeun.gidaechi.emojipicker.core.utiles.isEmojiCharacterRenderable
import com.github.apeun.gidaechi.emojipicker.core.utiles.rememberTextWidth
import com.github.apeun.gidaechi.emojipicker.ui.EmojiUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.floor

@Composable
fun EmojiPicker(
    modifier: Modifier = Modifier
) {

    var emojiRemoteState: Result<ImmutableList<EmojiModel>> by remember { mutableStateOf(Result.Loading) }
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        EmojiLoader
            .getEmojiList()
            .catch {
                coroutineScope.launch {
                    emojiRemoteState = Result.Error(it)
                }
            }
            .collect {
                coroutineScope.launch {
                    emojiRemoteState = Result.Success(
                        it
                            .filter {
                                val value = isEmojiCharacterRenderable(
                                    emojiCharacter = it.character,
                                )

                                Log.d("TAG", "${EmojiCompat.get().loadState == EmojiCompat.LOAD_STATE_SUCCEEDED}EmojiPickerUi: ${it.character} $value")

                                value
                            }
                            .toImmutableList()
                    )
                }
            }
    }

    EmojiPickerUi(
        modifier = modifier,
        emojiRemoteState = emojiRemoteState
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmojiPickerUi(
    modifier: Modifier = Modifier,
    emojiRemoteState: Result<ImmutableList<EmojiModel>>
) {
    Scaffold(
        modifier = modifier
            .navigationBarsPadding(),
        bottomBar = {

        }
    ) {
        when (emojiRemoteState) {
            is Result.Success -> {
                val emojiGroups: ImmutableList<Pair<String, ImmutableList<EmojiModel>>> = remember(emojiRemoteState) {
                    emojiRemoteState.data
                        .groupBy { emoji ->
                            emoji.group
                        }
                        .filter { (_, emojis) ->
                            emojis.isNotEmpty()
                        }
                        .map {
                            Pair(it.key, it.value.toImmutableList())
                        }
                        .toImmutableList()
                }

                val emojiWidth = rememberTextWidth(
                    text = emojiGroups.firstOrNull()?.second?.firstOrNull()?.character?: "",
                    fontSize = defaultEmojiFontSize,
                )
                BoxWithConstraints {
                    if (emojiWidth == null) {
                        Text(text = "이모지가 없어요")
                    } else {
                        val (columnCount, itemPadding) = getColumnData(
                            maxColumnWidth = maxWidth,
                            emojiWidth = emojiWidth,
                        )
                        LazyColumn(
                            modifier = Modifier
                                .padding(it)
                        ) {
                            emojiGroups.mapIndexed { index, (group, emojis)  ->
                                item {
                                    Text(text = group)
                                }
                                emojis.chunked(
                                    size = columnCount
                                ).map {
                                    item {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            it.fastMap {
                                                EmojiUi(
                                                    emojiCharacter = it.character,
                                                    onClick = {}
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Result.Loading -> {
                Text("Loading")
            }
            is Result.Error -> {
                Text("Load Failed")
            }
        }

    }
}

fun getColumnData(
    maxColumnWidth: Dp,
    emojiWidth: Dp,
): Pair<Int, Dp> {
    val emojiWidthWithPadding = emojiWidth + (emojiWidth * 0.4f)
    val columnCount = (maxColumnWidth / (emojiWidthWithPadding)).toInt()
    val ceilEmojiWidth = ceil(emojiWidthWithPadding.value).dp
    val itemPadding = floor(
        x = max(
            a = 0.dp,
            b = (maxColumnWidth - (ceilEmojiWidth * columnCount)) / (2 * columnCount),
        ).value,
    ).dp
    return Pair(columnCount, itemPadding)
}

internal val defaultEmojiFontSize = 28.sp