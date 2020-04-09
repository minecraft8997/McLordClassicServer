package ru.mclord.classic.server.messages.ctos

import ru.mclord.classic.server.Helper.isUnsignedByte

data class CToSSetBlockMessage (
    val x: Short,
    val y: Short,
    val z: Short,
    val mode: Int, // unsigned byte
    val blockType: Int // unsigned byte
): ClientToServerMessage {
    companion object {
        const val PACKET_ID = 0x05 // unsigned byte
        const val PACKET_LEN = 1 + 2 * 3 + 1 + 1
    }

    init {
        if (!mode.isUnsignedByte ||
            !blockType.isUnsignedByte
        ) throw IllegalArgumentException()
    }
}