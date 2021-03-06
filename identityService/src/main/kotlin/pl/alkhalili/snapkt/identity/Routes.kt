package pl.alkhalili.snapkt.identity

import com.google.gson.Gson
import io.vertx.core.eventbus.EventBus
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.await
import pl.alkhalili.snapkt.common.Routing
import pl.alkhalili.snapkt.common.eventBusMessageOf
import pl.alkhalili.snapkt.common.toJson
import pl.alkhalili.snapkt.identity.domain.requests.AuthenticationRequest
import pl.alkhalili.snapkt.identity.domain.requests.CredentialsCreationRequest
import pl.alkhalili.snapkt.identity.domain.requests.TokenValidationRequest

class Routes(bus: EventBus) : Routing(bus) {
    suspend fun authenticate(ctx: RoutingContext) {
        if(ctx.body == null) {
            ctx.response().setStatusCode(401).end()
            return
        }

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
        if(ctx.body == null) {
            ctx.response().setStatusCode(400).end()
            return
        }

        val req: CredentialsCreationRequest = Gson().fromJson(ctx.bodyAsString, CredentialsCreationRequest::class.java)

        eventBus.request<Void>(
            AuthenticationVerticle.ADDRESS,
            eventBusMessageOf(req, CredentialsCreationRequest::class.java).toJson()
        )

        ctx.response().setStatusCode(201).end()
    }

    fun validateToken(ctx: RoutingContext) {
        val req: TokenValidationRequest = Gson().fromJson(ctx.bodyAsString, TokenValidationRequest::class.java)

        val tokenStatus = eventBus.request<Int>(
            AuthenticationVerticle.ADDRESS,
            eventBusMessageOf(req, TokenValidationRequest::class.java).toJson()
        )

        if (tokenStatus.failed()) {
            ctx.response().setStatusCode(401).end()
            return
        }

        ctx.response().setStatusCode(201).end()
    }
}
