package pl.alkhalili.snapkt.common.identity

import com.google.gson.Gson
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.client.WebClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

class IdentityClient(
    private val webClient: WebClient,
    private val identityServiceHost: String,
    private val identityServicePort: Int = 8080,
    private val identityServiceRequestUriPrefix: String = "/v1"
) {
    fun requireAuthorized(ctx: RoutingContext) {
        val token = ctx.request().getHeader("Authorization")
        if (token == null) {
            logger.warn(
                "Access to service without authorization header by ${
                    ctx.request().connection().localAddress().hostAddress()
                }"
            )
            throw AuthorizationMethodNotSupported("None")
        }

        // Yup, I know that's a wrong way to implement authentication, but I would like to make it as simple, as it can be.
        // So, we'll use HTTP Basic Auth with token as a password (for better security). In the nearest future, I'll implement Bearer Auth or maybe OAuth2.
        val splitAuthorizationToken = token.split(" ")
        val authorizationTokenMethod: String = splitAuthorizationToken[0]
        val authorizationTokenValue: String = Base64.getDecoder().decode(splitAuthorizationToken[1]).toString()

        // We support only Basic Authentication
        if (authorizationTokenMethod != "Basic") {
            logger.warn("Unsupported authentication header")
            throw AuthorizationMethodNotSupported(authorizationTokenMethod)
        }

        // Extract username and token
        val splitTokenValue = authorizationTokenValue.split(":")
        val username = splitTokenValue[0]
        val tokenValue = splitTokenValue[1]

        val req =
            webClient.post(identityServicePort, identityServiceHost, "$identityServiceRequestUriPrefix/validateToken")
                .sendJsonObject(
                    JsonObject(Gson().toJson(TokenValidationRequest(username, tokenValue)))
                )

        if (req.failed()) {
            throw AuthorizationFailureException(req.cause().message!!)
        }

        req.onComplete {
            if (it.result().statusCode() == 401) {
                throw AuthorizationFailureException("Not authorized")
            }
        }

        ctx.next()
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.qualifiedName)
    }
}
