package pl.alkhalili.snapkt.identity.domain

data class CredentialsCreationRequest(val username: String, val password: String, val phoneNumber: Int)
