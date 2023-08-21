package com.example.loom

import org.junit.jupiter.api.Test
import java.util.concurrent.Callable

internal class ScopedValueTest {
    @Test
    fun testValues() {
        println("isBound: ${KEY.isBound}")
        ScopedValue.where(KEY, "token")
            .call {
                Callable {
                    if (KEY.isBound) {
                        println("get ${KEY.get()}")
                    }
                }
            }
    }

    companion object {
        @JvmStatic
        private val KEY = ScopedValue.newInstance<String>()
    }
}
