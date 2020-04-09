package ru.mclord.classic.server.messages.stoc

import ru.mclord.classic.server.Helper.isByte
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

data class SToCDespawnPlayerMessage (
    val playerID: Int // byte
) : ServerToClientMessage {
    companion object {
        const val PACKET_ID = 0x0c // unsigned byte
        const val PACKET_LEN = 1 + 1
    }

    init {
        if (!playerID.isByte) throw IllegalArgumentException()
    }

    override val bytes: ByteArray by lazy {
        val out = ByteArrayOutputStream(PACKET_LEN)
        val dataOutputStream = DataOutputStream(out)

        dataOutputStream.writeByte(PACKET_ID)
        dataOutputStream.writeByte(playerID)

        dataOutputStream.close()
        out.toByteArray()
    }
}