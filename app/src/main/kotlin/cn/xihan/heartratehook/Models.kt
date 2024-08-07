package cn.xihan.heartratehook

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @项目名 : HeartRateHook
 * @作者 : MissYang
 * @创建时间 : 2024/8/7 23:56
 * @介绍 :
 */
@Keep
@Serializable
data class Models(
    @SerialName("data") var `data`: String = "",
    @SerialName("dataDes") var dataDes: String = "",
    @SerialName("other") var other: String = ""
)

@Keep
@Serializable
data class HeartRateModel(
    @SerialName("data")
    var `data`: DataModel = DataModel(),
    @SerialName("measured_at")
    var measuredAt: Long = 0
) {
    @Keep
    @Serializable
    data class DataModel(
        @SerialName("heart_rate")
        var heartRate: Int = 0
    )
}