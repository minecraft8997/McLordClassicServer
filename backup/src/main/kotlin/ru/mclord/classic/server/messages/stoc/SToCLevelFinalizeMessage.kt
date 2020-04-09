package ru.mclord.classic.server.messages.stoc

import ru.mclord.classic.server.Helper.isShort
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

data class SToCLevelFinalizeMessage (
    val xSize: Int, // short
    val ySize: Int, // short
    val zSize: Int // short
) : ServerToClientMessage {
    companion object {
        const val PACKET_ID = 0x04 // unsigned byte
        const val PACKET_LEN = 1 + 2 * 3
    }

    init {
        if (!xSize.isShort ||
            !ySize.isShort ||
            !zSize.isShort
        ) throw IllegalArgumentException()
    }

    override val bytes: ByteArray by lazy {
        val out = ByteArrayOutputStream(PACKET_LEN)
        val dataOutputStream = DataOutputStream(out)

        dataOutputStream.writeByte(PACKET_ID)
        dataOutputStream.writeShort(xSize)
        dataOutputStream.writeShort(ySize)
        dataOutputStream.writeShort(zSize)

        dataOutputStream.close()
        out.toByteArray()
    }
}