package com.example.coroutine

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.util.concurrent.*


internal class TestConcurrent {
    @Test
    fun callbackTest() {
        val countDownLatch = CountDownLatch(3)
        getUserByNameAsync("111", { u ->
            getUserOrdersAsync(u.name, {
                println(it)
                countDownLatch.countDown()
            }) //耗时1s
            getUserAvatarAsync(u.name, {
                println(it)
                countDownLatch.countDown()
            }) //耗时1s
            countDownLatch.countDown()
        });
        countDownLatch.await()
    }

    private val executors: ExecutorService = Executors.newFixedThreadPool(5)

    @Test
    fun futureTest() {
        data class Result(
            val orders: List<Int>,
            val avatar: UserAvatar
        )
        executors.asCoroutineDispatcher()
        CompletableFuture.supplyAsync { getUserByName("111") }
            .thenApplyAsync {
                val orders = executors.submit(Callable { getUserOrders(it.name) })
                val avatar = executors.submit(Callable { getUserAvatar(it.name) })
                Result(orders.get(), avatar.get())
            }.thenApply {
                println(it)
            }.join()
    }

    @Test
    fun rxjava() {
        val tasks: BlockingQueue<Runnable> = LinkedBlockingQueue()
        Observable.fromCallable { getUserByName("111") }.flatMap { u ->
            val avatar = Observable.fromCallable { getUserAvatar(u.name) }.subscribeOn(Schedulers.io())
            val order = Observable.fromCallable { getUserOrders(u.name) }.subscribeOn(Schedulers.io())
            Observable.zip(avatar, order) { t1, t2 -> Pair(t1, t2) }
        }.subscribeOn(Schedulers.io())
            .observeOn(Schedulers.from { tasks.add(it) })
            .subscribe(
                {
                    println(it)
                },
                {
                    println(it)
                }
            )
        tasks.take().run()
    }

    @Test
    fun coroutine(): Unit {
        runBlocking {
            val user = getUserByIdSuspended("111")
            val avatar = async { getUserAvatarSuspended(user.name) }
            val orders = async { getUserOrdersSuspended(user.name) }
            println("avatar is ${avatar.await()}")
            println("orders is ${orders.await()}")
        }
    }

}