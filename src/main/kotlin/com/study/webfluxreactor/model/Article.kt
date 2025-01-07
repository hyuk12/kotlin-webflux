package com.study.webfluxreactor.model

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

// r2dbc 는 영속성 컨텍스트라는 것이 없다. equals, hash code 를 어렵게 구현할 필요가 없다. data class 를 써도된다. jpa 의 one to many 나 many to one 을 쓸 수가 없다.
// 순수하게 db 와 mapping 만 한다.
@Table("TB_ARTICLE")
class Article(
    @Id
    var id: Long = 0,
    var title: String,
    var body: String? = null,
    var authorId: Long? = null,
): BaseEntity() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Article
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "id=$id, title='$title', body='$body', authorId=$authorId, ${super.toString()}"
}

open class BaseEntity(
    @CreatedDate
    var createdAt: LocalDateTime? = null,
    @LastModifiedDate
    var updatedAt: LocalDateTime? = null,
) {
    override fun toString(): String = "createdAt=$createdAt, updatedAt=$updatedAt"
}