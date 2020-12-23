package pl.alkhalili.snapkt.identity.services

import pl.alkhalili.snapkt.identity.domain.AuthenticationRequest
import pl.alkhalili.snapkt.identity.domain.CredentialsCreationRequest

interface AuthenticationService {
    fun authenticate(req: AuthenticationRequest): Boolean
    fun createCredentials(req: CredentialsCreationRequest): Boolean
}
