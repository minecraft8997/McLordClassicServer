package ru.mclord.classic.server

import io.netty.channel.Channel
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.group.DefaultChannelGroup
import io.netty.util.concurrent.GlobalEventExecutor
import ru.mclord.classic.server.Helper.MAX_ONLINE_PLAYERS
import ru.mclord.classic.server.Helper.disconnect
import ru.mclord.classic.server.exceptions.AuthException
import ru.mclord.classic.server.exceptions.ServerError
import ru.mclord.classic.server.logic.Player
import ru.mclord.classic.server.messages.ctos.CToSChatMessageMessage
import ru.mclord.classic.server.messages.ctos.CToSPlayerIdentificationMessage
import ru.mclord.classic.server.messages.ctos.CToSPositionAndOrientationMessage
import ru.mclord.classic.server.messages.ctos.ClientToServerMessage
import ru.mclord.classic.server.messages.stoc.*
import ru.mclord.classic.server.utils.ChannelNeededToConfirm
import ru.mclord.classic.server.utils.MinecraftString
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

@ChannelHandler.Sharable
object ServerHandler : SimpleChannelInboundHandler<ClientToServerMessage>() {
    private val activeChannels = DefaultChannelGroup(GlobalEventExecutor.INSTANCE)
    val channelsNeededToConfirm = Collections.synchronizedSet(
        HashSet<ChannelNeededToConfirm>()
    )
    private val activePlayers = Collections.synchronizedList(ArrayList<Player>())
    private val availableIDs = Collections.synchronizedSet(HashSet<Byte>(256))

    init {
        for (i in Byte.MIN_VALUE..Byte.MAX_VALUE) availableIDs += i.toByte()
        if (availableIDs.size != 256) throw IllegalStateException("availableIDs.size != 256")
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        println("new connection!")

        val closed = closeConnectionIfServerIsNotAbleToAcceptNewPlayers(ctx)
        if (!closed) {
            val channel: Channel = ctx.channel()
            activeChannels += channel
            channelsNeededToConfirm += ChannelNeededToConfirm(channel)
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        val player = getPlayerByChannel(ctx.channel())
        if (player != null) {
            activePlayers -= player
            availableIDs += player.id
        }

        activeChannels -= ctx.channel()
        super.channelInactive(ctx)

        println("Player disconnected =( ${player!!.nickname}")
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: ClientToServerMessage) {
        when (msg) {
            is CToSChatMessageMessage -> {
                val player =
                    getPlayerByChannel(ctx.channel()) ?: throw AuthException()
                val chatMessage = msg.message

                val packetForSender = SToCChatMessageMessage(message = chatMessage)
                val packetForAll = SToCChatMessageMessage(player.id.toInt(), chatMessage)

                player.connectedChannel.writeAndFlush(packetForSender)
                sendMessageForAllPlayersExcept(packetForAll, player)
            }

            is CToSPlayerIdentificationMessage -> {
                ctx.writeAndFlush(
                    SToCServerIdentificationMessage(
                        0x07,
                        MinecraftString("McLord Classic Server"),
                        MinecraftString("Feel of nostalgia now!"),
                        0x00
                    )
                )

                val newPlayer: Player?

                synchronized(this) {
                    //TODO AUTHORIZE

                    if (activePlayers.size >= MAX_ONLINE_PLAYERS)
                        throw AuthException(
                            Helper.tooManyPeopleArePlayingRightNowError
                        )

                    if (msg.protocolVersion != 0x07)
                        throw AuthException("Unsupported protocol version.")

                    //if (msg.userName.originalString.length > 16)
                    //    throw AuthException("Your nickname is too long.")

                    val id = generatePlayerID()
                        ?: throw ServerError("Unable to generate PlayerID.")

                    val player = Player (
                        ctx.channel(), id, msg.userName, 0, 0, 0, 0, 0
                    )

                    activePlayers += player
                    newPlayer = player
                }

                if (newPlayer == null) throw ServerError("newPlayer is null")

                for (player in activePlayers) {
                    if (player.connectedChannel.id() == newPlayer.connectedChannel.id()) {
                        ctx.writeAndFlush(
                            SToCSpawnPlayerMessage(
                                -1, player.nickname, 0, 0, 0, 0, 0
                            )
                        )

                        println("sent location data to new player.")
                        continue
                    }

                    player.connectedChannel.writeAndFlush(
                        SToCSpawnPlayerMessage(
                            newPlayer.id.toInt(),
                            newPlayer.nickname,
                            newPlayer.x.toInt(),
                            newPlayer.y.toInt(),
                            newPlayer.z.toInt(),
                            newPlayer.yaw,
                            newPlayer.pitch
                        )
                    )

                    println("sent location of new player to another.")

                    newPlayer.connectedChannel.writeAndFlush(
                        SToCSpawnPlayerMessage(
                            player.id.toInt(),
                            player.nickname,
                            player.x.toInt(),
                            player.y.toInt(),
                            player.z.toInt(),
                            player.yaw,
                            player.pitch
                        )
                    )

                    println("sent location of another player to new player.")
                }

                newPlayer.connectedChannel.writeAndFlush(
                    SToCLevelInitializeMessage
                )

                newPlayer.connectedChannel.writeAndFlush(
                    SToCLevelDataChunkMessage(
                        1024, ByteArray(1024) { 0x01 }, 100
                    )
                )

                newPlayer.connectedChannel.writeAndFlush(
                    SToCLevelFinalizeMessage(
                        16, 256, 16
                    )
                )

                newPlayer.connectedChannel.writeAndFlush(SToCPingMessage)

                println("A new player successfully connected! (nickname: ${newPlayer.nickname})")
            }

            is CToSPositionAndOrientationMessage -> {
                val player =
                    getPlayerByChannel(ctx.channel()) ?: throw AuthException()

                player.x = msg.x
                player.y = msg.y
                player.z = msg.z
                player.yaw = msg.yaw
                player.pitch = msg.pitch

                sendMessageForAllPlayersExcept(
                    SToCPositionAndOrientationMessage(
                        player.id.toInt(),
                        msg.x.toInt(),
                        msg.y.toInt(),
                        msg.z.toInt(),
                        msg.yaw,
                        msg.pitch
                    ), player
                )

                player.connectedChannel.writeAndFlush(
                    SToCPositionAndOrientationMessage(
                        -1,
                        msg.x.toInt(),
                        msg.y.toInt(),
                        msg.z.toInt(),
                        msg.yaw,
                        msg.pitch
                    )
                )
            }
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        disconnect(ctx.channel(), "Some error occurred...")
        cause.printStackTrace()
    }

    @Synchronized
    private fun generatePlayerID(): Byte? {
        if (availableIDs.isEmpty()) return null

        val id: Byte = availableIDs.random()
        availableIDs -= id
        return id
    }

    private fun getPlayerByChannel (c: Channel): Player? {
        for (player in activePlayers) {
            if (player.connectedChannel.id() == c.id()) return player
        }

        return null
    }

    private fun sendMessageForAllPlayersExcept (
        msg: ServerToClientMessage,
        vararg exceptPlayers: Player
    ) {
        activePlayers.forEach { player ->
            if (player !in exceptPlayers) player.connectedChannel.writeAndFlush(msg)
        }
    }

    // return true if channel is closed
    private fun closeConnectionIfServerIsNotAbleToAcceptNewPlayers (
        ctx: ChannelHandlerContext
    ): Boolean {
        if (activePlayers.size >= MAX_ONLINE_PLAYERS ||
            channelsNeededToConfirm.size >= MAX_ONLINE_PLAYERS * 4
        ) {
            disconnect(ctx.channel(), Helper.tooManyPeopleArePlayingRightNowError)

            return true
        }

        return false
    }
}