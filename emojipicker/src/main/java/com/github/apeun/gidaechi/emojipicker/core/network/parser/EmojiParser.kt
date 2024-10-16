package com.github.apeun.gidaechi.emojipicker.core.network.parser

import com.github.apeun.gidaechi.emojipicker.core.network.parser.EmojiParserImplConstants.GROUP_LINE_PREFIX
import com.github.apeun.gidaechi.emojipicker.core.network.parser.EmojiParserImplConstants.REQUIRED_EMOJI_STATUS
import com.github.apeun.gidaechi.emojipicker.core.network.parser.EmojiParserImplConstants.SUB_GROUP_LINE_PREFIX
import com.github.apeun.gidaechi.emojipicker.core.network.parser.EmojiParserImplConstants.skinTonesCodePoints
import com.github.apeun.gidaechi.emojipicker.core.network.response.EmojiResponse
import java.util.stream.Collectors

private object EmojiParserImplConstants {
    const val COMMENT_CHAR = '#'
    const val SUB_GROUP_LINE_PREFIX = "# subgroup: "
    const val GROUP_LINE_PREFIX = "# group: "
    const val LIGHT_SKIN_TONE = "1F3FB"
    const val MEDIUM_LIGHT_SKIN_TONE = "1F3FC"
    const val MEDIUM_SKIN_TONE = "1F3FD"
    const val MEDIUM_DARK_SKIN_TONE = "1F3FE"
    const val DARK_SKIN_TONE = "1F3FF"
    const val REQUIRED_EMOJI_STATUS = "fully-qualified"
    val skinTonesCodePoints = setOf(
        LIGHT_SKIN_TONE,
        MEDIUM_LIGHT_SKIN_TONE,
        MEDIUM_SKIN_TONE,
        MEDIUM_DARK_SKIN_TONE,
        DARK_SKIN_TONE,
    )
}

internal object EmojiParser {
    fun parseEmojiData(
        data: String,
        isSkinTonesSupported: Boolean,
    ): List<EmojiResponse> {
        val emojis = mutableListOf<EmojiResponse>()
        var currentGroup = ""
        var currentSubgroup = ""

        data
            .trim()
            .split("\n")
            .filter {
                it.isNotBlank()
            }.forEach { line ->
                // Update current group and subgroup info from comments and ignore other comments
                if (line[0] == EmojiParserImplConstants.COMMENT_CHAR) {
                    if (line.contains(SUB_GROUP_LINE_PREFIX)) {
                        currentSubgroup = line.replace(SUB_GROUP_LINE_PREFIX, "")
                    } else if (line.contains(GROUP_LINE_PREFIX)) {
                        currentGroup = line.replace(GROUP_LINE_PREFIX, "")
                    }
                } else {
                    /**
                     * Sample line to parse
                     * 1F635 200D 1F4AB                ; fully-qualified     # 😵‍💫 E13.1 face with spiral eyes
                     */
                    val (codePoint, statusAndName) = line
                        .trim()
                        .split(";")
                        .map {
                            it.trim()
                        }
                    val (status, characterAndName) = statusAndName
                        .trim()
                        .split("#")
                        .map {
                            it.trim()
                        }
                    if (status == REQUIRED_EMOJI_STATUS) {
                        val characterAndNameSplits = characterAndName
                            .trim()
                            .split(" ")
                        val codePointSplit = codePoint.split(" ")
                        val emojiHasSkinTone = codePointSplit.any {
                            skinTonesCodePoints.contains(it)
                        }
                        if (isSkinTonesSupported || !emojiHasSkinTone) {
                            emojis.add(
                                element = EmojiResponse(
                                    character = characterAndNameSplits[0],
                                    codePoint = codePoint,
                                    group = currentGroup,
                                    subgroup = currentSubgroup,
                                    unicodeName = characterAndNameSplits
                                        .stream()
                                        .skip(1)
                                        .collect(Collectors.joining(" ")),
                                ),
                            )
                        }
                    }
                }
            }
        return emojis
    }
}
