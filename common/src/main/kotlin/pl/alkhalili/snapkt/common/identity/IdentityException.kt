package pl.alkhalili.snapkt.common.identity

abstract class IdentityException(override val message: String) : Exception()

class AuthorizationFailureException(override val message: String) : IdentityException(message)
class AuthorizationMethodNotSupported(authorizationMethod: String) :
    IdentityException("Authorization method not supported: $authorizationMethod")
