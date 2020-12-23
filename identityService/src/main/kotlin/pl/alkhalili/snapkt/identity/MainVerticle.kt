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
import pl.alkhalili.snapkt.common.Migration
import pl.alkhalili.snapkt.identity.repository.CredentialsRepository
import pl.alkhalili.snapkt.identity.repository.CredentialsRepositoryImpl
import pl.alkhalili.snapkt.identity.services.AuthenticationService
import pl.alkhalili.snapkt.identity.services.AuthenticationServiceImpl
import javax.sql.DataSource

class MainVerticle : AbstractVerticle() {
    private var httpServer: HttpServer? = null
    private var database: Database? = null

    private var eventBus: EventBus? = null

    override fun start(promise: Promise<Void>) {
        val connectionPool = HikariConnectionPool("root", "", "jdbc:h2:mem:test")
        database = initializeDatabase(connectionPool.dataSource())
        if (database == null) {
            promise.fail("Failed to connect to database!")
            return
        }

        Migration.migrate(connectionPool.dataSource())

        // Initialize repositories and services
        val credentialsRepository: CredentialsRepository = CredentialsRepositoryImpl(database!!)
        val authenticationService: AuthenticationService = AuthenticationServiceImpl(credentialsRepository)

        // Launch event bus
        eventBus = vertx.eventBus()

        // Run all services
        vertx.deployVerticle(AuthenticationVerticle(authenticationService))

        // Create HTTP server
        httpServer = vertx.createHttpServer()
        httpServer?.requestHandler(setupRoutes())?.listen(8080) {
            if (it.succeeded()) {
                promise.complete()
                logger.info("The Identity service is now working!")
            } else {
                promise.fail(it.cause())
            }
        }
    }

    override fun stop(promise: Promise<Void>) = when (httpServer) {
        null -> promise.complete()
        else -> httpServer?.close(promise)
    }!!

    private fun initializeDatabase(dataSource: DataSource): Database {
        return Database.connect(dataSource)
    }

    private fun setupRoutes(): Router {
        return Router.router(vertx).apply {
            route().handler(BodyHandler.create())
            route().handler(HttpErrorMiddleware())
            post("/$API_VERSION/authenticate").handler { ctx ->
                GlobalScope.launch {
                    routing().authenticate(ctx)
                }
            }
            post("/$API_VERSION/create").handler { ctx -> routing().createCredentials(ctx) }
        }
    }

    private fun routing(): Routing = Routing(eventBus!!)

    companion object {
        private const val API_VERSION: String = "v1"
        private val logger: Logger = LoggerFactory.getLogger(this::class.qualifiedName)
    }
}
