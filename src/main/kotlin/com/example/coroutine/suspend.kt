package com.example.coroutine

import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED

suspend fun test(id: String): String {
    println("suspend start")
    val user = getUserByIdSuspended(id)
    println("suspend end")
    return user.name
}

abstract class BaseContinuationImpl(public val completion: Continuation<Any?>) : Continuation<Any?> {
    override val context: CoroutineContext = completion.context
    override fun resumeWith(result: Result<Any?>) {
        var current = this
        var param = result
        while (true) {
            with(current) {
                val completion = completion
                val outcome: Result<Any?> = try {
                    val outcome = invokeSuspend(param)
                    if (outcome === COROUTINE_SUSPENDED) return
                    Result.success(outcome)
                } catch (exception: Throwable) {
                    Result.failure(exception)
                }
                if (completion is BaseContinuationImpl) {
                    current = completion
                    param = outcome
                } else {
                    completion.resumeWith(outcome)
                    return
                }
            }
        }
    }

    protected abstract fun invokeSuspend(result: Result<Any?>): Any?
}


fun testCsp(id: String, continuation: Continuation<Any?>): Any? {
    class continPass : BaseContinuationImpl(continuation) {
        var result: Any? = null
        var label = 0

        override fun invokeSuspend(r: Result<Any?>): Any? {
            result = r;
            label = label or Int.MIN_VALUE
            return testCsp("", this)
        }
    }

    val nextLevelContinuation =
        if (continuation is continPass)
            continuation
        else {
            continPass()
        }
    var res: Any?
    when (nextLevelContinuation.label) {
        0 -> { //没有执行
            println("suspended start")
            res = getUserByIdCsp(id, continuation);
            nextLevelContinuation.label = 1
            if (res == COROUTINE_SUSPENDED) {
                return COROUTINE_SUSPENDED
            }
        }

        1 -> {
            res = nextLevelContinuation.result
        }

        else -> throw RuntimeException()
    }
    val userData = res as UserData
    println("suspended end")
    return userData.name
}