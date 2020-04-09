package ru.mclord.classic.server.messages.stoc

import ru.mclord.classic.server.Helper.isByte
import ru.mclord.classic.server.Helper.isUnsignedByte
import ru.mclord.classic.server.Helper.isShort
import ru.mclord.classic.server.utils.MinecraftString
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

data class SToCSpawnPlayerMessage (
    val playerID: Int, // byte
    val playerName: MinecraftString,
    val x: Int, // short
    val y: Int, // short
    val z: Int, // short
    val yaw: Int, // unsigned byte
    val pitch: Int // unsigned byte
): ServerToClientMessage {
    companion object {
        const val PACKET_ID = 0x07 // unsigned byte
        const val PACKET_LEN = 1 + 1 + MinecraftString.length + 2 * 3 + 1 + 1
    }

    init {
        if (!playerID.isByte ||
            !x.isShort ||
            !y.isShort ||
            !z.isShort ||
            !yaw.isUnsignedByte ||
            !pitch.isUnsignedByte
        ) throw IllegalArgumentException()
    }

    override val bytes: ByteArray by lazy {
        val out = ByteArrayOutputStream(PACKET_LEN)
        val dataOutputStream = DataOutputStream(out)

        dataOutputStream.writeByte(PACKET_ID)
        dataOutputStream.writeByte(playerID)
        dataOutputStream.write(playerName.minecraftString)
        dataOutputStream.writeShort(x)
        dataOutputStream.writeShort(y)
        dataOutputStream.writeShort(z)
        dataOutputStream.writeByte(yaw)
        dataOutputStream.writeByte(pitch)

        dataOutputStream.close()
        out.toByteArray()
    }
}