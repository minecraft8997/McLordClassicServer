package ru.mclord.classic.server.messages.stoc

import ru.mclord.classic.server.Helper.isByte
import ru.mclord.classic.server.Helper.isUnsignedByte
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

data class SToCPositionAndOrientationUpdateMessage (
    val playerID: Int, // byte
    val changeInX: Int, // byte
    val changeInY: Int, // byte
    val changeInZ: Int, // byte
    val yaw: Int, // unsigned byte
    val pitch: Int // unsigned byte
): ServerToClientMessage {
    companion object {
        const val PACKET_ID = 0x09 // unsigned byte
        const val PACKET_LEN = 1 * 7
    }

    init {
        if (!playerID.isByte ||
            !changeInX.isByte ||
            !changeInY.isByte ||
            !changeInZ.isByte ||
            !yaw.isUnsignedByte ||
            !pitch.isUnsignedByte
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
        dataOutputStream.writeByte(yaw)
        dataOutputStream.writeByte(pitch)

        dataOutputStream.close()
        out.toByteArray()
    }
}