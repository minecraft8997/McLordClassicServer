package ru.mclord.classic.server.logic

import ru.mclord.classic.server.Helper.coordinatesAreValid
import ru.mclord.classic.server.ServerHandler
import ru.mclord.classic.server.messages.stoc.SToCLevelDataChunkMessage
import ru.mclord.classic.server.messages.stoc.SToCLevelFinalizeMessage
import ru.mclord.classic.server.messages.stoc.SToCLevelInitializeMessage
import ru.mclord.classic.server.messages.stoc.SToCSetBlockMessage
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.util.zip.GZIPOutputStream

class World {
    companion object {
        const val X_SIZE = 256
        const val Y_SIZE = 256
        const val Z_SIZE = 256
    }

    @Volatile var generated = false
        private set
    private val blocks = ByteArray(X_SIZE * Y_SIZE * Z_SIZE) { Block.AIR.id.toByte() }

    @Synchronized
    fun generate() {
        if (generated) return

        for (x in 0 until X_SIZE) {
            for (y in 0 until Y_SIZE) {
                for (z in 0 until Z_SIZE) {
                    if (y == 0) setBlockAt(x, y, z, Block.GRASS, false)
                }
            }
        }

        generated = true
    }

    fun sendTo (player: Player) {
        val bytes = ByteArrayOutputStream()
        val gzipOutputStream = GZIPOutputStream(bytes)
        val dataOut = DataOutputStream(gzipOutputStream)

        dataOut.writeInt(blocks.size)
        dataOut.write(blocks)

        dataOut.close()
        gzipOutputStream.close()

        val compressedMap = bytes.toByteArray()

        player.connectedChannel.writeAndFlush(SToCLevelInitializeMessage)
        for (part in compressedMap.indices step 1024) {
            val chunk = ByteArray(1024)

            val length =
                if (compressedMap.size - part < 1024) (compressedMap.size - part)
                else 1024

            System.arraycopy(compressedMap, part, chunk, 0, length)

            player.connectedChannel.writeAndFlush(
                SToCLevelDataChunkMessage(
                    length,
                    chunk,
                    ((part + length).toDouble() / compressedMap.size.toDouble() * 100).toInt()
                )
            )
        }

        player.connectedChannel.writeAndFlush(
            SToCLevelFinalizeMessage(X_SIZE, Y_SIZE, Z_SIZE)
        )
    }

    fun setBlockAt (x: Int, y: Int, z: Int, block: Block, notify: Boolean = true) {
        if (!coordinatesAreValid(x, y, z))
            throw IllegalArgumentException()

        blocks[(y * Z_SIZE + z) * X_SIZE + x] = block.id.toByte()
        if (notify) {
            for (player in ServerHandler.activePlayers) {
                player.connectedChannel.writeAndFlush(
                    SToCSetBlockMessage(x, y, z, block.id)
                )
            }
        }
    }

    fun getBlockIDAt (x: Int, y: Int, z: Int): Int {
        if (!coordinatesAreValid(x, y, z))
            throw IllegalArgumentException()

        return blocks[(y * Z_SIZE + z) * X_SIZE + x].toInt() and 0xFF
    }
}