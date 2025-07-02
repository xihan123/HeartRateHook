package website.xihan.pbra.utils

import com.drake.serialize.serialize.annotation.SerializeConfig
import com.drake.serialize.serialize.serial


/**
 * @项目名 : QDReaderHook
 * @作者 : MissYang
 * @创建时间 : 2025/1/10 16:56
 * @介绍 :
 */
@SerializeConfig(mmapID = "heart_rate_hook")
object Settings {

    var isLogin by serial(false)
    var userName by serial("")
    var userPass by serial("")

    var baseUrl by serial("")

    // 获取状态
    fun getStatus() = !isLogin && baseUrl.isBlank()

    // 获取上报索引 0:直链 1:Cookie
    var reportIndex by serial(0)

    // 获取上报索引文本
    fun getReportIndexText() = if (reportIndex == 0) {
        "直链"
    } else {
        "Cookie"
    }

    // 选中基础URL 0:香港 1:cloudflare
    var baseUrlIndex by serial(0)
    const val HK_BASE_URL = "https://public-heart-rate-api.xihan.website"
    const val CLOUD_FLARE_BASE_URL = "https://public-heart-rate-api.xihan.lat"

    fun getSelectedBaseUrl(): String {
        return if (baseUrl.isNotBlank()) {
            baseUrl
        } else {
            if (baseUrlIndex == 0) HK_BASE_URL else CLOUD_FLARE_BASE_URL
        }
    }

    fun getSelectedBaseUrlText() = if (baseUrlIndex == 0) {
        "香港"
    } else {
        "CloudFlare"
    }

    var enableNonSportReport by serial(true)
    var did by serial("")
}

