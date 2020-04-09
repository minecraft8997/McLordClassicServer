package ru.mclord.classic.server.exceptions

class AuthException : Exception {
    constructor()
    constructor(message: String) : super(message)
}