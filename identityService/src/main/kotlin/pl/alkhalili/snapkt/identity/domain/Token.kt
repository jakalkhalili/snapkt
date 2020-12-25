package pl.alkhalili.snapkt.identity.domain

import org.jetbrains.exposed.sql.ResultRow
import org.joda.time.DateTime

import pl.alkhalili.snapkt.identity.repository.TokenTable
import kotlin.random.Random

data class Token(
    val id: Long,
    val credentialsId: Long,
    val value: String,
    val creationDate: DateTime,
    val expirationDate: DateTime
)

fun ResultRow.toToken(): Token = Token(
    id = this[TokenTable.id],
    credentialsId = this[TokenTable.credentialsId],
    value = this[TokenTable.value],
    creationDate = this[TokenTable.creationDate],
    expirationDate = this[TokenTable.expirationDate]
)

enum class TokenStatus(val value: Int) {
    INVALID(0), EXPIRED(1), VALID(2)
}

fun tokenOf(credentialsId: Long, length: Int = 150): Token {
    // TODO: use secure random
    val pool = ('a'..'z') + ('0'..'9') + ('A'..'Z')
    val tokenValue: String = (1..length)
        .map { Random.nextInt(0, pool.size) }
        .map(pool::get)
        .joinToString("")

    // TODO: token regeneration
    return Token(0, credentialsId, tokenValue, DateTime.now(), DateTime.now().plusDays(7))
}
