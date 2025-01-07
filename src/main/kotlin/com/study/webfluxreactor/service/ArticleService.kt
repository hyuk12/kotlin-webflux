package com.study.webfluxreactor.service

import com.study.webfluxreactor.exception.NoArticleException
import com.study.webfluxreactor.model.Article
import com.study.webfluxreactor.repository.ArticleRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class ArticleService(
    private val articleRepository: ArticleRepository,
) {

    @Transactional
    fun create(request: ReqCreate): Mono<Article> {
        return articleRepository.save(request.toArticle())
    }

    fun get(id: Long): Mono<Article> {
        return articleRepository.findById(id)
            .switchIfEmpty{ throw NoArticleException("id: $id") }
    }

    fun getAll(title: String? = null): Flux<Article> {
        return if (title.isNullOrEmpty()) articleRepository.findAll() else articleRepository.findAllByTitleContains(title)
    }

    fun update(id: Long, request: ReqUpdate): Mono<Article> {
        return articleRepository.findById(id)
            .switchIfEmpty { throw NoArticleException("id: $id") }
            .flatMap { article ->
                request.title?.let { article.title = it }
                request.body?.let { article.body = it }
                request.authorId?.let { article.authorId = it }
                articleRepository.save(article)
            }
    }

    fun delete(id: Long): Mono<Void> {
        return articleRepository.deleteById(id)
    }
}

data class ReqUpdate(
    val title: String? = null,
    var body: String? = null,
    var authorId: Long? = null,
) {

}

data class ReqCreate(
    val title: String,
    var body: String? = null,
    var authorId: Long? = null,
) {
    fun toArticle(): Article {
        return Article(
            title = this.title,
            body = this.body,
            authorId = this.authorId,
        )
    }
}
