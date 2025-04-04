package website.xihan.pbra.utils


import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.edit
import kotlinx.serialization.json.Json
import website.xihan.pbra.utils.Settings.baseUrl
import website.xihan.pbra.utils.Settings.enableNonSportReport
import website.xihan.pbra.utils.Settings.getReportIndexText
import website.xihan.pbra.utils.Settings.getSelectedBaseUrlText
import website.xihan.pbra.utils.Settings.reportIndex
import java.lang.ref.WeakReference
import kotlin.reflect.KProperty
import kotlin.system.exitProcess

val kJson = Json {
    isLenient = true
//    prettyPrint = true
    encodeDefaults = true
    ignoreUnknownKeys = true
    coerceInputValues = true
}

class Weak<T>(val initializer: () -> T?) {
    private var weakReference: WeakReference<T?>? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>) = weakReference?.get() ?: let {
        weakReference = WeakReference(initializer())
        weakReference
    }?.get()

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        weakReference = WeakReference(value)
    }
}

val systemContext: Context
    get() {
        val activityThread = "android.app.ActivityThread".findClassOrNull(null)
            ?.callStaticMethod("currentActivityThread")!!
        return activityThread.callMethodAs("getSystemContext")
    }


fun getPackageVersion(packageName: String) = try {
    systemContext.packageManager.getPackageInfo(packageName, 0).run {
        String.format("${packageName}@%s(%s)", versionName, getVersionCode(packageName))
    }
} catch (e: Throwable) {
    Log.e(e)
    "(unknown)"
}


fun getVersionCode(packageName: String) = try {
    @Suppress("DEPRECATION") systemContext.packageManager.getPackageInfo(packageName, 0).versionCode
} catch (e: Throwable) {
    Log.e(e)
    null
} ?: 6080000


inline fun <reified T> Any?.safeCast(): T? = this as? T

fun Context.copyToClipboard(text: String) {
    // 获取系统服务中的剪贴板管理器
    val clipboardManager = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    // 将文本创建为新的剪贴板数据，并设置到剪贴板中
    clipboardManager.setPrimaryClip(ClipData.newPlainText(null, text))
}

@Throws(NoSuchFieldException::class, IllegalAccessException::class)
inline fun <reified T : View> Any.getViews(isSuperClass: Boolean = false) =
    getParamList<T>(isSuperClass)

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

fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

infix fun Int.x(other: Int): ViewGroup.LayoutParams = ViewGroup.LayoutParams(this, other)

fun Activity.restartApplication() = packageManager.getLaunchIntentForPackage(packageName)?.let {
    finishAffinity()
    startActivity(intent)
    exitProcess(0)
}

fun ImageView.setOnClickListener(activity: Activity) {
    setOnClickListener {
        activity.showBaseUrlDialog()
    }
}

fun Activity.showBaseUrlDialog() {
    var innerBaseUrl = baseUrl
    var innerNonSportReport = enableNonSportReport
    val switch = CustomSwitchView(
        context = this,
        isChecked = innerNonSportReport,
        text = "非运动模式上报",
        onCheckedChangeListener = { isChecked, _ ->
            innerNonSportReport = isChecked
            enableNonSportReport = isChecked
        })

    var index = reportIndex
    val postSwitch = CustomSwitchView(
        context = this,
        isChecked = index == 0,
        text = getReportIndexText(),
        onCheckedChangeListener = { isChecked, view ->
            index = if (isChecked) 0 else 1
            reportIndex = index
            view.updateText(getReportIndexText())
            ToastUtil.show("切换成功")
        })
    val editText = CustomEditText(
        context = this, value = innerBaseUrl, hint = "填入完整的地址"
    ) {
        innerBaseUrl = it
    }

    val linearLayout = CustomLinearLayout(
        context = this, isAutoWidth = false, isAutoHeight = true
    ).apply {
        addView(switch)
        addView(postSwitch)
        addView(editText)
    }

    alertDialog {
        title = "设置服务器地址"
        message = buildString {
            appendLine("请输入完整的数据上报接口地址")
            appendLine("说明一下有两种方式上报方式,优先使用方法1")
            appendLine("1. 直链模式:直接填入完整的地址")
            appendLine("2. Cookie模式:需要登录")
            appendLine("非运动模式上报原理是反射调用同步数据的方法,首次需要在设备页面获取到反射类，固定1分钟上报一次,心率监测那里频率推荐1分钟/次")
        }

        customView = linearLayout
        okButton {
            if (innerBaseUrl.isBlank()) {
                ToastUtil.show("服务器地址不能为空")
            } else {
                baseUrl = innerBaseUrl
                ToastUtil.show("设置成功")
            }
        }
        neutralPressed("登录/注册") {
            showLoginOrRegisterDialog()
        }
        build()
        show()
    }
}

fun Activity.showLoginOrRegisterDialog() {
    var innerUserName = Settings.userName
    var innerUserPass = Settings.userPass
    var baseUrlIndex = Settings.baseUrlIndex
    val baseUrlSwitch = CustomSwitchView(
        context = this,
        isChecked = baseUrlIndex == 1,
        text = getSelectedBaseUrlText(),
        onCheckedChangeListener = { isChecked, view ->
            baseUrlIndex = if (isChecked) 1 else 0
            Settings.baseUrlIndex = baseUrlIndex
            view.updateText(getSelectedBaseUrlText())
            ToastUtil.show("切换成功")
        })
    val editText = CustomEditText(
        context = this, value = innerUserName, hint = "请输入账号3-50位"
    ) {
        innerUserName = it
    }
    val editText2 = CustomEditText(
        context = this, value = innerUserPass, hint = "请输入密码8-72位"
    ) {
        innerUserPass = it
    }
    val loginText = "登录"
    val registerText = "注册"

    val linearLayout = CustomLinearLayout(
        context = this, isAutoWidth = false, isAutoHeight = true
    ).apply {
        addView(baseUrlSwitch)
        addView(editText)
        addView(editText2)
    }

    alertDialog {
        title = "登录或注册"
        customView = linearLayout

        positiveButton(loginText) {
            if (innerUserName.isBlank() || innerUserPass.isBlank()) {
                ToastUtil.show("账号和密码不能为空")
            } else {
                Ktor.login(userName = innerUserName, userPass = innerUserPass, type = loginText)
            }
        }

        negativeButton("退出登录") {
            Settings.userName = ""
            Settings.userPass = ""
            Settings.isLogin = false
            context.getSharedPreferences("heart_rate_cookie_prefs", Context.MODE_PRIVATE)
                .edit { clear() }
            ToastUtil.show("退出登录成功")
        }
        neutralPressed(registerText) {
            if (innerUserName.isBlank() || innerUserPass.isBlank()) {
                ToastUtil.show("账号和密码不能为空")
            } else {
                Ktor.login(userName = innerUserName, userPass = innerUserPass, type = registerText)
            }
        }
        build()
        show()
    }

}