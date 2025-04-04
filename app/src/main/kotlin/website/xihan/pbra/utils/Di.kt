package website.xihan.pbra.utils

import android.content.Context
import android.os.Build
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.lazyModule
import website.xihan.pbra.BuildConfig

/**
 * @项目名 : QDReadHook
 * @作者 : MissYang
 * @创建时间 : 2024/6/21 下午11:27
 * @介绍 :
 */
val appModule = lazyModule {
    singleOf(::provideHttpCookies)
    singleOf(::provideHttpClient)
}

private fun provideHttpCookies(context: Context) = CustomCookiesStorage(context)

private fun provideHttpClient(cookiesStorage: CustomCookiesStorage): HttpClient =
    HttpClient(OkHttp) {
        expectSuccess = true
        install(HttpCookies) {
            storage = cookiesStorage
        }
        install(ContentNegotiation) {
            json(kJson)
        }
        HttpResponseValidator {
            handleResponseExceptionWithRequest { exception, request ->
                Log.e("Error Url: ${request.url}, ${exception.message}")
                throw Throwable(exception.message ?: "Unknown error")
            }
        }
        install(ContentEncoding) {
            deflate(1.0F)
            gzip(0.9F)
        }

        install(UserAgent) {
            agent =
                "${BuildConfig.APPLICATION_ID}/${BuildConfig.VERSION_NAME}-(${BuildConfig.VERSION_CODE})|${
                    System.getProperty(
                        "http.agent"
                    ) ?: "(Android ${Build.VERSION.RELEASE})"
                }"
        }

        if (BuildConfig.DEBUG) {
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d(message)
                    }
                }
                level = LogLevel.ALL
            }
        }
    }

