package ru.mclord.classic.server.logic

import io.netty.channel.Channel
import ru.mclord.classic.server.utils.Head
import ru.mclord.classic.server.utils.Location
import ru.mclord.classic.server.utils.MinecraftString

class Player (
    val connectedChannel: Channel,

    val id: Byte, // byte
    val nickname: MinecraftString,
    val location: Location,
    val head: Head
)