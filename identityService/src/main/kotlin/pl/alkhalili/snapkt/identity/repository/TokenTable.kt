package pl.alkhalili.snapkt.identity.repository

import org.jetbrains.exposed.sql.Table

internal object TokenTable : Table("tokens") {
    val id = long("id").primaryKey().autoIncrement()
    val credentialsId = long("credentials_id").references(CredentialsTable.id)
    val value = text("value")
    val creationDate = datetime("creation_date")
    val expirationDate = datetime("expiration_date")
}
