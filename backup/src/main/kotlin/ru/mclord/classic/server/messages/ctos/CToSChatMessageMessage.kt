package ru.mclord.classic.server.messages.ctos

import ru.mclord.classic.server.utils.MinecraftString

data class CToSChatMessageMessage (
    // there is one more byte which is unused, perhaps it is playerID, because
    // it always equals -1 (255 unsigned)
    val message: MinecraftString
): ClientToServerMessage {
    companion object {
        const val PACKET_ID = 0x0d // unsigned byte
        const val PACKET_LEN = 1 + 1 + MinecraftString.length
    }
}