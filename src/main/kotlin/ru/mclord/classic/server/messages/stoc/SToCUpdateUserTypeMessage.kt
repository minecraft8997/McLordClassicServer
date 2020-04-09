package ru.mclord.classic.server.messages.stoc

import ru.mclord.classic.server.Helper.isUnsignedByte
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

data class SToCUpdateUserTypeMessage (
    val userType: Int // unsigned byte
) : ServerToClientMessage {
    companion object {
        const val PACKET_ID = 0x0f // unsigned byte
        const val PACKET_LEN = 1 + 1
    }

    init {
        if (!userType.isUnsignedByte) throw IllegalArgumentException()
    }

    override val bytes: ByteArray by lazy {
        val out = ByteArrayOutputStream(PACKET_LEN)
        val dataOutputStream = DataOutputStream(out)

        dataOutputStream.writeByte(PACKET_ID)
        dataOutputStream.writeByte(userType)

        dataOutputStream.close()
        out.toByteArray()
    }
}