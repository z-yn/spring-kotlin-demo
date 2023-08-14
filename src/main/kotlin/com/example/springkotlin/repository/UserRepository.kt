package com.example.springkotlin.repository

import com.example.springkotlin.dto.User
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.awaitOneOrNull
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitOneOrNull
import org.springframework.stereotype.Repository

@Repository
class UserRepository(
    private val client: DatabaseClient,
    private val template: R2dbcEntityTemplate
) {
    suspend fun findByIdUseClient(id: Long): User? =
        client
            .sql("SELECT * FROM users WHERE id = $id")
            .map { row ->
                User(
                    row.get("id") as Long,
                    row.get("username") as String,
                    row.get("email") as String,
                )
            }
            .awaitOneOrNull()

    suspend fun findByIdUseTemplate(id: Long): User? =
        template.select(User::class.java)
            .matching(
                Query.query(
                    Criteria.where("id").`is`(id)
                )
            )
            .awaitOneOrNull()
}