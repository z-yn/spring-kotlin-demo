package com.example.springkotlin.repository

import com.example.springkotlin.dto.User
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface CoroutineUserRepository : CoroutineCrudRepository<User, Long>