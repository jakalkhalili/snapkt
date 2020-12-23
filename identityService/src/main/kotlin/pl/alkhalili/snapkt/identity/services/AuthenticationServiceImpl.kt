package pl.alkhalili.snapkt.identity.services

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pl.alkhalili.snapkt.identity.domain.AuthenticationRequest
import pl.alkhalili.snapkt.identity.domain.Credentials
import pl.alkhalili.snapkt.identity.domain.CredentialsCreationRequest
import pl.alkhalili.snapkt.identity.repository.CredentialsRepository

class AuthenticationServiceImpl(private val credentialsRepository: CredentialsRepository) : AuthenticationService {
    private val bcryptPasswordEncoder: BCryptPasswordEncoder = BCryptPasswordEncoder()

    override fun authenticate(req: AuthenticationRequest): Boolean {
        val credentials: Credentials = credentialsRepository.findByUsername(req.username) ?: return false

        if (!bcryptPasswordEncoder.matches(req.password, credentials.password)) {
            return false
        }

        return true
    }

    override fun createCredentials(req: CredentialsCreationRequest): Boolean {
        val credentials = Credentials(
            id = 0, // Will be auto incremented by database
            username = req.username,
            password = bcryptPasswordEncoder.encode(req.password),
            phoneNumber = req.phoneNumber
        )

        return credentialsRepository.insert(credentials)
    }
}
