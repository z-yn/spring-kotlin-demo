package com.example.springkotlin.service

import com.example.springkotlin.dto.User
import com.example.springkotlin.repository.CoroutineUserRepository
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class UserService(val repository: CoroutineUserRepository) {
    suspend fun findUserById(id: Long): User? = repository.findById(id)
    suspend fun listAll(): List<User> = repository.findAll().toList()
}