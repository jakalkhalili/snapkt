package pl.alkhalili.snapkt.identity.repository

import org.jetbrains.exposed.sql.Table

internal object CredentialsTable : Table("credentials") {
    val id = long("id").primaryKey().autoIncrement()
    val username = varchar("username", 32)
    val password = text("password")
    val phoneNumber = integer("phone_number")
}
