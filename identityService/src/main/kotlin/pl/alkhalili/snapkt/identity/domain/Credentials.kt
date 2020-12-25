package pl.alkhalili.snapkt.identity.domain

import org.jetbrains.exposed.sql.ResultRow
import pl.alkhalili.snapkt.identity.repository.CredentialsTable


data class Credentials(val id: Long, val username: String, val password: String, val phoneNumber: Int)

fun ResultRow.toCredentials(): Credentials = Credentials(
    id = this[CredentialsTable.id],
    username = this[CredentialsTable.username],
    password = this[CredentialsTable.password],
    phoneNumber = this[CredentialsTable.phoneNumber]
)

fun credentialsOf(username: String, password: String, phoneNumber: Int): Credentials = Credentials(
    0,
    username,
    password,
    phoneNumber
)
