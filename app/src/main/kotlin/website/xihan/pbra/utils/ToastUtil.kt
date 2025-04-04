package website.xihan.pbra.utils

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * @项目名 : QDReaderHook
 * @作者 : MissYang
 * @创建时间 : 2025/2/25 16:58
 * @介绍 :
 */
object ToastUtil : KoinComponent {

    private val context by inject<Context>()
    private var toast: Toast? = null

    /** 取消吐司显示 */
    @JvmStatic
    fun cancel() {
        toast?.cancel()
    }

    /**
     * 显示吐司
     * @param msg 吐司内容
     * @param duration 吐司显示时长 0 短时间显示 1 长时间显示
     */
    @SuppressLint("ShowToast")
    private fun showToast(msg: CharSequence?, duration: Int) {
        msg ?: return
        toast?.cancel()
        mainThreadImmediate {
            toast = Toast.makeText(context.applicationContext, msg, duration)
            toast?.show()
        }
    }

    /**
     * 短时间显示的吐司
     * @param msg 吐司内容
     */
    fun show(@StringRes msg: Int) {
        showToast(context.getString(msg), 0)
    }

    /**
     * 短时间显示的吐司
     * @param msg 吐司内容
     */
    fun show(msg: CharSequence?) {
        showToast(msg, 0)
    }

    /**
     * 长时间显示的吐司
     * @param msg 吐司内容
     */
    fun longShow(@StringRes msg: Int) {
        longShow(context.getString(msg))
    }

    /**
     * 长时间显示的吐司
     * @param msg 吐司内容
     */
    private fun longShow(msg: CharSequence?) {
        showToast(msg, 1)
    }


}

