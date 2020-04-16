package ru.mclord.classic.server.utils

import io.netty.channel.Channel
import ru.mclord.classic.server.Helper.CHANNEL_CONFIRM_TIMEOUT_MS

data class ChannelNeededToConfirm(
    val channel: Channel
) {
    val timeout = System.currentTimeMillis() + CHANNEL_CONFIRM_TIMEOUT_MS
}