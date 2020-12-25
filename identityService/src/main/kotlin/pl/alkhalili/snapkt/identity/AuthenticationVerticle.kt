package pl.alkhalili.snapkt.identity

import com.google.gson.Gson
import io.vertx.core.Promise
import pl.alkhalili.snapkt.common.EventBusMessage
import pl.alkhalili.snapkt.common.ServiceVerticle
import pl.alkhalili.snapkt.identity.domain.TokenStatus
import pl.alkhalili.snapkt.identity.domain.requests.AuthenticationRequest
import pl.alkhalili.snapkt.identity.domain.requests.CredentialsCreationRequest
import pl.alkhalili.snapkt.identity.domain.requests.TokenValidationRequest
import pl.alkhalili.snapkt.identity.exceptions.ExpiredTokenException
import pl.alkhalili.snapkt.identity.exceptions.InvalidTokenException
import pl.alkhalili.snapkt.identity.services.AuthenticationService

class AuthenticationVerticle(override val service: AuthenticationService) :
    ServiceVerticle<AuthenticationService>("identity.AuthenticationService", service) {
    override fun start(promise: Promise<Void>?) {
        super.start(promise)

        eventBus?.consumer<String>(ADDRESS) {
            val eventBusMessage = Gson().fromJson<EventBusMessage>(it.body(), EventBusMessage::class.java)
            logger.debug("Received message type: ${eventBusMessage.type}")
            when (eventBusMessage.type) {
                AuthenticationRequest::class.java.simpleName -> {
                    val req = Gson().fromJson<AuthenticationRequest>(
                        eventBusMessage.message,
                        AuthenticationRequest::class.java
                    )
                    if (!service.authenticate(req)) {
                        it.fail(100, "Authentication failure")
                    } else {
                        it.reply(true)
                    }
                }
                CredentialsCreationRequest::class.java.simpleName -> {
                    val req = Gson().fromJson<CredentialsCreationRequest>(
                        eventBusMessage.message,
                        CredentialsCreationRequest::class.java
                    )
                    it.reply(service.createCredentials(req) != null)
                }
                TokenValidationRequest::class.java.simpleName -> {
                    val req = Gson().fromJson<TokenValidationRequest>(
                        eventBusMessage.message,
                        TokenValidationRequest::class.java
                    )

                    try {
                        service.validateToken(req.username, req.tokenValue)

                        logger.info("Token valid for ${req.username}!")
                        it.reply(TokenStatus.VALID.value)
                    } catch (e: InvalidTokenException) {
                        logger.warn("Invalid token for ${req.username}")
                        it.fail(TokenStatus.INVALID.value, "Invalid token")
                    } catch (e: ExpiredTokenException) {
                        logger.warn("Expired token for ${req.username}")
                        it.fail(TokenStatus.EXPIRED.value, "Expired token")
                    }
                }
            }
        }

        promise?.complete()
    }

    override fun stop(promise: Promise<Void>?) {
        super.stop(promise)
    }

    companion object {
        val ADDRESS: String = this::class.qualifiedName!!
    }
}
