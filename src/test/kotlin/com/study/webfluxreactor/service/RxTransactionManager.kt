package com.study.webfluxreactor.service

import com.study.webfluxreactor.service.RxTransactionManager.Companion.rxtx
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class RxTransactionManager: ApplicationContextAware {
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        rxtx = applicationContext.getBean(TransactionalOperator::class.java)
    }

    companion object {
        lateinit var rxtx: TransactionalOperator
            private set
    }
}

fun <T> Mono<T>.rollBack(): Mono<T> {
    val publisher = this
    return rxtx.execute { tx ->
        tx.setRollbackOnly()
        publisher
    }.next()
}

fun <T> Flux<T>.rollBack(): Flux<T> {
    val publisher = this
    return rxtx.execute { tx ->
        tx.setRollbackOnly()
        publisher
    }
}