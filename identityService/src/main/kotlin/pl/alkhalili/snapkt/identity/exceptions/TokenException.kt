package pl.alkhalili.snapkt.identity.exceptions

import pl.alkhalili.snapkt.identity.domain.Token

abstract class TokenException(
    message: String?,
    cause: Throwable?
) : Exception(message, cause)

class ExpiredTokenException(token: Token) : TokenException("Token ${token.id} expired", null)
class InvalidTokenException() : TokenException("Invalid token", null)
