package ru.mclord.classic.server.messages.ctos

import ru.mclord.classic.server.Helper.isUnsignedByte
import ru.mclord.classic.server.utils.MinecraftString

data class CToSPlayerIdentificationMessage (
    val protocolVersion: Int, // unsigned byte, current protocol version is 0x07
    val userName: MinecraftString,
    val verificationKey: MinecraftString
    // there is one more unsigned byte which is unused (always equals 0x00)
): ClientToServerMessage {
    companion object {
        const val PACKET_ID = 0x00 // unsigned byte
        const val PACKET_LEN = 1 + 1 + MinecraftString.length * 2 + 1
    }

    init {
        if (!protocolVersion.isUnsignedByte)
            throw IllegalArgumentException()
    }
}