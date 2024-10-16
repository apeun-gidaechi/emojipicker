package com.github.apeun.gidaechi.emojipicker.core

import com.github.apeun.gidaechi.emojipicker.core.network.EmojiRemoteDataSource
import com.github.apeun.gidaechi.emojipicker.core.network.service.EmojiRemoteService

object EmojiLoader {

    suspend fun getEmojiList(
        emojiRemoteDataSource: EmojiRemoteDataSource = EmojiRemoteService()
    ) {
        emojiRemoteDataSource.getEmojiAll()
    }

}