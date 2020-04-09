package ru.mclord.classic.server.messages.stoc

import ru.mclord.classic.server.Helper.isShort
import ru.mclord.classic.server.Helper.isUnsignedByte
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

data class SToCSetBlockMessage (
    val x: Int, // short
    val y: Int, // short
    val z: Int, // short
    val blockType: Int // unsigned byte
) : ServerToClientMessage {
    companion object {
        const val PACKET_ID = 0x06 // unsigned byte
        const val PACKET_LEN = 1 + 2 * 3 + 1
    }

    init {
        if (!x.isShort ||
            !y.isShort ||
            !z.isShort ||
            !blockType.isUnsignedByte
        ) throw IllegalArgumentException()
    }

    override val bytes: ByteArray by lazy {
        val out = ByteArrayOutputStream(PACKET_LEN)
        val dataOutputStream = DataOutputStream(out)

        dataOutputStream.writeByte(PACKET_ID)
        dataOutputStream.writeShort(x)
        dataOutputStream.writeShort(y)
        dataOutputStream.writeShort(z)
        dataOutputStream.writeByte(blockType)

        dataOutputStream.close()
        out.toByteArray()
    }
}