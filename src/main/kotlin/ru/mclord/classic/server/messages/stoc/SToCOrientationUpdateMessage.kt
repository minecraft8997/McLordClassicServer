package ru.mclord.classic.server.messages.stoc

import ru.mclord.classic.server.Helper.isByte
import ru.mclord.classic.server.Helper.isUnsignedByte
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

data class SToCOrientationUpdateMessage (
    val playerID: Int, // byte
    val yaw: Int, // unsigned byte
    val pitch: Int // unsigned byte
) : ServerToClientMessage {
    companion object {
        const val PACKET_ID = 0x0b // unsigned byte
        const val PACKET_LEN = 1 + 1 + 1 + 1
    }

    init {
        if (!playerID.isByte ||
            !yaw.isUnsignedByte ||
            !pitch.isUnsignedByte
        ) throw IllegalArgumentException()
    }

    override val bytes: ByteArray by lazy {
        val out = ByteArrayOutputStream(PACKET_LEN)
        val dataOutputStream = DataOutputStream(out)

        dataOutputStream.writeByte(PACKET_ID)
        dataOutputStream.writeByte(playerID)
        dataOutputStream.writeByte(yaw)
        dataOutputStream.writeByte(pitch)

        dataOutputStream.close()
        out.toByteArray()
    }
}