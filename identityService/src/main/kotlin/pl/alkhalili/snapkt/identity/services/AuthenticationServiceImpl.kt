package pl.alkhalili.snapkt.identity.services

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pl.alkhalili.snapkt.common.extensions.sha256
import pl.alkhalili.snapkt.identity.domain.Credentials
import pl.alkhalili.snapkt.identity.domain.credentialsOf
import pl.alkhalili.snapkt.identity.domain.requests.AuthenticationRequest
import pl.alkhalili.snapkt.identity.domain.requests.CredentialsCreationRequest
import pl.alkhalili.snapkt.identity.exceptions.ExpiredTokenException
import pl.alkhalili.snapkt.identity.exceptions.InvalidTokenException
import pl.alkhalili.snapkt.identity.repository.CredentialsRepository
import pl.alkhalili.snapkt.identity.repository.TokenRepository

class AuthenticationServiceImpl(
    private val credentialsRepository: CredentialsRepository,
    private val tokenRepository: TokenRepository,
    private val bcryptPasswordEncoder: BCryptPasswordEncoder
) : AuthenticationService {
    override fun authenticate(req: AuthenticationRequest): Boolean {
        val credentials: Credentials = credentialsRepository.findByUsername(req.username) ?: return false

        if (!bcryptPasswordEncoder.matches(req.password, credentials.password)) {
            return false
        }

        return true
    }

    override fun createCredentials(req: CredentialsCreationRequest): Credentials? {
        val credentials = credentialsOf(req.username, bcryptPasswordEncoder.encode(req.password), req.phoneNumber)
        return credentialsRepository.insert(credentials)
    }

    override fun validateToken(username: String, tokenValue: String) {
        val foundToken = tokenRepository.findByValue(tokenValue.sha256()) ?: throw InvalidTokenException()
        if (foundToken.expirationDate.isBeforeNow) {
            throw ExpiredTokenException(foundToken)
        }

        // Check it's user own token or somebody else
        credentialsRepository.findById(foundToken.credentialsId) ?: throw InvalidTokenException()
    }
}
