package com.example.coroutine

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.concurrent.thread
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

data class UserData(val id: String, val name: String)
data class UserAvatar(val svg: String)

fun getUserByName(name: String): UserData {
    Thread.sleep(1000)//模仿网络请求
    return UserData(UUID.randomUUID().toString(), name)
}

fun getUserAvatar(userId: String): UserAvatar {
    Thread.sleep(1000)//模仿网络请求
    return UserAvatar("<svg></svg>")
}

fun getUserOrders(userId: String): List<Int> {
    Thread.sleep(1000)//模仿网络请求
    return listOf(1, 2, 3)
}

fun getUserByNameCsp(id: String, continuation: Continuation<Any?>) {

}

fun linear() {
    val user = getUserByName("111"); //耗时1s
    val order = getUserOrders(user.id) //耗时1s
    val userAvatar = getUserAvatar(user.id) //耗时1s
    // 其他业务逻辑 ...
}

fun <T> callbackStyle(task: () -> T, onSuccess: (T) -> Unit, onFailure: ((Exception) -> Unit)? = null) {
    thread {
        try {
            val invoke = task()
            onSuccess(invoke)
        } catch (e: Exception) {
            onFailure?.invoke(e)
        }
    }
}

fun getUserByNameAsync(id: String, onSuccess: (UserData) -> Unit, onFailure: ((Exception) -> Unit)? = null) =
    callbackStyle({ getUserByName(id) }, onSuccess, onFailure)

fun getUserAvatarAsync(id: String, onSuccess: (UserAvatar) -> Unit, onFailure: ((Exception) -> Unit)? = null) =
    callbackStyle({ getUserAvatar(id) }, onSuccess, onFailure)

fun getUserOrdersAsync(id: String, onSuccess: (List<Int>) -> Unit, onFailure: ((Exception) -> Unit)? = null) =
    callbackStyle({ getUserOrders(id) }, onSuccess, onFailure)


suspend fun <T> suspendedCall(task: () -> T) = suspendCoroutine { continuation ->
    thread {
        try {
            val invoke = task()
            continuation.resume(invoke)
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }
}

suspend fun getUserByIdSuspended(id: String): UserData = suspendedCall { getUserByName(id) }
suspend fun getUserAvatarSuspended(id: String): UserAvatar = suspendedCall { getUserAvatar(id) }
suspend fun getUserOrdersSuspended(id: String): List<Int> = suspendedCall { getUserOrders(id) }


fun test() = runBlocking {
    launch {
        val user = getUserByName("1111")
        launch {
            getUserAvatar(user.id)
        }
        launch {
            getUserOrders(user.id)
        }
    }
}
