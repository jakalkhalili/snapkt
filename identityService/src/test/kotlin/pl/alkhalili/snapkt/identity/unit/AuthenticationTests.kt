package pl.alkhalili.snapkt.identity.unit

import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.fail
import pl.alkhalili.snapkt.common.HikariConnectionPool
import pl.alkhalili.snapkt.common.Migration
import pl.alkhalili.snapkt.common.eventBusMessageOf
import pl.alkhalili.snapkt.common.toJson
import pl.alkhalili.snapkt.identity.AuthenticationVerticle
import pl.alkhalili.snapkt.identity.domain.AuthenticationRequest
import pl.alkhalili.snapkt.identity.domain.CredentialsCreationRequest
import pl.alkhalili.snapkt.identity.repository.CredentialsRepository
import pl.alkhalili.snapkt.identity.repository.CredentialsRepositoryImpl
import pl.alkhalili.snapkt.identity.services.AuthenticationService
import pl.alkhalili.snapkt.identity.services.AuthenticationServiceImpl

@ExtendWith(VertxExtension::class)
class AuthenticationTests {
    companion object {
        private var database: Database? = null
        private var credentialsRepository: CredentialsRepository? = null
        private var authenticationService: AuthenticationService? = null

        @BeforeAll
        @JvmStatic
        fun `Connect to database and create schema`() {
            val connectionPool = HikariConnectionPool("root", "", "jdbc:h2:mem:test")
            database = Database.connect(connectionPool.dataSource())
            if (database == null) {
                fail("Cannot connect to database")
            }

            runBlocking {
                Migration.migrate(connectionPool.dataSource())
            }

            credentialsRepository = CredentialsRepositoryImpl(database!!)
            authenticationService = AuthenticationServiceImpl(credentialsRepository!!)
        }
    }

    @BeforeEach
    fun `Setup the Authentication verticle`(vertx: Vertx, ctx: VertxTestContext) {
        vertx.deployVerticle(AuthenticationVerticle(authenticationService!!), DeploymentOptions(), ctx.succeeding {
            ctx.completeNow()
        })
    }

    @Test
    fun `Try to create new credentials`(vertx: Vertx, ctx: VertxTestContext) {
        vertx.eventBus().request<Boolean>(
            AuthenticationVerticle.ADDRESS,
            eventBusMessageOf(
                CredentialsCreationRequest(
                    "test",
                    "test123",
                    1000000
                ),
                CredentialsCreationRequest::class.java
            ).toJson()
        ) {
            if (it.result().body()) {
                ctx.completeNow()
            } else {
                ctx.failNow("Credentials weren't created")
            }
        }
    }

    @Test
    fun `Try to authenticate user`(vertx: Vertx, ctx: VertxTestContext) {
        vertx.eventBus().request<Boolean>(
            AuthenticationVerticle.ADDRESS,
            eventBusMessageOf(
                CredentialsCreationRequest(
                    "testa",
                    "test123",
                    1000000
                ),
                CredentialsCreationRequest::class.java
            ).toJson()
        ) {
            if (it.result().body()) {
                ctx.completeNow()
            } else {
                ctx.failNow("Credentials weren't created")
            }
        }

        vertx.eventBus().request<Boolean>(
            AuthenticationVerticle.ADDRESS,
            eventBusMessageOf(
                AuthenticationRequest(
                    "testa",
                    "test123"
                ),
                AuthenticationRequest::class.java
            ).toJson()
        ) {
            if (it.result().body()) {
                ctx.completeNow()
            } else {
                ctx.failNow("Failed to authenticate user!")
            }
        }
    }
}
