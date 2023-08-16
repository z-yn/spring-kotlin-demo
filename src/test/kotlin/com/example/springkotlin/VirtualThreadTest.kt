package com.example.springkotlin

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

private const val THREAD_NUM = 10_0000

// https://openjdk.org/jeps/444
class VirtualThreadTest {
    
        @Test
    fun threadTest() {
        val scheduledExecutor = Executors.newSingleThreadScheduledExecutor()
        val threadStart = System.currentTimeMillis()
        println("thread start @ $threadStart")
        repeat(THREAD_NUM) {
            scheduledExecutor.schedule({ println(it) }, 1, TimeUnit.SECONDS)
        }
        scheduledExecutor.close()
        val threadEnd = System.currentTimeMillis()
        println("thread end @ $threadEnd")
        println("thread cost time ${threadEnd - threadStart}")
    }

    @Test
    fun virtualThreadExecutorTest() {
        val newVirtualThreadPerTaskExecutor = Executors.newVirtualThreadPerTaskExecutor()
        val virtualThreadStart = System.currentTimeMillis()
        println("virtual thread executor start @ $virtualThreadStart")
        repeat(THREAD_NUM) {
            newVirtualThreadPerTaskExecutor.submit {
                Thread.sleep(Duration.ofSeconds(1))
            }
        }
        newVirtualThreadPerTaskExecutor.close()
        val virtualThreadEnd = System.currentTimeMillis()
        println("virtual thread executor end @ $virtualThreadEnd")
        println("virtual thread executor cost time ${virtualThreadEnd - virtualThreadStart}")
    }

    @Test
    fun virtualThreadTest() {
        val virtualThreadStart = System.currentTimeMillis()
        println("virtual thread start @ $virtualThreadStart")
        val threads = List(THREAD_NUM) {
            Thread.ofVirtual().unstarted {
                Thread.sleep(Duration.ofSeconds(1))
                println(it)
            }
        }
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        val virtualThreadEnd = System.currentTimeMillis()
        println("virtual thread end @ $virtualThreadEnd")
        println("virtual thread cost time ${virtualThreadEnd - virtualThreadStart}")
    }

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
        val coroutineEnd = System.currentTimeMillis()
        println("coroutine end @ $coroutineEnd")
        println("coroutine cost time ${coroutineEnd - coroutineStart}")
    }
}

