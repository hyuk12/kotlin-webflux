package com.study.webfluxreactor.service

import com.study.webfluxreactor.repository.ArticleRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Mono

@SpringBootTest
class ArticleServiceTest(
    @Autowired private val articleService: ArticleService,
    @Autowired private val articleRepository: ArticleRepository,
) {

    @Test
    fun createAndGet() {

        articleService.create(ReqCreate("title", body = "blabla")).flatMap { created ->
            articleService.get(created.id).doOnNext { read ->
                assertEquals(created.id, read.id)
                assertEquals(created.title, read.title)
                assertEquals(created.body, read.body)
                assertEquals(created.authorId, read.authorId)
                assertNotNull(read.createdAt)
                assertNotNull(read.updatedAt)
            }
        }.rollBack().block()
    }

    @Test
    fun getAll() {
        Mono.zip(
            articleService.create(ReqCreate("title1", body = "1", authorId = 1234)),
            articleService.create(ReqCreate("title2", body = "2")),
            articleService.create(ReqCreate("title matched", body = "3")),
        ).flatMap {
            articleService.getAll().collectList().doOnNext {
                assertEquals(3, it.size)
            }
        }.flatMap {
            articleService.getAll("matched").collectList().doOnNext {
                assertEquals(1, it.size)
            }
        }.rollBack().block()
    }

    @Test
    fun update() {
        val request = ReqUpdate(
            title = "updated !",
            body = "updated body!",
        )

        articleService.create(ReqCreate("title1", body = "1")).flatMap { new ->
            articleService.update(new.id, request).flatMap { articleService.get(new.id) }.doOnNext { updated ->
                assertEquals(request.title, updated.title)
                assertEquals(request.body, updated.body)
                assertEquals(new.authorId, updated.authorId)
            }
        }.rollBack().block()

    }

    @Test
    fun delete() {
        articleRepository.count().flatMap { prevSize ->
            articleService.create(ReqCreate("title", body = "blabla")).flatMap { new ->
                articleService.delete(new.id).then(Mono.defer {
                    articleRepository.count().doOnNext { sizeAfterDelete ->
                        assertEquals(prevSize, sizeAfterDelete)
                    }
                })
            }
        }.rollBack().block()
    }

    @Test
    fun deleteOnRollBackFunctionally() {
        articleRepository.count().flatMap { prevSize ->
            articleService.create(ReqCreate("title", body = "blabla")).flatMap { created ->
                Mono.zip(Mono.just(prevSize), Mono.just(created))
            }
        }.flatMap { context ->
            val created = context.t2
            articleService.delete(created.id).flatMap {
                Mono.zip(Mono.just(context.t1), Mono.just(created))
            }
        }.flatMap { context ->
            articleRepository.count().flatMap { currSize ->
                Mono.zip(Mono.just(context.t1), Mono.just(context.t2), Mono.just(currSize))
            }
        }.doOnNext {
            val prevSize = it.t1
            val currSize = it.t3
            assertEquals(prevSize, currSize)
        }.rollBack().block()
    }
}