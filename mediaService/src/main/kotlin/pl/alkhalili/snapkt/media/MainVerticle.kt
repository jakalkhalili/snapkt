package pl.alkhalili.snapkt.media

import io.vertx.core.Promise
import io.vertx.ext.web.Router
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pl.alkhalili.snapkt.common.HikariConnectionPool
import pl.alkhalili.snapkt.common.middleware.HttpErrorMiddleware
import pl.alkhalili.snapkt.common.Microservice
import pl.alkhalili.snapkt.common.identity.IdentityClient

class MainVerticle : Microservice() {
    override val connectionPool: HikariConnectionPool = HikariConnectionPool("root", "", "jdbc:h2:mem:test")

    private var identityClient: IdentityClient? = null

    override fun start(promise: Promise<Void>) {
        super.start(promise)

        identityClient = IdentityClient(WebClient.create(vertx), "envoy-front-proxy", 8080, "/identity/v1")
    }

    override fun setupRoutes(): Router {
        return Router.router(vertx).apply {
            route().handler(BodyHandler.create())
            route().handler(HttpErrorMiddleware())

            post("/media/$API_VERSION/upload").handler { ctx ->
                try {
                    identityClient?.requireAuthorized(ctx)
                } catch(e: Exception) {
                    ctx.response().setStatusCode(401).end()
                }
                GlobalScope.launch(vertx.dispatcher()) {
                    ctx.response().setStatusCode(200).end("TODO: upload")
                }
            }
        }
    }

    companion object {
        private const val API_VERSION: String = "v1"
    }
}
