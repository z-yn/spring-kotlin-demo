package com.example.loom

import org.junit.jupiter.api.Test
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.StructuredTaskScope


//https://openjdk.org/jeps/453
//错误短熔断
//取消传播
//清晰的
//可观测的
internal class StructuredConcurrencyTest {
    private fun <T> executeAll(tasks: List<Callable<T>>): List<Future<T>> {
        StructuredTaskScope.ShutdownOnFailure().use { scope ->
            val futures = tasks.stream()
                .map { task: Callable<T> ->
                    Callable {
                        try {
                            return@Callable CompletableFuture.completedFuture<T>(task.call())
                        } catch (ex: Exception) {
                            return@Callable CompletableFuture.failedFuture<T>(ex)
                        }
                    }
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

    @Test
    fun testShutdownOnFailure() {
        StructuredTaskScope.ShutdownOnFailure().use {
            it.fork {
                println("start subTask1")
                Thread.sleep(100)
                println("subTask1 throw Exception")
                throw Exception("subTask1")
            }
            it.fork {
                println("start subTask2")
                Thread.sleep(1000)
                println("end subTask2")
            }
            it.join().throwIfFailed()
        }
    }

    @Test
    fun testShutdownOnSuccess() {
        StructuredTaskScope.ShutdownOnSuccess<Int>().use {
            val task1 = it.fork {
                println("start subTask1")
                Thread.sleep(100)
                1
            }
            val task2 = it.fork {
                println("start subTask2")
                Thread.sleep(1000)
                2
            }
            println("state of task1 is ${task1.state()}")
            println("state of task2 is ${task2.state()}")
            it.join()
            println("result is ${it.result()}")
        }
    }

    @Test
    fun testNumbersTaskScope() {
        NumbersTaskScope().use {
            it.fork {
                val random = Math.random()
                println("task1 is $random")
                random
            }
            it.fork {
                val random = Math.random()
                println("task2 is $random")
                random
            }
            it.fork {
                val random = Math.random()
                println("task3 is $random")
                random
            }
            it.fork {
                val random = Math.random()
                println("task4 is $random")
                random
            }

            it.join()
            println("max is ${it.max()}")
            println("min is ${it.min()}")
        }
    }

    @Test
    fun testBatch() {
        this.executeAll(listOf(
            Callable {
                println("start subTask1")
                Thread.sleep(100)
                println("subTask1 throw Exception")
                throw Exception("subTask1")
            },
            Callable {
                println("start subTask2")
                Thread.sleep(50)
                println("end subTask2")
            },
            Callable {
                println("start subTask3")
                Thread.sleep(1000)
                println("end subTask3")
            }
        ))
    }

    class NumbersTaskScope : StructuredTaskScope<Double>() {
        private val taskResults = ArrayList<Double>()
        private val exceptions = ArrayList<Throwable>()
        override fun handleComplete(subtask: Subtask<out Double>) {
            when (subtask.state()) {
                Subtask.State.UNAVAILABLE -> throw Exception("state error")
                Subtask.State.SUCCESS -> taskResults.add(subtask.get())
                Subtask.State.FAILED -> exceptions.add(subtask.exception())
            }
        }

        fun max(): Double = taskResults.max()
        fun min(): Double = taskResults.min()
    }
}