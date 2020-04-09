package ru.mclord.classic.server.messages.stoc

import ru.mclord.classic.server.messages.Message

interface ServerToClientMessage : Message {
    val bytes: ByteArray
}