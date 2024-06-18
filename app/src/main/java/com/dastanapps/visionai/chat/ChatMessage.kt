package com.dastanapps.visionai.chat

import java.util.UUID

enum class Participant {
    USER, MODEL, FUNCTION, ERROR
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    var text: String = "",
    val participant: Participant = Participant.USER,
    var isPending: Boolean = false
)
