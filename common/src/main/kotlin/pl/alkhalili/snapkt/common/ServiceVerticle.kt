package pl.alkhalili.snapkt.common

import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.eventbus.EventBus
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class ServiceVerticle<T>(protected val serviceName: String, protected open val service: T) :
    AbstractVerticle() {
    protected val logger: Logger = LoggerFactory.getLogger(this::class.qualifiedName)

    protected var eventBus: EventBus? = null

    override fun start(promise: Promise<Void>?) {
        eventBus = vertx.eventBus()
        logger.info("Service \"$serviceName\" initialized")
    }

    override fun stop(promise: Promise<Void>?) {
        logger.info("Service \"$serviceName\" stopped")
        promise?.complete()
    }
}
