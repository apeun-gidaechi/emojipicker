package com.github.apeun.gidaechi.emojipicker

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMap
import androidx.emoji2.text.EmojiCompat
import com.github.apeun.gidaechi.emojipicker.core.EmojiLoader
import com.github.apeun.gidaechi.emojipicker.core.model.EmojiModel
import com.github.apeun.gidaechi.emojipicker.core.model.Result
import com.github.apeun.gidaechi.emojipicker.core.utiles.isEmojiCharacterRenderable
import com.github.apeun.gidaechi.emojipicker.core.utiles.rememberTextWidth
import com.github.apeun.gidaechi.emojipicker.core.utiles.toDp
import com.github.apeun.gidaechi.emojipicker.ui.EmojiUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.floor

@Composable
fun EmojiPicker(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFFF2F2F7),
    onClick: (emoji: String) -> Unit,
    onLongClick: ((emoji: String) -> Unit)? = null,
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
                                isEmojiCharacterRenderable(
                                    emojiCharacter = it.character,
                                )
                            }
                            .toImmutableList()
                    )
                }
            }
    }

    EmojiPickerUi(
        modifier = modifier,
        emojiRemoteState = emojiRemoteState,
        backgroundColor = backgroundColor,
        onClick = onClick,
        onLongClick = onLongClick,
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
private fun EmojiPickerUi(
    modifier: Modifier = Modifier,
    emojiRemoteState: Result<ImmutableList<EmojiModel>>,
    backgroundColor: Color,
    onClick: (emoji: String) -> Unit,
    onLongClick: ((emoji: String) -> Unit)?,
) {

    val lazyListState = rememberLazyListState()
    val density = LocalDensity.current
    val emojiList: ImmutableList<Int> = persistentListOf(
        R.drawable.ic_smile_eye,
        R.drawable.ic_person,
        R.drawable.ic_dog_face,
        R.drawable.ic_hamburger,
        R.drawable.ic_hotel,
        R.drawable.ic_soccer_ball,
        R.drawable.ic_light_bulb,
        R.drawable.ic_black_heart,
        R.drawable.ic_flag
    )

    val coroutineScope = rememberCoroutineScope()
    var categoryIndexList: ImmutableList<Int> by remember { mutableStateOf(persistentListOf()) }
    val firstVisibleIndex by remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }

    Scaffold(
        modifier = modifier
            .navigationBarsPadding(),
        containerColor = backgroundColor,
        bottomBar = {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            color = Color.Gray
                        )
                )
                Row {
                    emojiList.fastForEachIndexed { index, iconId ->

                        val nowItem = categoryIndexList.getOrNull(index) ?: 0
                        val nextItem = categoryIndexList.getOrNull(index+1) ?: Int.MAX_VALUE

                        Icon(
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .weight(1f)
                                .height(density.toDp(defaultEmojiFontSize))
                                .clickable {
                                    if (categoryIndexList.size == 0) {
                                        return@clickable
                                    }
                                    coroutineScope.launch {
                                        lazyListState.animateScrollToItem(nowItem)
                                    }
                                },
                            painter = painterResource(id = iconId),
                            contentDescription = null,
                            tint = if (
                                categoryIndexList.size > 0 &&
                                nowItem <= firstVisibleIndex &&
                                firstVisibleIndex < nextItem
                            ) Color(0xFF3478F6) else Color.Gray
                        )
                    }
                }
            }
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
                        Text(
                            text = "이모지가 없어요",
                            color = Color(0xFF8E8E92)
                        )
                    } else {
                        val (columnCount, itemPadding) = getColumnData(
                            maxColumnWidth = maxWidth - 12.dp,
                            emojiWidth = emojiWidth,
                        )

                        LaunchedEffect(emojiGroups) {
                            // 각 그룹의 첫 번째 이모지 인덱스를 계산
                            var currentIndex = 0
                            val newHeaderKey = emojiGroups.mapIndexed { index, (_, emojis) ->
                                val headerIndex = currentIndex
                                currentIndex += emojis.size / columnCount + // 다음 그룹의 시작 인덱스를 위해 누적
                                        if (emojis.size % columnCount != 0) 1 else 0//
                                currentIndex++
                                headerIndex
                            }.toImmutableList()

                            categoryIndexList = newHeaderKey
                        }

                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier
                                .padding(it),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            emojiGroups.mapIndexed { index, (group, emojis)  ->
                                stickyHeader {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF2F2F7))
                                    ) {
                                        Text(
                                            modifier = Modifier
                                                .padding(vertical = 8.dp)
                                                .padding(start = 6.dp),
                                            text = group,
                                            color = Color(0xFF8E8E92)
                                        )
                                    }
                                }
                                emojis.chunked(
                                    size = columnCount
                                ).map { chunk ->
                                    item(key = chunk) {
                                        Row(
                                            modifier = Modifier
                                                .padding(
                                                    horizontal = 6.dp
                                                )
                                                .fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            chunk.fastMap {
                                                EmojiUi(
                                                    emojiCharacter = it.character,
                                                    onClick = {
                                                        onClick(it.character)
                                                    },
                                                    onLongClick = {
                                                        if (onLongClick != null) {
                                                            onLongClick(it.character)
                                                        }
                                                    }
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