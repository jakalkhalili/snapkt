package pl.alkhalili.snapkt.identity

import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.eventbus.EventBus
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.Database
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import pl.alkhalili.snapkt.common.HikariConnectionPool
import pl.alkhalili.snapkt.common.HttpErrorMiddleware
import pl.alkhalili.snapkt.common.Microservice
import pl.alkhalili.snapkt.common.Migration
import pl.alkhalili.snapkt.identity.repository.CredentialsRepository
import pl.alkhalili.snapkt.identity.repository.CredentialsRepositoryImpl
import pl.alkhalili.snapkt.identity.services.AuthenticationService
import pl.alkhalili.snapkt.identity.services.AuthenticationServiceImpl
import javax.sql.DataSource

class MainVerticle : Microservice() {
    override val connectionPool: HikariConnectionPool = HikariConnectionPool("root", "", "jdbc:h2:mem:test")

    override fun start(promise: Promise<Void>) {
        super.start(promise)
        val credentialsRepository: CredentialsRepository = CredentialsRepositoryImpl(database!!)
        val authenticationService: AuthenticationService = AuthenticationServiceImpl(credentialsRepository)
        // Run all services
        vertx.deployVerticle(AuthenticationVerticle(authenticationService))

    }

    override fun setupRoutes(): Router {
        val routing = Routes(eventBus!!)
        return Router.router(vertx).apply {
            route().handler(BodyHandler.create())
            route().handler(HttpErrorMiddleware())
            post("/$API_VERSION/authenticate").handler { ctx ->
                GlobalScope.launch {
                    routing.authenticate(ctx)
                }
            }
            post("/$API_VERSION/create").handler { ctx -> routing.createCredentials(ctx) }
        }
    }

    companion object {
        private const val API_VERSION: String = "v1"
    }
}
