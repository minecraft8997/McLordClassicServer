package ru.mclord.classic.server.messages.stoc

import ru.mclord.classic.server.Helper
import ru.mclord.classic.server.Helper.isUnsignedByte
import ru.mclord.classic.server.Helper.isShort
import ru.mclord.classic.server.Helper.isValidMinecraftChunk
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

data class SToCLevelDataChunkMessage (
    val chunkLength: Int, // short
    val chunkData: ByteArray,
    val percentComplete: Int // unsigned byte
): ServerToClientMessage {
    companion object {
        const val PACKET_ID = 0x03 // unsigned byte
        const val PACKET_LEN = 1 + 2 + Helper.minecraftChunkSize + 1
    }

    init {
        if (!chunkLength.isShort ||
            !chunkData.isValidMinecraftChunk ||
            !percentComplete.isUnsignedByte
        ) throw IllegalArgumentException()
    }

    override val bytes: ByteArray by lazy {
        val out = ByteArrayOutputStream(PACKET_LEN)
        val dataOutputStream = DataOutputStream(out)

        dataOutputStream.writeByte(PACKET_ID)
        dataOutputStream.writeShort(chunkLength)
        dataOutputStream.write(chunkData)
        dataOutputStream.writeByte(percentComplete)

        dataOutputStream.close()
        out.toByteArray()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SToCLevelDataChunkMessage

        if (chunkLength != other.chunkLength) return false
        if (!chunkData.contentEquals(other.chunkData)) return false
        if (percentComplete != other.percentComplete) return false

        return true
    }

    override fun hashCode(): Int {
        var result = chunkLength
        result = 31 * result + chunkData.contentHashCode()
        result = 31 * result + percentComplete
        return result
    }
}