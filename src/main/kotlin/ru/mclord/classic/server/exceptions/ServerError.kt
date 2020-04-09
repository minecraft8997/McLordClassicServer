package ru.mclord.classic.server.exceptions

class ServerError : Error {
    constructor()
    constructor(message: String) : super(message)
}