package com.example.loom

import org.junit.jupiter.api.Test
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.StructuredTaskScope
import java.util.concurrent.StructuredTaskScope.ShutdownOnFailure
import java.util.function.Function


//https://openjdk.org/jeps/453
//错误短熔断
//取消传播
//清晰的
//可观测的
internal class StructuredConcurrencyTest {

    private fun <T> asFuture(task: Callable<T>): Callable<Future<T>> {
        return Callable {
            try {
                return@Callable CompletableFuture.completedFuture<T>(task.call())
            } catch (ex: java.lang.Exception) {
                return@Callable CompletableFuture.failedFuture<T>(ex)
            }
        }
    }

    fun <T> executeAll(tasks: List<Callable<T>>): List<Future<T>> {
        ShutdownOnFailure().use { scope ->
            val futures = tasks.stream()
                .map{ task: Callable<T> ->
                    asFuture(task)
                }.map {
                    scope.fork(it)
                }
                .toList()
            scope.join()
            return futures.stream().map {
                it.get()
            }.toList()
        }
    }

    @Throws(Exception::class)
    @Test
    fun test() {
     StructuredTaskScope.ShutdownOnFailure().use {
         val sub1 = it.fork {
             println("start subTask1")
             Thread.sleep(100)
             println("subTask1 throw Exception")
             throw Exception("subTask1")
         }
         val sub2 = it.fork {
             println("start subTask2")
             Thread.sleep(1000)
             println("end subTask2")
         }
         it.join().throwIfFailed()
     }

    }
}