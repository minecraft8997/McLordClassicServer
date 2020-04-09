package ru.mclord.classic.server.messages.stoc

object SToCPingMessage : ServerToClientMessage {
    const val PACKET_ID = 0x01 // unsigned byte
    const val PACKET_LEN = 1
    override val bytes = byteArrayOf(PACKET_ID.toByte())
}