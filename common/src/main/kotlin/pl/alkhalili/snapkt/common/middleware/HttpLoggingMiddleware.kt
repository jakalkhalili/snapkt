package pl.alkhalili.snapkt.common.middleware

import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class HttpLoggingMiddleware : Handler<RoutingContext> {
    override fun handle(ctx: RoutingContext?) {
        if(ctx == null) {
            return
        }

        logger.info("${ctx.request().connection().localAddress()} -> ${ctx.request().uri()}")
        ctx.next()
    }

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(this::class.qualifiedName!!)
    }
}
