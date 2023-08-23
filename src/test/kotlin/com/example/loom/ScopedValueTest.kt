package com.example.loom

import org.junit.jupiter.api.Test
import java.util.concurrent.Callable
import java.util.concurrent.StructuredTaskScope

internal class ScopedValueTest {
    @Test
    fun testValues() {
        println("isBound: ${KEY.isBound}")
        ScopedValue.where(KEY, "token")
            .run {
                if (KEY.isBound) { //bound
                    println("get ${KEY.get()}")
                }
                Thread.ofVirtual().unstarted {
                    if (KEY.isBound) {//not bound
                        println("in_virtual get ${KEY.get()}")
                    }
                }
                StructuredTaskScope.ShutdownOnFailure().use {
                    it.fork {
                        if (KEY.isBound) { //bound
                            println("forked_virtual_thread get ${KEY.get()}")
                        }
                    }
                    it.join()
                }
            }
    }

    companion object {
        @JvmStatic
        private val KEY = ScopedValue.newInstance<String>()
    }
}
