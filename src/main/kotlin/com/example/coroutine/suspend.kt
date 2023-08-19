package com.example.coroutine

import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED

suspend fun test(id: String): String {
    println("suspend start")
    val user = getUserByIdSuspended(id)
    println("suspend end")
    return user.id
}

abstract class BaseContinuationImpl(private val completion: Continuation<Any?>) : Continuation<Any?> {
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


fun testCsp(id: String, cont: Continuation<Any?>): Any? {
    class  NextCont : BaseContinuationImpl(cont) {
        var result: Any? = null
        var label = 0
        override fun invokeSuspend(r: Result<Any?>): Any? {
            result = r;
            return testCsp("", this)
        }
    }
    val nextCont =
        if (cont is NextCont) cont else NextCont()

    val res: Any?
    when (nextCont.label) {
        0 -> { //没有执行
            println("suspended start")
            res = getUserByNameCsp(id, cont);
            nextCont.label = 1
            if (res == COROUTINE_SUSPENDED) {
                return COROUTINE_SUSPENDED
            }
        }
        1 -> { res = nextCont.result }
        else -> throw RuntimeException()
    }
    val userData = res as UserData
    println("suspended end")
    return userData.id
}