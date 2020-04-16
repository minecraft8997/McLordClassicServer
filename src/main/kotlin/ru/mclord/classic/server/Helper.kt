@file:JvmName("Helper")

package ru.mclord.classic.server

import com.google.common.base.Splitter
import io.netty.channel.Channel
import ru.mclord.classic.server.ServerHandler.channelsNeededToConfirm
import ru.mclord.classic.server.logic.Player
import ru.mclord.classic.server.logic.World
import ru.mclord.classic.server.messages.stoc.SToCChatMessageMessage
import ru.mclord.classic.server.messages.stoc.SToCDisconnectPlayerMessage
import ru.mclord.classic.server.utils.ChannelNeededToConfirm
import ru.mclord.classic.server.utils.MinecraftString
import kotlin.math.sqrt

object Helper {
    const val minecraftChunkSize = 1024
    const val MAX_ONLINE_PLAYERS = 250
    const val CHANNEL_CONFIRM_TIMEOUT_MS = 1000

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

    @Volatile
    var currentWorld = World()
        private set

    init {
        currentWorld.generate()
    }

    fun sendChatMessage (player: Player, message: String) {
        Splitter
            .fixedLength(MinecraftString.length)
            .split(message)
            .forEach { piece ->
                run {
                    player.connectedChannel.writeAndFlush(
                        SToCChatMessageMessage(
                            player.id.toInt(), MinecraftString(piece)
                        )
                    )
                }
            }
    }

    fun coordinatesAreValid(x: Int, y: Int, z: Int) =
        (x.isShort && y.isShort && z.isShort) &&
                (x in 0 until World.X_SIZE && y in 0 until World.Y_SIZE && z in 0 until World.Z_SIZE)

    object PingSender : Thread() {

    }

    object UnconfirmedChannelsKicker : Thread() {
        override fun run() {
            var reportedInterruptedException = false

            while (true) {
                val currentTime = System.currentTimeMillis()

                try {
                    for (unconfirmedChannel in channelsNeededToConfirm) {
                        if (currentTime >= unconfirmedChannel.timeout) {
                            disconnect(unconfirmedChannel.channel, "Timed out")
                            channelsNeededToConfirm -= unconfirmedChannel
                        }
                    }

                    sleep(2)
                    if (reportedInterruptedException)
                        reportedInterruptedException = false
                } catch (t: Throwable) {
                    if (t is InterruptedException) {
                        println("InterruptedException occurred!")
                        println(t.message)
                        t.printStackTrace()

                        reportedInterruptedException = true
                    } else {
                        println("An exception/error occurred!")
                        println(t.message)
                        t.printStackTrace()
                    }
                }
            }
        }
    }

    fun disconnect(c: Channel, reason: String) {
        if (reason.length > MinecraftString.length)
            throw IllegalArgumentException()

        c.writeAndFlush(
            SToCDisconnectPlayerMessage(
                MinecraftString(reason)
            )
        )

        c.close()
    }

    fun getDistance(x1: Int, y1: Int, z1: Int, x2: Int, y2: Int, z2: Int): Double {
        val dx = x1 - x2
        val dy = y1 - y2
        val dz = z1 - z2

        return sqrt((dx * dx + dy * dy + dz * dz).toDouble())
    }
}