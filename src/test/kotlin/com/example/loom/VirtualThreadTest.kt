@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")
package com.example.loom

import com.example.springkotlin.THREAD_NUM
import jdk.internal.vm.Continuation
import jdk.internal.vm.ContinuationScope
import jdk.internal.vm.ContinuationSupport
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

// https://openjdk.org/jeps/444


internal class VirtualThreadTest {

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
                if (it == 1) println("task $it @before_sleep: thread is ${Thread.currentThread()}")
                Thread.sleep(Duration.ofSeconds(1))
                if (it == 1) println("task $it @after_sleep: thread is ${Thread.currentThread()}")
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
        val threads = List(10) {
            Thread.ofVirtual().unstarted {
                if (1==it) println("before_sleep @ ${Thread.currentThread()}")
                Thread.sleep(Duration.ofSeconds(1))
                if (1==it) println("after_sleep @ ${Thread.currentThread()}")
            }
        }
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        val virtualThreadEnd = System.currentTimeMillis()
        println("virtual thread end @ $virtualThreadEnd")
        println("virtual thread cost time ${virtualThreadEnd - virtualThreadStart}")
    }

    @Test
    fun testContinuation() {
        println("Continuation  Support: ${ContinuationSupport.isSupported()}")
        val scope = ContinuationScope("myScope")
        val continuation = Continuation(scope) {
            println("Continuation running");
            Continuation.yield(scope);
            println("Continuation still running");
            Continuation.yield(scope);
            println("Continuation finished");

        }

        val step = AtomicInteger(1)
        while (!continuation.isDone) {
            println("run continuation ${step.getAndIncrement()}")
            continuation.run()
        }
    }

    //BaseVirtualThread --sealed VirtualThread, ThreadBuilders.BoundVirtualThread
    // park()
    // parkNanos()
    //unpark()

    // VirtualThread


    /*
   * Virtual thread state and transitions:
   *
   *      NEW -> STARTED         // Thread.start
   *  STARTED -> TERMINATED      // failed to start
   *  STARTED -> RUNNING         // first run
   *
   *  RUNNING -> PARKING         // Thread attempts to park
   *  PARKING -> PARKED          // cont.yield successful, thread is parked
   *  PARKING -> PINNED          // cont.yield failed, thread is pinned
   *
   *   PARKED -> RUNNABLE        // unpark or interrupted
   *   PINNED -> RUNNABLE        // unpark or interrupted
   *
   * RUNNABLE -> RUNNING         // continue execution
   *
   *  RUNNING -> YIELDING        // Thread.yield
   * YIELDING -> RUNNABLE        // yield successful
   * YIELDING -> RUNNING         // yield failed
   *
   *  RUNNING -> TERMINATED      // done
   */

    /**
     *
     *     //  // scheduler and continuation
     *    private final Executor scheduler;
     *     private final Continuation cont;
     *
     *      private final Runnable runContinuation;
     *
     *     // virtual thread state, accessed by VM
     *     private volatile int state;
     *
     *     // parking permit
     *     private volatile boolean parkPermit;
     *
     *     // carrier thread when mounted, accessed by VM
     *     private volatile Thread carrierThread;
     *
     *     // termination object when joining, created lazily if needed
     *     private volatile CountDownLatch termination;
     */

}

