package pl.alkhalili.snapkt.identity.repository

import pl.alkhalili.snapkt.identity.domain.Token

interface TokenRepository {
    fun all(): ArrayList<Token>
    fun insert(token: Token): Token?
    fun update(token: Token)
    fun findById(id: Long): Token?
    fun findByValue(value: String): Token?
}
