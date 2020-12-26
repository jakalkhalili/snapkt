package pl.alkhalili.snapkt.common.middleware

import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext

class HttpErrorMiddleware : Handler<RoutingContext> {
    override fun handle(ctx: RoutingContext?) {
        val thrown = ctx?.failure()
        catchError(thrown)
        ctx?.next()
    }

    private fun catchError(throwable: Throwable?) {
        if (throwable != null) {
            println(throwable.message)
        }
    }
}
