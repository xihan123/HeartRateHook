package cn.xihan.heartratehook

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Parcelable
import com.alibaba.fastjson2.toJSONString
import com.highcapable.yukihookapi.hook.factory.MembersType
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.param.HookParam
import com.highcapable.yukihookapi.hook.param.PackageParam
import kotlinx.serialization.json.Json
import java.io.Serializable

/**
 * @项目名 : HeartRateHook
 * @作者 : MissYang
 * @创建时间 : 2024/8/7 20:27
 * @介绍 :
 */

val kJson = Json {
    isLenient = true
//    prettyPrint = true
    encodeDefaults = true
    ignoreUnknownKeys = true
    coerceInputValues = true
}

/**
 * 安全转换
 * @see [T?]
 * @return [T?]
 * @suppress Generate Documentation
 */
inline fun <reified T> Any?.safeCast(): T? = this as? T

/**
 * 打印错误日志
 * @suppress Generate Documentation
 */
fun String.loge() {
    if (BuildConfig.DEBUG) {
        YLog.error(msg = this, tag = YLog.Configs.tag)
    }
}

/**
 * 打印错误日志
 * @suppress Generate Documentation
 */
fun Throwable.loge() {
    if (BuildConfig.DEBUG) {
        YLog.error(msg = this.message ?: "未知错误", tag = YLog.Configs.tag)
    }
}

/**
 * 获取版本代码
 * @param [packageName] 包名
 * @see [Int]
 * @return [Int]
 */
fun Context.getVersionCode(packageName: String): Int = try {
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
            packageManager.getPackageInfo(
                packageName, PackageManager.GET_SIGNING_CERTIFICATES
            ).longVersionCode.toInt()
        }

        Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
            packageManager.getPackageInfo(packageName, 0).longVersionCode.toInt()
        }

        else -> {
            packageManager.getPackageInfo(packageName, 0).versionCode
        }
    }
} catch (e: Throwable) {
    e.loge()
    0
}

/**
 * 打印调用堆栈
 * @suppress Generate Documentation
 */
fun String.printCallStack() {
    val stringBuilder = StringBuilder()
    stringBuilder.appendLine("----className: $this ----")
    stringBuilder.appendLine("Dump Stack: ---------------start----------------")
    val ex = Throwable()
    val stackElements = ex.stackTrace
    stackElements.forEachIndexed { index, stackTraceElement ->
        stringBuilder.appendLine("Dump Stack: $index: $stackTraceElement")
    }
    stringBuilder.appendLine("Dump Stack: ---------------end----------------")
    stringBuilder.toString().loge()
}

/**
 * 打印调用堆栈
 * @suppress Generate Documentation
 */
fun Any.printCallStack() {
    this.javaClass.name.printCallStack()
}

/**
 * 查找方法和打印
 * @param [className] 类名
 * @param [printCallStack] 打印调用堆栈
 * @param [printType] 打印类型
 * @suppress Generate Documentation
 */
fun PackageParam.findMethodAndPrint(
    className: String,
    printCallStack: Boolean = false,
    printType: MembersType = MembersType.METHOD,
    classLoader: ClassLoader? = appClassLoader
) {
    when (printType) {
        MembersType.METHOD -> {
            className.toClass(classLoader).method().hookAll().after {
                print(printCallStack)
            }
        }

        MembersType.CONSTRUCTOR -> {
            className.toClass(classLoader).constructor().hookAll().after {
                print(printCallStack)
            }
        }

        else -> {
            with(className.toClass(classLoader)) {
                (method().giveAll() + constructor().giveAll()).hookAll {
                    after {
                        print(printCallStack)
                    }
                }
            }
        }
    }
}

private fun HookParam.print(
    printCallStack: Boolean = false
) {
    val stringBuilder = StringBuilder().apply {
        append("---类名: ${instanceClass?.name} 方法名: ${method.name}\n")
        if (args.isEmpty()) {
            append("无参数\n")
        } else {
            args.forEachIndexed { index, any ->
                append("参数${any?.javaClass?.simpleName} ${index}: ${any.mToString()}\n")
            }
        }
        result?.let { append("---返回值: ${it.mToString()}") }
    }
    stringBuilder.toString().loge()
    if (printCallStack) {
        instance.printCallStack()
    }
}

/**
 * 打印参数
 * @return [String]
 * @suppress Generate Documentation
 */
fun Array<Any?>.printArgs(): String {
    val stringBuilder = StringBuilder()
    this.forEachIndexed { index, any ->
        stringBuilder.append("args[$index]: ${any.mToString()}\n")
    }
    return stringBuilder.toString()
}

/**
 * m到字符串
 * @return [String]
 * @suppress Generate Documentation
 */
fun Any?.mToString(): String = when (this) {
    is String, is Int, is Long, is Float, is Double, is Boolean -> "$this"
    is Array<*> -> this.joinToString(",")
    is ByteArray -> this.toHexString()//this.toString(Charsets.UTF_8)
    is Serializable, is Parcelable -> this.toJSONString()
    else -> {
        val list = listOf(
            "Entity",
            "Model",
            "Bean",
            "Result",
        )
        if (list.any { this?.javaClass?.name?.contains(it) == true }) this.toJSONString()
        else this?.toString() ?: "null"
    }
}

/**
 * 字节数组使用hex编码
 */
fun ByteArray.toHexString(): String {
    val sb = StringBuilder()
    forEach {
        sb.append(String.format("%02x", it))
    }
    return sb.toString()
}