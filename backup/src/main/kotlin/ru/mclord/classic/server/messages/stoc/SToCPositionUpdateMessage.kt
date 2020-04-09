package ru.mclord.classic.server.messages.stoc

import ru.mclord.classic.server.Helper.isByte
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

data class SToCPositionUpdateMessage (
    val playerID: Int, // byte
    val changeInX: Int, // byte
    val changeInY: Int, // byte
    val changeInZ: Int // byte
): ServerToClientMessage {
    companion object {
        const val PACKET_ID = 0x0a // unsigned byte
        const val PACKET_LEN = 1 + 1 + 1 + 1 + 1
    }

    init {
        if (!playerID.isByte ||
            !changeInX.isByte ||
            !changeInY.isByte ||
            !changeInZ.isByte
        ) throw IllegalArgumentException()
    }

    override val bytes: ByteArray by lazy {
        val out = ByteArrayOutputStream(PACKET_LEN)
        val dataOutputStream = DataOutputStream(out)

        dataOutputStream.writeByte(PACKET_ID)
        dataOutputStream.writeByte(playerID)
        dataOutputStream.writeByte(changeInX)
        dataOutputStream.writeByte(changeInY)
        dataOutputStream.writeByte(changeInZ)

        dataOutputStream.close()
        out.toByteArray()
    }
}