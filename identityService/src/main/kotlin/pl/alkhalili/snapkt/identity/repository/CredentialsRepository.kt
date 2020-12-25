package pl.alkhalili.snapkt.identity.repository

import pl.alkhalili.snapkt.identity.domain.Credentials

interface CredentialsRepository {
    fun all(): ArrayList<Credentials>
    fun insert(credentials: Credentials): Credentials?
    fun update(credentials: Credentials)
    fun findById(id: Long): Credentials?
    fun findByUsername(username: String): Credentials?
}
