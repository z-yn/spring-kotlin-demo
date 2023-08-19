package com.example.loom

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.util.concurrent.StructuredTaskScope


//https://openjdk.org/jeps/453
internal class StructuredConcurrencyTest {
    @Test
    fun awaitAll() {
        try {
            runBlocking {
                val a = async {
                    delay(2000)
                    if (Math.random() > 0) {
                        throw RuntimeException("")
                    }
                    return@async 1
                }

                launch {
                    while (true) {
                        println("I am B")
                        delay(1000)
                    }
                }
                val await = a.await()
                await
            }
        } catch (e: Exception) {
            println(e.stackTrace)
        }
        Thread.sleep(10000)
    }

    @Throws(Exception::class)
    @Test
    fun test() {
        val shutdownOnFailureScope = StructuredTaskScope.ShutdownOnFailure()
        val userName = shutdownOnFailureScope.fork {
            Thread.sleep(100)
            "userName"
        }
        val orderId = shutdownOnFailureScope.fork {
            Thread.sleep(100)
            1
        }
        shutdownOnFailureScope.join().throwIfFailed()
        println("userName is ${userName.get()}, orderId is ${orderId.get()}")
    }
}