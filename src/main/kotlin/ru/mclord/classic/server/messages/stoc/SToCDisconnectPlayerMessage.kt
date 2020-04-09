package ru.mclord.classic.server.messages.stoc

import ru.mclord.classic.server.utils.MinecraftString
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

data class SToCDisconnectPlayerMessage (
    val disconnectReason: MinecraftString
) : ServerToClientMessage {
    companion object {
        const val PACKET_ID = 0x0e // unsigned byte
        const val PACKET_LEN = 1 + MinecraftString.length
    }

    override val bytes: ByteArray by lazy {
        val out = ByteArrayOutputStream(PACKET_LEN)
        val dataOutputStream = DataOutputStream(out)

        dataOutputStream.writeByte(PACKET_ID)
        dataOutputStream.write(disconnectReason.minecraftString)

        dataOutputStream.close()
        out.toByteArray()
    }
}