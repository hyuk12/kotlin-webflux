package com.study.webfluxreactor.controller

import com.study.webfluxreactor.model.Article
import com.study.webfluxreactor.repository.ArticleRepository
import com.study.webfluxreactor.service.ReqCreate
import com.study.webfluxreactor.service.ReqUpdate
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.event.annotation.AfterTestClass
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.test.assertEquals

@SpringBootTest
class ArticleControllerTest(
    @Autowired private val context: ApplicationContext,
    @Autowired private val articleRepository: ArticleRepository,
) {
   val client = WebTestClient.bindToApplicationContext(context).build()

    @AfterTestClass
    fun clean() {
        articleRepository.deleteAll()
    }

    @Test
    fun create() {
      val request = ReqCreate("title 1", body = "it is r2dbc demo", 9978)
      client.post().uri("/article").accept(APPLICATION_JSON).bodyValue(request).exchange()
      .expectStatus().isCreated
      .expectBody()
      .jsonPath("$.title").isEqualTo(request.title)
      .jsonPath("$.body").isEqualTo(request.body!!)
      .jsonPath("$.authorId").isEqualTo(request.authorId!!)
    }

    @Test
    fun get() {
      val request = ReqCreate("title 1", body = "it is r2dbc demo", 9978)
      val created = client.post().uri("/article").accept(APPLICATION_JSON).bodyValue(request).exchange()
       .expectBody(Article::class.java).returnResult().responseBody!!
      val read = client.get().uri("/article/${created.id}").accept(APPLICATION_JSON).exchange()
       .expectStatus().isOk
       .expectBody(Article::class.java).returnResult().responseBody!!

      assertEquals(created.id, read.id)
      assertEquals(created.title, read.title)
      assertEquals(created.body, read.body)
      assertEquals(created.authorId, read.authorId)
      assertEquals(created.createdAt, read.createdAt)
      assertEquals(created.updatedAt, read.updatedAt)
    }

    @Test
    fun getAll() {
       repeat(5) { i ->
        val request = ReqCreate("title $i", body = "it is r2dbc demo", i.toLong())
        client.post().uri("/article").accept(APPLICATION_JSON).bodyValue(request).exchange()
       }

       client.post().uri("/article").accept(APPLICATION_JSON).bodyValue(
         ReqCreate("title matched", "it is r2dbc demo")
       ).exchange()

        val cnt = client.get().uri("/article/all").accept(APPLICATION_JSON).exchange()
            .expectStatus().isOk
            .expectBody(List::class.java)
            .returnResult().responseBody?.size ?: 0

        assertTrue(cnt > 0)

        client.get().uri("/article/all?title=matched").accept(APPLICATION_JSON).exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
    }

    @Test
    fun update() {
       val created = client.post().uri("/article").accept(APPLICATION_JSON)
        .bodyValue(ReqCreate("title 1", body = "it is r2dbc demo", 9978)).exchange()
        .expectBody(Article::class.java)
        .returnResult().responseBody!!

       client.put().uri("/article/${created.id}").accept(APPLICATION_JSON)
        .bodyValue(ReqUpdate(authorId = 7))
        .exchange()
        .expectBody()
        .jsonPath("$.authorId").isEqualTo(7)
    }

    @Test
    fun delete() {
     val prevCount = getArticleSize()
     val created = client.post().uri("/article").accept(APPLICATION_JSON)
        .bodyValue(ReqCreate("title 1", body = "it is r2dbc demo", 9978)).exchange()
        .expectBody(Article::class.java)
        .returnResult().responseBody!!

     client.delete().uri("/article/${created.id}").accept(APPLICATION_JSON).exchange()
     val currentCount = getArticleSize()
     assertEquals(prevCount, currentCount)
    }

    private fun getArticleSize(): Int {
     return client.get().uri("/article/all").accept(APPLICATION_JSON).exchange()
      .expectStatus().isOk
      .expectBody(List::class.java)
      .returnResult().responseBody?.size ?: 0
    }
}