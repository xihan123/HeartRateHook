package cn.xihan.heartratehook

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
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

    private val apiService: ApiService by inject()
    private val heartRateChannel = Channel<Int>(Channel.UNLIMITED)

    private suspend fun <T> apiService(
        block: suspend ApiService.() -> T,
    ): T = apiService.block()


    /**
     * 开始监听来自 heartRateChannel 的心率更新
     * 更新进行去抖以避免快速连续更新
     * 每个心率值均由 updateHeartRate 函数处理
     */
    @OptIn(FlowPreview::class)
    fun startHeartRateUpdates() {
        CoroutineScope(Dispatchers.IO).launch {
            heartRateChannel.consumeAsFlow()
                .debounce(250) // Debounce 等待 250ms 的非活动状态，然后再处理下一个值
                .onEach { heartRate ->
                    updateHeartRate(heartRate) // 处理每个心率值
                }
                .collect() //收集流
        }
    }

    suspend fun updateHeartRate(heartRate: Int) = apiService {
        updateHeartRate(
            HeartRateModel(
                data = HeartRateModel.DataModel(heartRate),
                measuredAt = System.currentTimeMillis()
            )
        )
    }


    fun sendHeartRate(heartRate: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            heartRateChannel.send(heartRate)
        }
    }

}