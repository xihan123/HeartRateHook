package website.xihan.pbra.utils

import android.content.Context
import androidx.core.content.edit
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.CookieEncoding
import io.ktor.http.Url
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CustomCookiesStorage(
    context: Context
) : CookiesStorage {

    private val mutex = Mutex()

    private val prefs by lazy {
        context.getSharedPreferences("heart_rate_cookie_prefs", Context.MODE_PRIVATE)
    }

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        with(cookie) {
            if (name.isBlank()) return
        }
        mutex.withLock {
            Log.d("requestUrl: $requestUrl \ncookie: $cookie")
            prefs.edit {
                putString(cookie.name, cookie.value)
                apply()
            }
        }
    }


    override fun close() {}


    override suspend fun get(requestUrl: Url): List<Cookie> {
        val cookies = mutableListOf<Cookie>()
        mutex.withLock {
            prefs.all.forEach {
                val name = it.key
                val value = it.value as? String
                Log.d("name: $name \nvalue: $value")
                if (!name.isNullOrBlank() && !value.isNullOrBlank()) {
                    cookies.add(Cookie(name, value, CookieEncoding.RAW))
                }
            }
//            val name = prefs.getString("name", "")
//            val value = prefs.getString("value", "")
//            if (!name.isNullOrBlank() && !value.isNullOrBlank()) {
//                cookies.add(Cookie(name, value))
//            }
        }
        return cookies
    }
}