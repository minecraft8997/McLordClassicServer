package ru.mclord.classic.server.utils

import ru.mclord.classic.server.Helper.coordinatesAreValid
import ru.mclord.classic.server.logic.World

data class Location (
    private var world: World,
    private var x: Int,
    private var y: Int,
    private var z: Int
) {
    @Volatile private var blocked = false
    @Volatile private var lastLocationChange = System.currentTimeMillis()

    init {
        if (!coordinatesAreValid(x, y, z))
            throw IllegalArgumentException()
    }

    @Synchronized
    @Suppress("ControlFlowWithEmptyBody")
    private fun setBlocked (block: Boolean) {
        if (blocked && block) while (blocked) ;
        blocked = block
    }

    fun getLastLocationChange(): Long {
        setBlocked(true)
        val currentLastLocationChange = lastLocationChange
        setBlocked(false)

        return currentLastLocationChange
    }

    fun getWorld(): World {
        setBlocked(true)
        val currentWorld = world
        setBlocked(false)

        return currentWorld
    }

    fun getXYZ(): Triple<Int, Int, Int> {
        setBlocked(true)
        val currentX = x
        val currentY = y
        val currentZ = z
        setBlocked(false)

        return Triple(currentX, currentY, currentZ)
    }

    fun setCoordinates(newWorld: World? = null, newX: Int, newY: Int, newZ: Int) {
        if (!coordinatesAreValid(newX, newY, newZ))
            throw IllegalArgumentException()

        setBlocked(true)
        if (newWorld != null) world = newWorld
        x = newX
        y = newY
        z = newZ
        lastLocationChange = System.currentTimeMillis()
        setBlocked(false)
    }
}