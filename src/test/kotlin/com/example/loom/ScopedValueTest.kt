package com.example.loom

import org.junit.jupiter.api.Test
import java.util.concurrent.Executors
import java.util.concurrent.StructuredTaskScope

internal class ScopedValueTest {
    @Test
    fun testValues() {
        val scopedValue = ScopedValue.newInstance<String>()
        println("isBound: ${scopedValue.isBound}")
        val executor = Executors.newSingleThreadScheduledExecutor()
        ScopedValue.where(scopedValue, "token")
            .run {
                if (scopedValue.isBound) println("in_top_block ${scopedValue.get()}") // bound,token
                Thread.ofVirtual().unstarted {
                    if (scopedValue.isBound) println("in_virtual ${scopedValue.get()}") //not bound
                }
                executor.submit {
                    if (scopedValue.isBound) println("in_executor ${scopedValue.get()}") //not bound
                }
                ScopedValue.where(scopedValue, "newToken").run {
                    StructuredTaskScope.ShutdownOnFailure().use {
                        it.fork {
                            if (scopedValue.isBound) println("forked_virtual_thread ${scopedValue.get()}") //bound,newToken
                        }
                        it.join()
                    }
                }
                if (scopedValue.isBound) println("in_top_block ${scopedValue.get()}") // bound token
            }
    }

}
