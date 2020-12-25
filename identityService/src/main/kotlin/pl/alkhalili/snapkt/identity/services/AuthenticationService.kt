package pl.alkhalili.snapkt.identity.services

import pl.alkhalili.snapkt.identity.domain.Credentials
import pl.alkhalili.snapkt.identity.domain.requests.AuthenticationRequest
import pl.alkhalili.snapkt.identity.domain.requests.CredentialsCreationRequest

interface AuthenticationService {
    fun authenticate(req: AuthenticationRequest): Boolean
    fun createCredentials(req: CredentialsCreationRequest): Credentials?
    fun validateToken(username: String, tokenValue: String)
}
