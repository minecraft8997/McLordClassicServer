package ru.mclord.classic.server.codec

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import ru.mclord.classic.server.Helper.disconnect
import ru.mclord.classic.server.exceptions.ProtocolException
import ru.mclord.classic.server.messages.ctos.CToSChatMessageMessage
import ru.mclord.classic.server.messages.ctos.CToSPlayerIdentificationMessage
import ru.mclord.classic.server.messages.ctos.CToSPositionAndOrientationMessage
import ru.mclord.classic.server.messages.ctos.CToSSetBlockMessage
import ru.mclord.classic.server.utils.MinecraftString.Companion.readMinecraftString
import java.io.ByteArrayInputStream
import java.io.DataInputStream

class MessageDecoder : ByteToMessageDecoder() {
    override fun decode(
        ctx: ChannelHandlerContext,
        packet: ByteBuf,
        out: MutableList<Any>
    ) {
        val bytes = ByteArray(packet.readableBytes())
        for (i in bytes.indices) bytes[i] = packet.readByte()
        if (bytes.isEmpty()) return

        val byteArrayInputStream = ByteArrayInputStream(bytes)
        val packetData = DataInputStream(byteArrayInputStream)

        @Suppress("NAME_SHADOWING")
        packetData.use { packetData ->
            when (packetData.readByte().toInt() and 0xFF) {
                // The first byte of each packet is packet ID

                CToSChatMessageMessage.PACKET_ID -> {
                    if (packetData.available() != CToSChatMessageMessage.PACKET_LEN - 1)
                    // PACKET_LEN - 1 because we have already
                    // read PacketID
                        throw ProtocolException()

                    if ((packetData.readByte().toInt() and 0xFF) != 0xFF) throw ProtocolException()
                    val chatMessage = packetData.readMinecraftString()
                    out += CToSChatMessageMessage(chatMessage)
                }

                CToSPlayerIdentificationMessage.PACKET_ID -> {
                    if (packetData.available() != CToSPlayerIdentificationMessage.PACKET_LEN - 1)
                        throw ProtocolException("${packetData.available()} ${CToSPlayerIdentificationMessage.PACKET_LEN - 1}")

                    val protocolVersion = packetData.readByte().toInt() and 0xFF
                    val userName = packetData.readMinecraftString()
                    val verificationKey = packetData.readMinecraftString()
                    packetData.readByte() // unused

                    //val unused = packetData.readByte().toInt() and 0xFF
                    //if (unused != 0x00)
                    //    throw ProtocolException("unused: $unused")

                    out += CToSPlayerIdentificationMessage(
                        protocolVersion, userName, verificationKey
                    )

                    /*
                    val protocolVersion = packetData.readByte().toInt() and 0xFF
                    //val bytes = ByteArray(64)
                    //packetData.readFully(bytes)
                    val userName = packetData.readMinecraftString()
                    val verificationKey = packetData.readMinecraftString()
                    if ((packetData.readByte().toInt() and 0xFF) != 0x00) throw ProtocolException()

                    //println(protocolVersion) // 7
                    //println(String(bytes)) // deewend
                    //println(bytes.size) // 64
                    out += CToSPlayerIdentificationMessage(
                        protocolVersion, userName, verificationKey
                    )
                     */
                }

                CToSPositionAndOrientationMessage.PACKET_ID -> {
                    println("Received PositionAndOrientation")
                    //if (packetData.available() != CToSPositionAndOrientationMessage.PACKET_LEN - 1)
                    //    throw ProtocolException("${packetData.available()} ${CToSPositionAndOrientationMessage.PACKET_LEN - 1}")

                    println(packetData.available())
                    do {
                        val playerID = packetData.readByte().toInt() and 0xFF
                        if (playerID != 0xFF) throw ProtocolException()
                        val x = packetData.readShort()
                        val y = packetData.readShort()
                        val z = packetData.readShort()
                        val yaw = packetData.readByte().toInt() and 0xFF
                        val pitch = packetData.readByte().toInt() and 0xFF

                        out += CToSPositionAndOrientationMessage(
                            playerID, x, y, z, yaw, pitch
                        )
                    } while (
                        packetData.available() > 0 &&
                        packetData.readByte().toInt() and 0xFF == CToSPositionAndOrientationMessage.PACKET_ID
                    )
                }

                CToSSetBlockMessage.PACKET_ID -> {
                    if (packetData.available() != CToSSetBlockMessage.PACKET_LEN - 1)
                        throw ProtocolException()

                    val x = packetData.readShort()
                    val y = packetData.readShort()
                    val z = packetData.readShort()
                    val mode = packetData.readByte().toInt() and 0xFF
                    val blockType = packetData.readByte().toInt() and 0xFF

                    out += CToSSetBlockMessage(
                        x, y, z, mode, blockType
                    )
                }

                else -> throw ProtocolException() // unknown PacketID received
            }
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        when (cause.cause) {
            is ProtocolException -> {
                println(cause.message)
                cause.printStackTrace()
                //disconnect(ctx.channel(), "Are you playing via official McLord Classic Client?")
            }

            else -> {
                disconnect(ctx.channel(), "Some error occurred. Admins were notified about this problem. :(")

                //TODO
                if (cause.message != null) println(cause.message)
                cause.printStackTrace()
            }
        }
    }
}