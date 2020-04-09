package ru.mclord.classic.server

import io.netty.channel.Channel
import ru.mclord.classic.server.messages.stoc.SToCDisconnectPlayerMessage
import ru.mclord.classic.server.utils.MinecraftString

object Helper {
    const val minecraftChunkSize = 1024
    const val MAX_ONLINE_PLAYERS = 250
    const val CHANNEL_CONFIRM_TIMEOUT_MS = 100

    val tooManyPeopleArePlayingRightNowError =
"""        
Too many connections. Try again.
""".trimIndent()

    val Int.isByte
        get() = this in Byte.MIN_VALUE..Byte.MAX_VALUE
    val Int.isUnsignedByte
        get() = this in 0..255
    val Int.isShort
        get() = this in Short.MIN_VALUE..Short.MAX_VALUE
    val ByteArray.isValidMinecraftChunk
        get() = this.size == minecraftChunkSize //TODO

    object UnconfirmedChannelsKicker : Thread() {
        override fun run() {
            while (true) {
                //TODO
            }
        }
    }

    fun disconnect (c: Channel, reason: String) {
        if (reason.length > MinecraftString.length)
            throw IllegalArgumentException()

        Thread {
            c.writeAndFlush(
                SToCDisconnectPlayerMessage(
                    MinecraftString(reason)
                )
            ).await(100)
            c.close()
        }.start()
    }
}