package website.xihan.pbra.utils

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import website.xihan.pbra.HookEntry.Companion.mDeviceContact
import website.xihan.pbra.utils.Settings.baseUrl
import website.xihan.pbra.utils.Settings.did
import website.xihan.pbra.utils.Settings.getSelectedBaseUrl
import website.xihan.pbra.utils.Settings.getStatus
import website.xihan.pbra.utils.Settings.isLogin
import website.xihan.pbra.utils.Settings.reportIndex
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds


/**
 * @项目名 : HeartRateHook
 * @作者 : MissYang
 * @创建时间 : 2024/8/7 21:35
 * @介绍 :
 */

object Ktor : KoinComponent {
    private val httpClient: HttpClient by inject()
    var sportMode = false
    private var periodicSendingJob: Job? = null
    private val heartRateChannel = Channel<Int>(
        Channel.BUFFERED,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    fun login(userName: String, userPass: String, type: String) = ioThread {
        Log.d("$type: $userName, $userPass, $baseUrl")
        httpClient.post("${getSelectedBaseUrl()}/${if (type == "登录") "login" else "register"}") {
            contentType(ContentType.Application.Json)
            setBody(LoginModel(userName, userPass))
        }.let { response ->
            val message = JSONObject(response.bodyAsText()).optString("message")
            Log.d("${type}结果: ${response.status}, $message")
            if (response.status.value == 200) {
                Settings.userName = userName
                Settings.userPass = userPass
                isLogin = true
                ToastUtil.show(message)
            } else {
                isLogin = false
                Log.e("${type}失败: ${response.status}, ${response.bodyAsText()}")
            }
        }
    }


    @OptIn(FlowPreview::class)
    fun startHeartRateUpdates() = ioThread {
        heartRateChannel
            .consumeAsFlow()
            .debounce(250) // Debounce 等待 250ms 的非活动状态，然后再处理下一个值
            .catch {
                Log.e("协程流异常: ${it.message}")
            }
            .onEach(::updateHeartRate)
            .conflate()
            .collect() //收集流
    }


    fun updateHeartRate(heartRate: Int) = ioThread {
        if (getStatus()) return@ioThread
        val url = if (reportIndex == 0) baseUrl else "${getSelectedBaseUrl()}/receive_data"
        Log.d("更新心率: $heartRate")
        httpClient.post(url) {
            contentType(ContentType.Application.Json)
            setBody(HeartRateModel(HeartRateModel.DataModel(heartRate)))
        }
    }


    fun sendHeartRate(heartRate: Int) = ioThread {
        Log.d("发送心率: $heartRate")
        heartRateChannel.send(heartRate)
    }


    fun startPeriodicSending() {
        periodicSendingJob?.cancel()
        periodicSendingJob = ioThread {
            try {
                Log.d("startPeriodicSending")
//                val uuid = UUID.randomUUID().toString()
                while (isActive) {
//                    Log.d("uuid: $uuid")
                    if (sportMode) break
                    if (mDeviceContact?.get() != null) {
                        mDeviceContact?.get()?.callMethod("syncData", did, false)
                    } else {
                        ToastUtil.show("获取设备失败,需要去设备页面确定连接正常并同步一次数据")
                    }
                    delay(1.minutes)
                }
            } catch (e: Exception) {
                Log.e("Periodic sending error: ${e.message}")
            }
        }
    }

    fun stopPeriodicSending() {
        periodicSendingJob?.cancel()
        periodicSendingJob = null
    }
}