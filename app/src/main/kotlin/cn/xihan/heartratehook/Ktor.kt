package cn.xihan.heartratehook

import android.content.Context
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


/**
 * @项目名 : HeartRateHook
 * @作者 : MissYang
 * @创建时间 : 2024/8/7 21:35
 * @介绍 :
 */
interface ApiService {

    @Headers("Content-Type: application/json")
    @POST("receive_data")
    suspend fun updateHeartRate(
        @Body heartRateModel: HeartRateModel
    ): String

}

object Ktor : KoinComponent {

    private val ctx: Context by inject()
    private val apiService: ApiService by inject()
    private val heartRateFlow = MutableSharedFlow<Int>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private suspend fun <T> callApi(block: suspend ApiService.() -> T) =
        runCatching { apiService.block() }
            .onFailure {
                toast("网络请求失败: ${it.message}")
            }


    /**
     * 开始监听来自 heartRateChannel 的心率更新
     * 更新进行去抖以避免快速连续更新
     * 每个心率值均由 updateHeartRate 函数处理
     */
    @OptIn(FlowPreview::class)
    fun startHeartRateUpdates() = CoroutineScope(Dispatchers.IO).launch {
        heartRateFlow
            .asSharedFlow()
            .debounce(250) // Debounce 等待 250ms 的非活动状态，然后再处理下一个值
            .catch {
                "协程流异常: ${it.message}".loge()
            }
            .onEach(::updateHeartRate)
            .conflate()
            .collect() //收集流
    }


    suspend fun updateHeartRate(heartRate: Int) = callApi {
        updateHeartRate(
            HeartRateModel(
                data = HeartRateModel.DataModel(heartRate),
                measuredAt = System.currentTimeMillis()
            )
        )
    }


    fun sendHeartRate(heartRate: Int) = runBlocking(Dispatchers.IO) {
        heartRateFlow.emit(heartRate)
    }

    private fun toast(msg: String) = ctx.applicationContext.toast(msg)
}