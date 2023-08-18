package com.example.springkotlin

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test

internal class KotlinCoroutineTest {
    @Test
    fun coroutineTest() {
//        val dispatcher = Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()
        val coroutineStart = System.currentTimeMillis()
        runBlocking {
            println("coroutine start @ $coroutineStart")
            repeat(THREAD_NUM) {
                launch {
                    delay(1000)
                    println(it)
                }
            }
        }
        with(CoroutineScope(Dispatchers.Main)) {
            launch {

            }
        }
        val coroutineEnd = System.currentTimeMillis()
        println("coroutine end @ $coroutineEnd")
        println("coroutine cost time ${coroutineEnd - coroutineStart}")
    }
}

