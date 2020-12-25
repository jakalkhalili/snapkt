package pl.alkhalili.snapkt.identity.domain.requests

data class TokenValidationRequest(val username: String, val tokenValue: String)
