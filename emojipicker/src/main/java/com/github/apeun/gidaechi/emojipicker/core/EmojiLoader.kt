package com.github.apeun.gidaechi.emojipicker.core

import com.github.apeun.gidaechi.emojipicker.core.model.EmojiModel
import com.github.apeun.gidaechi.emojipicker.core.network.EmojiRemoteDataSource
import com.github.apeun.gidaechi.emojipicker.core.network.service.EmojiRemoteService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object EmojiLoader {

    suspend fun getEmojiList(
        emojiRemoteDataSource: EmojiRemoteDataSource = EmojiRemoteService()
    ): Flow<List<EmojiModel>> = emojiRemoteDataSource.getEmojiAll()
        .map {
            it.map { emoji ->
                EmojiModel(
                    character = emoji.character,
                    codePoint = emoji.codePoint,
                    group = emoji.group,
                    subgroup = emoji.subgroup,
                    unicodeName = emoji.unicodeName
                )
            }
        }
}