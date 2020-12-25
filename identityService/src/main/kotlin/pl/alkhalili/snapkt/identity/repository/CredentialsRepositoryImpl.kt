package pl.alkhalili.snapkt.identity.repository

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import pl.alkhalili.snapkt.identity.domain.Credentials
import pl.alkhalili.snapkt.identity.domain.toCredentials

class CredentialsRepositoryImpl(private val database: Database) : CredentialsRepository {
    override fun all(): ArrayList<Credentials> {
        val credentials: ArrayList<Credentials> = arrayListOf()
        transaction(database) {
            CredentialsTable.selectAll().map { credential ->
                credentials.add(credential.toCredentials())
            }
        }

        return credentials
    }

    override fun insert(credentials: Credentials): Credentials? {
        return transaction(database) {
            CredentialsTable.insert {
                it[username] = credentials.username
                it[password] = credentials.password
                it[phoneNumber] = credentials.phoneNumber
            }
        }.resultedValues?.get(0)?.toCredentials()
    }

    override fun update(credentials: Credentials) {
        transaction(database) {
            CredentialsTable.update({ CredentialsTable.id eq credentials.id }) {
                it[username] = credentials.username
                it[password] = credentials.password
                it[phoneNumber] = credentials.phoneNumber
            }
        }
    }

    override fun findById(id: Long): Credentials? {
        return transaction(database) {
            CredentialsTable.select { CredentialsTable.id eq id }.map { it.toCredentials() }.singleOrNull()
        }
    }

    override fun findByUsername(username: String): Credentials? {
        return transaction(database) {
            CredentialsTable.select { CredentialsTable.username eq username }.map { it.toCredentials() }.singleOrNull()
        }
    }
}
