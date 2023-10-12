package com.example.springkotlin.controller

import com.example.springkotlin.dto.User
import com.example.springkotlin.service.UserService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(private val userService: UserService) {
    @GetMapping("/users/{id}")
    suspend fun getUserById(@PathVariable id: Long): ResponseEntity<User> {
        val user = userService.findUserById(id)
        return if (user != null) ResponseEntity.ok(user)
        else ResponseEntity.notFound().build()
    }

    @GetMapping("/users")
    suspend fun listAll(): ResponseEntity<List<User>> = ResponseEntity.ok(userService.listAll())

    @GetMapping("/userFlow")
     fun testFlow():Flow<User> {
        return flow {
            repeat(100) {
                emit(it)
            }
        }.map {
            delay(100)
            User(it.toLong(), "fruit-$it","$it@eccom.com.cn")
        }
    }

}