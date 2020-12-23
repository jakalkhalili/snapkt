package pl.alkhalili.snapkt.identity

import com.google.gson.Gson
import io.vertx.core.eventbus.EventBus
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.await
import pl.alkhalili.snapkt.common.Routing
import pl.alkhalili.snapkt.common.eventBusMessageOf
import pl.alkhalili.snapkt.common.toJson
import pl.alkhalili.snapkt.identity.domain.AuthenticationRequest
import pl.alkhalili.snapkt.identity.domain.CredentialsCreationRequest

class Routes(bus: EventBus): Routing(bus) {
    suspend fun authenticate(ctx: RoutingContext) {
        val authenticationRequest: AuthenticationRequest =
            Gson().fromJson(ctx.bodyAsString, AuthenticationRequest::class.java)

        val eventBusRequest = eventBus.request<Boolean>(
            AuthenticationVerticle.ADDRESS,
            eventBusMessageOf(authenticationRequest, AuthenticationRequest::class.java).toJson()
        ).await()

        if (!eventBusRequest.body()) {
            ctx.response().setStatusCode(401).end()
            return
        }

        ctx.response().setStatusCode(200).end()
    }

    fun createCredentials(ctx: RoutingContext) {
        val req: CredentialsCreationRequest = Gson().fromJson(ctx.bodyAsString, CredentialsCreationRequest::class.java)

        eventBus.request<Void>(
            AuthenticationVerticle.ADDRESS,
            eventBusMessageOf(req, CredentialsCreationRequest::class.java).toJson()
        )

        ctx.response().setStatusCode(201).end()
    }
}
