package com.example.coroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis

internal class TestKotlinxCoroutine {
    private suspend fun massiveRun(action: suspend () -> Unit) {
        val n = 100  // number of coroutines to launch
        val k = 1000 // times an action is repeated by each coroutine
        val time = measureTimeMillis {
            coroutineScope { // scope for coroutines
                repeat(n) {
                    launch {
                        repeat(k) { action() }
                    }
                }
            }
        }
        println("Completed ${n * k} actions in $time ms")
    }

    @Test
    fun block() {
        val lock = Mutex(false)
        var counter = 0
        runBlocking {
            withContext(Dispatchers.Default) {
                massiveRun {
                    lock.withLock {
                        counter++
                    }
                }
            }
            println("Counter = $counter")
        }
    }

    @Test
    fun testSequence() {
        val sequence = sequence<Int> {
            for (i in listOf(1, 2, 3)) {
                Thread.sleep(1000)
                yield(i * 100)
            }
        }
        sequence.forEach { println(it) }
    }

    private suspend fun performRequest(request: Int): String {
        delay(1000) // 模仿长时间运行的异步工作
        return "response $request"
    }
    @Test
    fun testFlow() {
        fun genFlow(): Flow<Int> = flow { // 流构建器
            for (i in 1..3) {
                delay(100) // 假装我们在这里做了一些有用的事情
                emit(i*100) // 发送下一个值
            }
        }

        runBlocking {
            genFlow().map { it*100 }
                .collect{ println(it) }

            listOf(1,2,3).asFlow()
                .conflate()
                .map { performRequest(it) }
                .collect{ println(it) }
        }
    }

    @Test
    fun testChannel() {
        val  chan = Channel<Int>(2)
        runBlocking {
            launch {
                repeat(10) {
                    delay(100)
                    println("send: $it")
                    chan.send(it) //
                }
                chan.close()
            }
            launch {
               for (y in chan) {
                   delay(200)
                   println("receive $y")
               }
            }
        }
    }
    @OptIn(ObsoleteCoroutinesApi::class)
    @Test
    fun testTickerChannel() = runBlocking<Unit> {
        val tickerChannel = ticker(delayMillis = 100, initialDelayMillis = 0) //创建计时器通道
        var nextElement = withTimeoutOrNull(1) { tickerChannel.receive() }
        println("Initial element is available immediately: $nextElement") // no initial delay

        nextElement = withTimeoutOrNull(50) { tickerChannel.receive() } // all subsequent elements have 100ms delay
        println("Next element is not ready in 50 ms: $nextElement")

        nextElement = withTimeoutOrNull(60) { tickerChannel.receive() }
        println("Next element is ready in 100 ms: $nextElement")

        // 模拟大量消费延迟
        println("Consumer pauses for 150ms")
        delay(150)
        // 下一个元素立即可用
        nextElement = withTimeoutOrNull(1) { tickerChannel.receive() }
        println("Next element is available immediately after large consumer delay: $nextElement")
        // 请注意，`receive` 调用之间的暂停被考虑在内，下一个元素的到达速度更快
        nextElement = withTimeoutOrNull(60) { tickerChannel.receive() }
        println("Next element is ready in 50ms after consumer pause in 150ms: $nextElement")

        tickerChannel.cancel() // 表明不再需要更多的元素
    }
}