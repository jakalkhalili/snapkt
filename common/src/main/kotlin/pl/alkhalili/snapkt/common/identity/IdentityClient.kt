package pl.alkhalili.snapkt.common.identity

import io.vertx.core.http.HttpClient
import io.vertx.ext.web.RoutingContext

class IdentityClient(private val httpClient: HttpClient, private val identityHost: String) {
    fun requireAuthorized(ctx: RoutingContext) {
        val token = ctx.request().getHeader("X-SNAPKT-TOKEN")
        if (token == null) {
            ctx.response().setStatusCode(401).end("Please provide a token")
        }



        ctx.next()
    }
}
