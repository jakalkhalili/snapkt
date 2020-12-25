package pl.alkhalili.snapkt.common

import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.eventbus.EventBus
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import org.jetbrains.exposed.sql.Database
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.sql.DataSource

abstract class Microservice : AbstractVerticle() {
    private var httpServer: HttpServer? = null
    protected var database: Database? = null

    protected var eventBus: EventBus? = null

    abstract val connectionPool: HikariConnectionPool

    override fun start(promise: Promise<Void>) {
        database = initializeDatabase(connectionPool.dataSource())
        if (database == null) {
            promise.fail("Failed to connect to database!")
            return
        }

        Migration.migrate(connectionPool.dataSource())

        // Initialize repositories and services

        // Launch event bus
        eventBus = vertx.eventBus()

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

    abstract fun setupRoutes(): Router

    companion object {
        protected val logger: Logger = LoggerFactory.getLogger(this::class.qualifiedName)
    }
}
