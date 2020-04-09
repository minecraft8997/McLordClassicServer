package ru.mclord.classic.server.logic

import io.netty.channel.Channel
import ru.mclord.classic.server.Helper.isUnsignedByte
import ru.mclord.classic.server.utils.MinecraftString

class Player (
    val connectedChannel: Channel,

    val id: Byte, // byte
    val nickname: MinecraftString,
    var x: Short,
    var y: Short,
    var z: Short,
    yaw: Int, // unsigned byte
    pitch: Int // unsigned byte
) {
    init {
        if (!yaw.isUnsignedByte) throw IllegalArgumentException()
        if (!pitch.isUnsignedByte) throw IllegalArgumentException()
    }

    var yaw = yaw
        set(value) {
            if (!value.isUnsignedByte) throw IllegalArgumentException()
            field = value
        }
    var pitch = pitch
        set(value) {
            if (!value.isUnsignedByte) throw IllegalArgumentException()
            field = value
        }
}