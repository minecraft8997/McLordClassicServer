package ru.mclord.classic.server.codec

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import ru.mclord.classic.server.messages.stoc.ServerToClientMessage

class PacketEncoder : MessageToByteEncoder<ServerToClientMessage>() {
    override fun encode (
        ctx: ChannelHandlerContext,
        msg: ServerToClientMessage,
        out: ByteBuf
    ) {
        out.writeBytes(msg.bytes)
    }
}