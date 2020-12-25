package pl.alkhalili.snapkt.identity.domain.requests

data class CredentialsCreationRequest(val username: String, val password: String, val phoneNumber: Int)
