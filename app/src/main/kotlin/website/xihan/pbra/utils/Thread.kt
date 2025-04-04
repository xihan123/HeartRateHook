@file:JvmName("Thread")

package website.xihan.pbra.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import java.io.Closeable
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

typealias ErrorHandler = (Throwable) -> Unit

// 核心安全作用域
class SafeCoroutineScope private constructor(
    context: CoroutineContext,
    errorHandler: ErrorHandler?
) : CoroutineScope, Closeable {

    override val coroutineContext: CoroutineContext = context +
            SupervisorJob() +
            ExceptionHandler(errorHandler)

    override fun close() {
        coroutineContext.cancelChildren()
    }

    private class ExceptionHandler(
        private val handler: ErrorHandler?
    ) : AbstractCoroutineContextElement(CoroutineExceptionHandler),
        CoroutineExceptionHandler {

        override fun handleException(context: CoroutineContext, exception: Throwable) {
            if (exception is CancellationException) return
            handler?.invoke(exception)
            exception.printStackTrace()
        }
    }

    companion object {
        fun create(
            dispatcher: CoroutineDispatcher = Dispatchers.Default,
            errorHandler: ErrorHandler? = null
        ) = SafeCoroutineScope(dispatcher, errorHandler)
    }
}

fun ioScope(errorHandler: ErrorHandler? = null) =
    SafeCoroutineScope.create(Dispatchers.IO, errorHandler)

fun mainScope(errorHandler: ErrorHandler? = null) =
    SafeCoroutineScope.create(Dispatchers.Main, errorHandler)

fun mainImmediateScope(errorHandler: ErrorHandler? = null) =
    SafeCoroutineScope.create(Dispatchers.Main.immediate, errorHandler)

fun defaultScope(errorHandler: ErrorHandler? = null) =
    SafeCoroutineScope.create(Dispatchers.Default, errorHandler)

fun unconfinedScope(errorHandler: ErrorHandler? = null) =
    SafeCoroutineScope.create(Dispatchers.Unconfined, errorHandler)

fun mainThread(block: suspend CoroutineScope.() -> Unit) = mainScope().launch { block() }

fun mainThreadImmediate(block: suspend CoroutineScope.() -> Unit) =
    mainImmediateScope().launch { block() }

fun ioThread(block: suspend CoroutineScope.() -> Unit) =
    ioScope().launch { block() }


fun defaultThread(block: suspend CoroutineScope.() -> Unit) = defaultScope().launch { block() }

fun unconfinedThread(block: suspend CoroutineScope.() -> Unit) =
    unconfinedScope().launch { block() }

