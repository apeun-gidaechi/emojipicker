package com.github.apeun.gidaechi.emojipicker.core.network

import com.github.apeun.gidaechi.emojipicker.core.network.response.EmojiResponse
import kotlinx.coroutines.flow.Flow

interface EmojiRemoteDataSource {

    suspend fun getEmojiAll(): Flow<List<EmojiResponse>>
}