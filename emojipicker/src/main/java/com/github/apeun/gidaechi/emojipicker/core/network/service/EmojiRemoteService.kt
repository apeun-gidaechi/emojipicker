package com.github.apeun.gidaechi.emojipicker.core.network.service

import com.github.apeun.gidaechi.emojipicker.core.network.EmojiRemoteDataSource
import com.github.apeun.gidaechi.emojipicker.core.network.parser.EmojiParser
import com.github.apeun.gidaechi.emojipicker.core.network.response.EmojiResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal class EmojiRemoteService: EmojiRemoteDataSource {

    companion object {
        const val EMOJI_URL = "https://makeappssimple.com/hosting/emoji_core/emoji.txt"
    }

    override suspend fun getEmojiAll(): Flow<List<EmojiResponse>> = flow {
        val okHttpClientBuilder = OkHttpClient()
            .newBuilder()

        val okHttpClient = okHttpClientBuilder.build()

        val response = okHttpClient.get(url = EMOJI_URL)

        emit(
            EmojiParser.parseEmojiData(
                data = response.body?.string().orEmpty(),
                isSkinTonesSupported = false
            )
        )
    }


    private suspend fun OkHttpClient.get(
        url: String,
    ): Response {
        val request = Request.Builder()
            .cacheControl(
                cacheControl = CacheControl.Builder()
                    .minFresh(
                        minFresh = 3,
                        timeUnit = TimeUnit.DAYS,
                    )
                    .maxStale(
                        maxStale = 30,
                        timeUnit = TimeUnit.DAYS,
                    )
                    .build(),
            )
            .url(
                url = url,
            )
            .build()
        return suspendCoroutine<Response> { continuation ->
            this
                .newCall(
                    request = request,
                ).enqueue(
                    responseCallback = object : Callback {
                        override fun onFailure(
                            call: Call,
                            e: IOException,
                        ) {
                            continuation.resumeWithException(e)
                        }

                        override fun onResponse(
                            call: Call,
                            response: Response,
                        ) {
                            continuation.resume(response)
                        }
                    },
                )
        }
    }
}