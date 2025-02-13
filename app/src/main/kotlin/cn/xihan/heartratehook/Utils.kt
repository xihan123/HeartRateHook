package cn.xihan.heartratehook

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.alibaba.fastjson2.toJSONString
import com.drake.serialize.serialize.serial
import com.highcapable.yukihookapi.hook.factory.MembersType
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.param.HookParam
import com.highcapable.yukihookapi.hook.param.PackageParam
import kotlinx.serialization.json.Json
import java.io.Serializable
import kotlin.system.exitProcess

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

/**
 * 获取视图
 * @param [name] 名称
 * @param [isSuperClass] 是超一流
 * @return [T?]
 * @suppress Generate Documentation
 */
@Throws(NoSuchFieldException::class, IllegalAccessException::class)
inline fun <reified T : View> Any.getView(name: String, isSuperClass: Boolean = false): T? =
    getParam<T>(name, isSuperClass)

/**
 * 获取视图
 * @param [pairs] 对
 * @return [List<View>]
 * @suppress Generate Documentation
 */
fun Any.getViews(vararg pairs: Pair<String, Boolean> = arrayOf("name" to false)): List<View> =
    if (pairs.isEmpty()) emptyList()
    else pairs.mapNotNull { (name, isSuperClass) -> getParam<View>(name, isSuperClass) }

/**
 * 获取视图
 * @param [isSuperClass] 是超一流
 * @suppress Generate Documentation
 */
@Throws(NoSuchFieldException::class, IllegalAccessException::class)
inline fun <reified T : View> Any.getViews(isSuperClass: Boolean = false) =
    getParamList<T>(isSuperClass)

/**
 * 获取视图
 * @param [type] 类型
 * @param [isSuperClass] 是超一流
 * @return [ArrayList<Any>]
 * @suppress Generate Documentation
 */
@Throws(NoSuchFieldException::class, IllegalAccessException::class)
fun Any.getViews(type: Class<*>, isSuperClass: Boolean = false): ArrayList<Any> {
    val results = arrayListOf<Any>()
    val classes =
        if (isSuperClass) generateSequence(javaClass) { it.superclass }.toList() else listOf(
            javaClass
        )
    for (clazz in classes) {
        clazz.declaredFields.filter { type.isAssignableFrom(it.type) }.forEach { field ->
            field.isAccessible = true
            val value = field.get(this)
            if (type.isInstance(value)) {
                results += value as Any
            }
        }
    }
    return results
}

/**
 * 获取参数
 * @param [name] 名称
 * @param [isSuperClass] 是超一流
 * @return [T?]
 * @suppress Generate Documentation
 */
@Throws(NoSuchFieldException::class, IllegalAccessException::class)
inline fun <reified T> Any.getParam(name: String, isSuperClass: Boolean = false): T? {
    val queue = ArrayDeque<Class<*>>()
    var clazz: Class<*>? = if (isSuperClass) javaClass.superclass else javaClass
    while (clazz != null) {
        queue.add(clazz)
        clazz = clazz.superclass
    }
    while (queue.isNotEmpty()) {
        val currentClass = queue.removeFirst()
        try {
            val field = currentClass.getDeclaredField(name).apply { isAccessible = true }
            return field[this].safeCast<T>()
        } catch (_: NoSuchFieldException) {
            // Ignore and continue searching
        }
    }
    return null
}

/**
 * 获取参数列表
 * @param [isSuperClass] 是超一流
 * @return [ArrayList<T>]
 * @suppress Generate Documentation
 */
@Throws(NoSuchFieldException::class, IllegalAccessException::class)
inline fun <reified T> Any.getParamList(isSuperClass: Boolean = false): ArrayList<T> {
    val results = ArrayList<T>()
    val classes =
        if (isSuperClass) generateSequence(javaClass) { it.superclass }.toList() else listOf(
            javaClass
        )
    val type = T::class.java
    for (clazz in classes) {
        clazz.declaredFields.filter { type.isAssignableFrom(it.type) }.forEach { field ->
            field.isAccessible = true
            val value = field.get(this)
            if (type.isInstance(value)) {
                results += value as T
            }
        }
    }
    return results
}

fun ViewGroup.findViewsByType(viewClass: Class<*>): ArrayList<View> {
    val result = arrayListOf<View>()
    val queue = ArrayDeque<View>()
    queue.add(this)

    while (queue.isNotEmpty()) {
        val view = queue.removeFirst()
        if (viewClass.isInstance(view)) {
            result.add(view)
        }

        if (view is ViewGroup) {
            for (i in 0..<view.childCount) {
                queue.add(view.getChildAt(i))
            }
        }
    }

    return result
}

fun View.getName() = toString().substringAfter("/").replace("}", "")

fun Context.copyToClipboard(text: String) {
    val clipboardManager = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.setPrimaryClip(ClipData.newPlainText(null, text))
}

fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

infix fun Int.x(other: Int): ViewGroup.LayoutParams = ViewGroup.LayoutParams(this, other)

fun Context.toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

/**
 * 重新启动应用程序
 */
fun Activity.restartApplication() = packageManager.getLaunchIntentForPackage(packageName)?.let {
    finishAffinity()
    startActivity(intent)
    exitProcess(0)
}

object Utils {
    var BASE_URL: String? by serial()


    fun View.setOnClickListener(activity: Activity) {
        setOnClickListener {
            activity.showBaseUrlDialog()
        }
    }

    /**
     * 展示设置基础URL的对话框
     */
    fun Activity.showBaseUrlDialog() {
        var innerBaseUrl = BASE_URL ?: ""
        val editText = CustomEditText(
            context = this,
            value = innerBaseUrl,
            hint = "请输入服务器地址"
        ) {
            innerBaseUrl = it
        }

        alertDialog {
            title = "设置服务器地址"
            message = "先确认后再重启应用"
            customView = editText
            okButton {
                if (innerBaseUrl.isBlank()) {
                    toast("服务器地址不能为空")
                } else {
                    if (!innerBaseUrl.endsWith("/")) {
                        innerBaseUrl += "/"
                    }
                    BASE_URL = innerBaseUrl
                    toast("设置成功")
                    restartApplication()
                }
            }

            negativeButton("重启应用") {
                restartApplication()
            }
            build()
            show()
        }
    }


}
