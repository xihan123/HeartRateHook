package cn.xihan.heartratehook

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.lazyModule

/**
 * @项目名 : HeartRateHook
 * @作者 : MissYang
 * @创建时间 : 2024/8/7 21:28
 * @介绍 :
 */
val appModule = lazyModule {
    singleOf(::provideHttpClient)
    singleOf(::provideKtorfit)
    singleOf(::provideApiService)
}

private fun provideHttpClient(): HttpClient = HttpClient(OkHttp) {
    expectSuccess = true
    install(ContentNegotiation) {
        json(kJson)
    }
    HttpResponseValidator {
        handleResponseExceptionWithRequest { exception, request ->
            "Error Url: ${request.url}, ${exception.message}".loge()
            throw Throwable(exception.message ?: "Unknown error")
        }
    }
    install(ContentEncoding) {
        deflate(1.0F)
        gzip(0.9F)
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 10000
        connectTimeoutMillis = 10000
        socketTimeoutMillis = 10000
    }

//    install(Logging) {
//        logger = object : Logger {
//            override fun log(message: String) {
//                message.loge()
//            }
//        }
//        level = LogLevel.ALL
//    }
}

private fun provideKtorfit(httpClient: HttpClient) = Ktorfit.Builder()
    .httpClient(httpClient)
    .baseUrl("http://192.168.43.100:8000/") // TODO: 这里是你的服务器地址
    .build()

private fun provideApiService(ktorfit: Ktorfit): ApiService = ktorfit.createApiService()
