package ru.mclord.classic.server.messages.stoc

import ru.mclord.classic.server.Helper.isUnsignedByte
import ru.mclord.classic.server.utils.MinecraftString
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

data class SToCServerIdentificationMessage (
    val protocolVersion: Int = 0x07, // unsigned byte
    val serverName: MinecraftString,
    val serverMotd: MinecraftString,
    val userType: Int = 0x00 // unsigned byte, 0x64 if player is op.
) : ServerToClientMessage {
    companion object {
        const val PACKET_ID = 0x00 // unsigned byte
        const val PACKET_LEN = 1 + 1 + MinecraftString.length * 2 + 1
    }

    init {
        if (!protocolVersion.isUnsignedByte ||
            !userType.isUnsignedByte
        ) throw IllegalArgumentException()
    }

    override val bytes: ByteArray by lazy {
        val out = ByteArrayOutputStream(PACKET_LEN)
        val dataOutputStream = DataOutputStream(out)

        dataOutputStream.writeByte(PACKET_ID)
        dataOutputStream.writeByte(protocolVersion)
        dataOutputStream.write(serverName.minecraftString)
        dataOutputStream.write(serverMotd.minecraftString)
        dataOutputStream.writeByte(userType)

        dataOutputStream.close()
        out.toByteArray()
    }
}