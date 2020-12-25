package pl.alkhalili.snapkt.identity.it

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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pl.alkhalili.snapkt.common.HikariConnectionPool
import pl.alkhalili.snapkt.common.Migration
import pl.alkhalili.snapkt.common.eventBusMessageOf
import pl.alkhalili.snapkt.common.extensions.sha256
import pl.alkhalili.snapkt.common.toJson
import pl.alkhalili.snapkt.identity.AuthenticationVerticle
import pl.alkhalili.snapkt.identity.domain.credentialsOf
import pl.alkhalili.snapkt.identity.domain.requests.AuthenticationRequest
import pl.alkhalili.snapkt.identity.domain.requests.CredentialsCreationRequest
import pl.alkhalili.snapkt.identity.domain.requests.TokenValidationRequest
import pl.alkhalili.snapkt.identity.domain.tokenOf
import pl.alkhalili.snapkt.identity.repository.CredentialsRepository
import pl.alkhalili.snapkt.identity.repository.CredentialsRepositoryImpl
import pl.alkhalili.snapkt.identity.repository.TokenRepository
import pl.alkhalili.snapkt.identity.repository.TokenRepositoryImpl
import pl.alkhalili.snapkt.identity.services.AuthenticationService
import pl.alkhalili.snapkt.identity.services.AuthenticationServiceImpl

@ExtendWith(VertxExtension::class)
class AuthenticationTests {
    // TODO: write "unhappy" paths
    companion object {
        private var database: Database? = null
        private var credentialsRepository: CredentialsRepository? = null
        private var tokenRepository: TokenRepository? = null
        private var authenticationService: AuthenticationService? = null

        private val bcrypt: BCryptPasswordEncoder = BCryptPasswordEncoder()

        @BeforeAll
        @JvmStatic
        fun `Connect to database and migrate`() {
            val connectionPool = HikariConnectionPool("root", "", "jdbc:h2:mem:identity.authenticationTests")
            database = Database.connect(connectionPool.dataSource())
            if (database == null) {
                fail("Cannot connect to database")
            }

            runBlocking {
                Migration.migrate(connectionPool.dataSource())
            }

            credentialsRepository = CredentialsRepositoryImpl(database!!)
            tokenRepository = TokenRepositoryImpl(database!!)
            authenticationService = AuthenticationServiceImpl(credentialsRepository!!, tokenRepository!!, bcrypt)
        }
    }

    @BeforeEach
    fun `Setup the Authentication verticle`(vertx: Vertx, ctx: VertxTestContext) {
        vertx.deployVerticle(AuthenticationVerticle(authenticationService!!), DeploymentOptions(), ctx.succeeding {
            ctx.completeNow()
        })
    }

    @Test
    fun `should create new credentials`(vertx: Vertx, ctx: VertxTestContext) {
        val testCredentialsRequest = CredentialsCreationRequest("shouldCreateCredentials", "shouldCreateCredentials", 0)
        vertx.eventBus().request<Boolean>(
            AuthenticationVerticle.ADDRESS,
            eventBusMessageOf(
                testCredentialsRequest,
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
    fun `should authenticate user`(vertx: Vertx, ctx: VertxTestContext) {
        val testCredentials = credentialsOf("shouldAuthenticateUser", "shouldAuthenticateUser", 0)
        credentialsRepository?.insert(testCredentials.copy(password = bcrypt.encode(testCredentials.password)))

        vertx.eventBus().request<Boolean>(
            AuthenticationVerticle.ADDRESS,
            eventBusMessageOf(
                AuthenticationRequest(
                    testCredentials.username,
                    testCredentials.password
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

    @Test
    fun `should validate token successfully`(vertx: Vertx, ctx: VertxTestContext) {
        val testCredentials = credentialsOf("shouldValidateToken", bcrypt.encode("shouldValidateToken"), 0)
        val credentials =
            credentialsRepository?.insert(testCredentials.copy(password = bcrypt.encode(testCredentials.password)))
        val testToken = tokenOf(credentials?.id!!)
        tokenRepository?.insert(testToken.copy(value = testToken.value.sha256()))

        vertx.eventBus().request<Int>(
            AuthenticationVerticle.ADDRESS,
            eventBusMessageOf(
                TokenValidationRequest(
                    testCredentials.username,
                    testToken.value
                ),
                TokenValidationRequest::class.java
            ).toJson()
        ) {
            if (!it.failed()) {
                ctx.completeNow()
            } else {
                ctx.failNow("Token invalid or expired. Current token status: ${it.cause().message}")
            }
        }
    }

}
