package ru.mclord.classic.server

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelPipeline
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import ru.mclord.classic.server.codec.MessageDecoder
import ru.mclord.classic.server.codec.MessageEncoder

fun main() {
    val bossGroup = NioEventLoopGroup()
    val workerGroup = NioEventLoopGroup()
    val serverBootstrap = ServerBootstrap()

    serverBootstrap.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel::class.java)
        .handler(LoggingHandler(LogLevel.INFO))
        .childHandler (object : ChannelInitializer<SocketChannel>() {
            override fun initChannel(ch: SocketChannel) {
                val pipeline: ChannelPipeline = ch.pipeline()

                pipeline.addLast(MessageDecoder())
                pipeline.addLast(MessageEncoder())

                pipeline.addLast(ServerHandler)
            }
        })
    serverBootstrap.bind(25565).sync().channel().closeFuture().sync()
}