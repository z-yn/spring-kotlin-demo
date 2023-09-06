package com.example.coroutine

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test

class TestCoroutineScope {
    @Test
    fun testRunBlocking(): Unit = runBlocking {
        launch {
            delay(100)
            println("task1 is finished")
        }
        launch {
            delay(500)
            println("task2 is finished")
        }
    }

    @Test
     fun testJoin():Unit = runBlocking{
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        with(coroutineScope) {
            launch {
                println("task1 start")
                delay(50)
                println("task1 finished")
            }
            launch {
                println("task2 start")
                try {
                    delay(200)
                }catch (e:Exception) {
                    println(e)
                }
                println("task2 finished")
            }
        }
        delay(100)
        coroutineScope.cancel(CancellationException("Cancelled"))
    }

    @Test
    fun testNewCoroutineScope(): Unit = runBlocking {
        launch {
            delay(100)
            println("task1 is finished")
        }
        //切出去但是会阻塞
        withContext(Dispatchers.Default) {
            launch {
                delay(5000)
                println("with_context task is finished")
            }
        }
        //新建Scope后，任务将脱离管控关系
        with(CoroutineScope(Dispatchers.Default)) {
            launch {
                delay(100)
                println("new_scope task is finished")
            }
        }
        delay(500)
        println("task2 is finished")
    }

}