package ru.mclord.classic.server.utils

import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.nio.charset.Charset

data class MinecraftString(val originalString: String) {
    companion object {
        const val length = 64

        fun DataInputStream.readMinecraftString(): MinecraftString {
            val bytes = ByteArray(length)
            readFully(bytes)

            return MinecraftString (
                bytes.toString(Charset.forName("US-ASCII"))
            )
        }
    }

    init {
        if (originalString.length > length)
            throw IllegalArgumentException("Input string must be <= 64 bytes")
    }

    val minecraftString: ByteArray by lazy {
        val strBytes = originalString.toByteArray(Charset.forName("US-ASCII"))

        val byteArrayOutputStream = ByteArrayOutputStream(length)
        val dataOutputStream = DataOutputStream(byteArrayOutputStream)

        dataOutputStream.write(strBytes)
        repeat(length - strBytes.size) {
            dataOutputStream.writeByte(0x20)
        }

        dataOutputStream.close()
        val result = byteArrayOutputStream.toByteArray()
        println("[DEBUG] minecraftString length: ${result.size}")
        result
    }
}