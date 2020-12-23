package pl.alkhalili.snapkt.identity

import com.google.gson.Gson
import io.vertx.core.Promise
import pl.alkhalili.snapkt.common.EventBusMessage
import pl.alkhalili.snapkt.common.ServiceVerticle
import pl.alkhalili.snapkt.identity.domain.AuthenticationRequest
import pl.alkhalili.snapkt.identity.domain.CredentialsCreationRequest
import pl.alkhalili.snapkt.identity.services.AuthenticationService

class AuthenticationVerticle(override val service: AuthenticationService) :
    ServiceVerticle<AuthenticationService>("identity.AuthenticationService", service) {
    override fun start(promise: Promise<Void>?) {
        super.start(promise)

        eventBus?.consumer<String>(ADDRESS) {
            val eventBusMessage = Gson().fromJson<EventBusMessage>(it.body(), EventBusMessage::class.java)

            when (eventBusMessage.type) {
                AuthenticationService::class.java.simpleName -> {
                    val fromJson = Gson().fromJson<AuthenticationRequest>(
                        eventBusMessage.message,
                        AuthenticationRequest::class.java
                    )
                    if (!service.authenticate(fromJson)) {
                        it.fail(100, "Authentication failure")
                    } else {
                        it.reply(true)
                    }
                }
                CredentialsCreationRequest::class.java.simpleName -> {
                    val fromJson = Gson().fromJson<CredentialsCreationRequest>(
                        eventBusMessage.message,
                        CredentialsCreationRequest::class.java
                    )
                    it.reply(service.createCredentials(fromJson))
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
