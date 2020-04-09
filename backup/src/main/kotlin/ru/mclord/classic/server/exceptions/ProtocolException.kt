package ru.mclord.classic.server.exceptions

class ProtocolException : Exception {
    constructor()
    constructor(message: String) : super(message)
}