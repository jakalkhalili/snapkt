package pl.alkhalili.snapkt.identity

import io.vertx.core.Promise
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pl.alkhalili.snapkt.common.HikariConnectionPool
import pl.alkhalili.snapkt.common.HttpErrorMiddleware
import pl.alkhalili.snapkt.common.Microservice
import pl.alkhalili.snapkt.identity.repository.CredentialsRepository
import pl.alkhalili.snapkt.identity.repository.CredentialsRepositoryImpl
import pl.alkhalili.snapkt.identity.repository.TokenRepository
import pl.alkhalili.snapkt.identity.repository.TokenRepositoryImpl
import pl.alkhalili.snapkt.identity.services.AuthenticationService
import pl.alkhalili.snapkt.identity.services.AuthenticationServiceImpl

class MainVerticle : Microservice() {
    override val connectionPool: HikariConnectionPool = HikariConnectionPool("root", "", "jdbc:h2:mem:test")

    override fun start(promise: Promise<Void>) {
        super.start(promise)
        val credentialsRepository: CredentialsRepository = CredentialsRepositoryImpl(database!!)
        val tokenRepository: TokenRepository = TokenRepositoryImpl(database!!)
        val authenticationService: AuthenticationService =
            AuthenticationServiceImpl(credentialsRepository, tokenRepository, BCryptPasswordEncoder())
        // Run all services
        vertx.deployVerticle(AuthenticationVerticle(authenticationService))

    }

    override fun setupRoutes(): Router {
        val routing = Routes(eventBus!!)
        return Router.router(vertx).apply {
            route().handler(BodyHandler.create())
            route().handler(HttpErrorMiddleware())
            post("/$API_VERSION/authenticate").handler { ctx ->
                GlobalScope.launch(vertx.dispatcher()) {
                    routing.authenticate(ctx)
                }
            }
            post("/$API_VERSION/create").handler { ctx ->
                GlobalScope.launch(vertx.dispatcher()) {
                    routing.createCredentials(ctx)
                }
            }
            post("/$API_VERSION/validateToken").handler { ctx ->
                GlobalScope.launch(vertx.dispatcher()) {
                    routing.validateToken(ctx)
                }
            }
        }
    }

    companion object {
        private const val API_VERSION: String = "v1"
    }
}
