package ru.mclord.classic.server.utils

import io.netty.channel.Channel
import ru.mclord.classic.server.Helper

data class ChannelNeededToConfirm(
    val channel: Channel
) {
    val timeout = System.currentTimeMillis() + Helper.CHANNEL_CONFIRM_TIMEOUT_MS
    @Volatile var confirmed = false
}