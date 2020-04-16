package ru.mclord.classic.server.logic

import ru.mclord.classic.server.Helper.isUnsignedByte

enum class Block (val id: Int) {
    AIR(0),
    STONE(1),
    GRASS(2),
    DIRT(3),
    COBBLESTONE(4),
    WOOD(5);

    init {
        if (!id.isUnsignedByte)
            throw IllegalArgumentException()
    }
}