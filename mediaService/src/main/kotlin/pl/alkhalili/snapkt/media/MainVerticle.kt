package pl.alkhalili.snapkt.media

import io.vertx.core.Promise
import io.vertx.ext.web.Router
import pl.alkhalili.snapkt.common.HikariConnectionPool
import pl.alkhalili.snapkt.common.Microservice

class MainVerticle : Microservice() {
    override val connectionPool: HikariConnectionPool = HikariConnectionPool("root", "", "jdbc:h2:mem:test")

    override fun start(promise: Promise<Void>) {
        super.start(promise)
    }

    override fun setupRoutes(): Router {
        TODO("Implement routes")
    }

    companion object {
        private const val API_VERSION: String = "v1"
    }
}
