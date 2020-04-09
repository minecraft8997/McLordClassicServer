package ru.mclord.classic.server.messages.ctos

import ru.mclord.classic.server.Helper.isUnsignedByte

/*
 * Comment from wiki.vg:
 *
 * Sent frequently (even while not moving) by the player
 * with the player's current location and orientation.
 * Player ID is always 255, referring to itself.
 * Player coordinates are fixed-point values with the
 * lowest 5 bits representing the fractional position
 * (i.e. divide by 32 to get actual position in terms of block coordinates).
 * The angle parameters are scaled such that a value
 * of 256 would correspond to 360 degrees.
 */

data class CToSPositionAndOrientationMessage (
    val playerID: Int, // unsigned byte
                       // playerID is always 255, referring to itself
    val x: Short,
    val y: Short,
    val z: Short,
    val yaw: Int, // unsigned byte
    val pitch: Int // unsigned byte
): ClientToServerMessage {
    companion object {
        const val PACKET_ID = 0x08 // unsigned byte
        const val PACKET_LEN = 1 + 1 + 2 * 3 + 1 + 1
    }

    init {
        if (!playerID.isUnsignedByte ||
            !yaw.isUnsignedByte ||
            !pitch.isUnsignedByte
        ) throw IllegalArgumentException()
    }
}