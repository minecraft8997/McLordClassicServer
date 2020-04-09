package ru.mclord.classic.server.messages.stoc

import ru.mclord.classic.server.Helper.isByte
import ru.mclord.classic.server.utils.MinecraftString
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

data class SToCChatMessageMessage (
    val playerID: Int = -1, // byte
    val message: MinecraftString
): ServerToClientMessage {
    companion object {
        const val PACKET_ID = 0x0d // unsigned byte
        const val PACKET_LEN = 1 + 1 + MinecraftString.length
    }

    init {
        if (!playerID.isByte) throw IllegalArgumentException()
    }

    override val bytes: ByteArray by lazy {
        val out = ByteArrayOutputStream(PACKET_LEN)
        val dataOutputStream = DataOutputStream(out)

        dataOutputStream.writeByte(PACKET_ID)
        dataOutputStream.writeByte(playerID)
        dataOutputStream.write(message.minecraftString)

        dataOutputStream.close()
        out.toByteArray()
    }
}