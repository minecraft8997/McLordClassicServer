package ru.mclord.classic.server

import io.netty.channel.Channel
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.group.DefaultChannelGroup
import io.netty.util.concurrent.GlobalEventExecutor
import ru.mclord.classic.server.Helper.MAX_ONLINE_PLAYERS
import ru.mclord.classic.server.Helper.coordinatesAreValid
import ru.mclord.classic.server.Helper.disconnect
import ru.mclord.classic.server.Helper.getDistance
import ru.mclord.classic.server.Helper.sendChatMessage
import ru.mclord.classic.server.exceptions.AuthException
import ru.mclord.classic.server.exceptions.ServerError
import ru.mclord.classic.server.logic.Player
import ru.mclord.classic.server.messages.ctos.*
import ru.mclord.classic.server.messages.stoc.*
import ru.mclord.classic.server.utils.ChannelNeededToConfirm
import ru.mclord.classic.server.utils.Head
import ru.mclord.classic.server.utils.Location
import ru.mclord.classic.server.utils.MinecraftString
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

@ChannelHandler.Sharable
object ServerHandler : SimpleChannelInboundHandler<ClientToServerMessage>() {
    private val activeChannels = DefaultChannelGroup(GlobalEventExecutor.INSTANCE)
    val channelsNeededToConfirm: MutableSet<ChannelNeededToConfirm> = Collections.synchronizedSet(
        HashSet<ChannelNeededToConfirm>()
    )

    val activePlayers: MutableList<Player> = Collections.synchronizedList(ArrayList<Player>())
    private val availableIDs = Collections.synchronizedSet(HashSet<Byte>(256))

    init {
        // Byte.MIN_VALUE + 1 because McLordBot's id always equals Byte.MIN_VALUE
        // and it's id will never be available for common player
        for (i in (Byte.MIN_VALUE + 1)..Byte.MAX_VALUE) availableIDs += i.toByte()
        if (availableIDs.size != 256 - 1) throw IllegalStateException("availableIDs.size != 256")

        Helper.UnconfirmedChannelsKicker.start()
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
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
            println("Player disconnected (nickname: ${player.nickname.normalString})")
        }

        activeChannels -= ctx.channel()
        super.channelInactive(ctx)
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
                    var currentUnconfirmedChannel: ChannelNeededToConfirm? = null

                    for (unconfirmedChannel in channelsNeededToConfirm) {
                        if (unconfirmedChannel.channel.id() == ctx.channel().id()) {
                            currentUnconfirmedChannel = unconfirmedChannel
                            break
                        }
                    }

                    if (currentUnconfirmedChannel == null)
                        throw AuthException("Already authorized")

                    if (activePlayers.size >= MAX_ONLINE_PLAYERS)
                        throw AuthException(
                            Helper.tooManyPeopleArePlayingRightNowError
                        )

                    if (msg.protocolVersion != 0x07)
                        throw AuthException("Unsupported protocol version.")

                    if (msg.userName.normalString.length > 16)
                        throw AuthException("Your nickname is too long.")

                    val id = generatePlayerID()
                        ?: throw ServerError("Unable to generate PlayerID.")

                    channelsNeededToConfirm -= currentUnconfirmedChannel

                    val player = Player (
                        ctx.channel(), id, msg.userName,
                        Location(Helper.currentWorld, 100, 200, 100),
                        Head(100, 100)
                    )

                    newPlayer = player
                    activePlayers += newPlayer
                }

                if (newPlayer == null) throw ServerError()

                Thread {
                    val newPlayerWorld = newPlayer.location.getWorld()
                    val newPlayerPosition = newPlayer.location.getXYZ()
                    val newPlayerYawAndPitch = newPlayer.head.getYawAndPitch()

                    newPlayerWorld.sendTo(newPlayer)

                    newPlayer.connectedChannel.writeAndFlush(
                        SToCSpawnPlayerMessage(
                            -1,
                            newPlayer.nickname,
                            newPlayerPosition.first,
                            newPlayerPosition.second,
                            newPlayerPosition.third,
                            newPlayerYawAndPitch.first,
                            newPlayerYawAndPitch.second
                        )
                    )

                    newPlayer.connectedChannel.writeAndFlush(
                        SToCSpawnPlayerMessage(
                            Byte.MIN_VALUE.toInt(),
                            MinecraftString("McLordBot"),
                            100, 100, 100, 59, 10
                        )
                    )

                    for (player in activePlayers) {
                        if (player == newPlayer) continue

                        if (player.location.getWorld() == newPlayerWorld) {
                            player.connectedChannel.writeAndFlush(
                                SToCSpawnPlayerMessage(
                                    newPlayer.id.toInt(),
                                    newPlayer.nickname,
                                    newPlayerPosition.first,
                                    newPlayerPosition.second,
                                    newPlayerPosition.third,
                                    newPlayerYawAndPitch.first,
                                    newPlayerYawAndPitch.second
                                )
                            )

                            val playerPosition = player.location.getXYZ()
                            val playerYawAndPitch = player.head.getYawAndPitch()

                            newPlayer.connectedChannel.writeAndFlush(
                                SToCSpawnPlayerMessage(
                                    player.id.toInt(),
                                    player.nickname,
                                    playerPosition.first,
                                    playerPosition.second,
                                    playerPosition.third,
                                    playerYawAndPitch.first,
                                    playerYawAndPitch.second
                                )
                            )
                        }
                    }

                    println(
                        "A new player successfully connected! (nickname: ${newPlayer.nickname.normalString})"
                    )
                }.start()
            }

            is CToSPositionAndOrientationMessage -> {
                val player =
                    getPlayerByChannel(ctx.channel()) ?: throw AuthException()

                val playerLocation = player.location
                val playerHead = player.head
                val currentPosition = playerLocation.getXYZ()
                val currentHead = playerHead.getYawAndPitch()

                fun putPlayerInHisPlace() {
                    player.connectedChannel.writeAndFlush(
                        SToCPositionAndOrientationMessage(
                            -1,
                            currentPosition.first,
                            currentPosition.second,
                            currentPosition.third,
                            currentHead.first,
                            currentHead.second
                        )
                    )
                }

                val newX = msg.x.toInt() / 32
                val newY = msg.y.toInt() / 32
                val newZ = msg.z.toInt() / 32
                val newYaw = msg.yaw
                val newPitch = msg.pitch

                //println("$newX $newY $newZ")

                if (!coordinatesAreValid(newX, newY, newZ)) {
                    sendChatMessage(player, "You went out of the world!")
                    putPlayerInHisPlace()

                    return
                }

                /*
                val positionDelta = getDistance(
                    currentPosition.first, currentPosition.second, currentPosition.third,
                    newX, newY, newZ
                )

                val timeDelta =
                    System.currentTimeMillis() - playerLocation.getLastLocationChange()

                if (positionDelta > timeDelta / 20.0) {
                    sendChatMessage(player, "Are you Sonic? :D")
                    putPlayerInHisPlace()

                    return
                }

                 */

                playerLocation.setCoordinates(
                    null,
                    newX, newY, newZ
                )

                playerHead.setYawAndPitch(newYaw, newPitch)

                sendMessageForAllPlayersExcept(
                    SToCPositionAndOrientationMessage(
                        player.id.toInt(),
                        newX,
                        newY,
                        newZ,
                        newYaw,
                        newPitch
                    ), player
                )

                /*
                player.connectedChannel.writeAndFlush(
                    SToCPositionAndOrientationMessage(
                        -1,
                        newX,
                        newY,
                        newZ,
                        newYaw,
                        newPitch
                    )
                )

                 */

                println("done handling PositionAndOrientationMessage")
            }

            is CToSSetBlockMessage -> {
                //TODO
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

    // returns true if channel is closed
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