package com.study.webfluxreactor

import com.study.webfluxreactor.model.Article
import com.study.webfluxreactor.repository.ArticleRepository
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals

private val logger = KotlinLogging.logger {}

@SpringBootTest
class WebfluxReactorApplicationTests(
    @Autowired private val articleRepository: ArticleRepository,
) {

    @Test
    fun contextLoads() {
        val prevCount = articleRepository.count().block() ?: 0
        articleRepository.save(Article(title = "test", body = "test", authorId = 1)).block()
        val articles = articleRepository.findAll().collectList().block()

        articles?.forEach { logger.debug { it } }

        val currentCount = articleRepository.count().block() ?: 0
        assertEquals(prevCount + 1, currentCount)
    }

}
