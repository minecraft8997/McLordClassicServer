package ru.mclord.classic.server.utils

import ru.mclord.classic.server.Helper.isUnsignedByte

data class Head (
    private var yaw: Int,
    private var pitch: Int
) {
    @Volatile private var blocked = false

    init {
        if (!yaw.isUnsignedByte || !pitch.isUnsignedByte)
            throw IllegalArgumentException()
    }

    @Synchronized
    @Suppress("ControlFlowWithEmptyBody")
    private fun setBlocked (block: Boolean) {
        if (blocked && block) while (blocked) ;
        blocked = block
    }

    fun getYawAndPitch(): Pair<Int, Int> {
        setBlocked(true)
        val currentYaw = yaw
        val currentPitch = pitch
        setBlocked(false)

        return Pair(currentYaw, currentPitch)
    }

    fun setYawAndPitch(newYaw: Int, newPitch: Int) {
        if (!newYaw.isUnsignedByte || !newPitch.isUnsignedByte)
            throw IllegalArgumentException()

        setBlocked(true)
        yaw = newYaw
        pitch = newPitch
        setBlocked(false)
    }
}