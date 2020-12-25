package pl.alkhalili.snapkt.identity.repository

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import pl.alkhalili.snapkt.identity.domain.Token
import pl.alkhalili.snapkt.identity.domain.toToken

class TokenRepositoryImpl(private val database: Database) : TokenRepository {
    override fun all(): ArrayList<Token> {
        TODO("Listing all tokens isn't good idea.")
    }

    override fun insert(token: Token): Token? {
        return transaction(database) {
            TokenTable.insert {
                it[credentialsId] = token.credentialsId
                it[value] = token.value
                it[creationDate] = token.creationDate
                it[expirationDate] = token.expirationDate
            }.resultedValues?.get(0)?.toToken()
        }
    }

    override fun update(token: Token) {
        TODO("We shouldn't update existing token, right?")
    }

    override fun findById(id: Long): Token? {
        return transaction(database) {
            TokenTable.select { TokenTable.id eq id }.singleOrNull()?.toToken()
        }
    }

    override fun findByValue(value: String): Token? {
        return transaction(database) {
            TokenTable.select { TokenTable.value eq value }.singleOrNull()?.toToken()
        }
    }
}
