package website.xihan.pbra

import android.app.Activity
import android.app.Application
import android.app.Instrumentation
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import com.alibaba.fastjson2.toJSONString
import com.tencent.mmkv.MMKV
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.lazyModules
import website.xihan.pbra.utils.Ktor
import website.xihan.pbra.utils.Log
import website.xihan.pbra.utils.MiHealthPackage
import website.xihan.pbra.utils.MiHealthPackage.Companion.instance
import website.xihan.pbra.utils.Models
import website.xihan.pbra.utils.Settings.did
import website.xihan.pbra.utils.Settings.enableNonSportReport
import website.xihan.pbra.utils.appModule
import website.xihan.pbra.utils.callMethodOrNullAs
import website.xihan.pbra.utils.from
import website.xihan.pbra.utils.getIntFieldOrNull
import website.xihan.pbra.utils.getLongFieldOrNull
import website.xihan.pbra.utils.getObjectFieldOrNull
import website.xihan.pbra.utils.getObjectFieldOrNullAs
import website.xihan.pbra.utils.getViews
import website.xihan.pbra.utils.hookAfterMethod
import website.xihan.pbra.utils.hookAfterMethodByParameterTypes
import website.xihan.pbra.utils.hookBeforeMethod
import website.xihan.pbra.utils.kJson
import website.xihan.pbra.utils.safeCast
import website.xihan.pbra.utils.setOnClickListener
import java.lang.ref.WeakReference


/**
 * @项目名 : QDReaderHook
 * @作者 : MissYang
 * @创建时间 : 2025/1/4 15:42
 * @介绍 :
 */
class HookEntry : IXposedHookLoadPackage {


    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != "com.mi.health") return

        Instrumentation::class.java.hookBeforeMethod(
            "callApplicationOnCreate", Application::class.java
        ) { param ->
            val application = param.args[0].safeCast<Application>() ?: return@hookBeforeMethod
            MMKV.initialize(application)
            startKoin {
                androidLogger()
                androidContext(application)
                lazyModules(appModule)
            }
            Ktor.startHeartRateUpdates()

            Log.d("Mi Health process launched ...")
            Log.d("SDK: ${Build.VERSION.RELEASE}(${Build.VERSION.SDK_INT}); Phone: ${Build.BRAND} ${Build.MODEL}")

            MiHealthPackage(lpparam.classLoader)

            when {
                !lpparam.processName.contains(":") -> {
                    if (enableNonSportReport) {
                        Ktor.startPeriodicSending()
                        instance.apply {
                            deviceTabV4ViewModelClass?.hookAfterMethodByParameterTypes(
                                "syncDevice", 2
                            ) { param ->
                                param.args.first().callMethodOrNullAs<String>("getDid")?.let {
                                    if (did != it) {
                                        did = it
                                    }
                                }
                                val rawContact =
                                    param.thisObject.getObjectFieldOrNull("mDeviceContact")
                                if (mDeviceContact == null || mDeviceContact?.get() == null) {
                                    mDeviceContact = WeakReference(rawContact)
                                    Log.d("mDeviceContact: $mDeviceContact")
                                }
                            }

                            "com.xiaomi.fit.fitness.export.data.aggregation.DailyHrReport".from(
                                lpparam.classLoader
                            )?.hookAfterMethodByParameterTypes("setLatestHrRecord", 1) { param ->
                                param.args[0]?.let {
                                    val time = it.getLongFieldOrNull("time")
                                    if (time != null && time > System.currentTimeMillis() / 1000 - 60) {
                                        Ktor.apply {
                                            if (sportMode) {
                                                sportMode = false
                                                startPeriodicSending()
                                            }
                                        }
                                        it.getIntFieldOrNull("hr")?.let(Ktor::sendHeartRate)
                                    }
                                }
                            }
                        }
                    }

                    instance.apply {
                        baseSportVM?.hookAfterMethodByParameterTypes("onSuccess", 1) { param ->
                            Ktor.apply {
                                if (!sportMode) {
                                    sportMode = true
                                    stopPeriodicSending()
                                }
                            }
                            val sportingData =
                                param.args.firstOrNull() ?: return@hookAfterMethodByParameterTypes
                            val list = sportingData.getObjectFieldOrNullAs<List<*>>("list")
                                ?: return@hookAfterMethodByParameterTypes
                            val heartRateData = list.toJSONString()
                            val heartRateModel = kJson.decodeFromString<List<Models>>(heartRateData)
                            val heartRate =
                                heartRateModel.firstOrNull { it.dataDes == "心率" }?.data?.toIntOrNull()
                                    ?: return@hookAfterMethodByParameterTypes
                            Log.d("心率: $heartRate")
                            if (heartRate <= 0) return@hookAfterMethodByParameterTypes
                            Log.d("当前心率: $heartRate")
                            Ktor.sendHeartRate(heartRate)
                        }

                        aboutActivity?.hookAfterMethod("onCreate", Bundle::class.java) { param ->
                            val viewBinding = param.thisObject.getObjectFieldOrNull("mBinding")
                                ?: return@hookAfterMethod
                            val imageViews = viewBinding.getViews<ImageView>(isSuperClass = true)
                            if (imageViews.isEmpty()) {
                                Log.e("imageViews is empty")
                                return@hookAfterMethod
                            }
                            val activity =
                                param.thisObject.safeCast<Activity>() ?: return@hookAfterMethod
                            imageViews.forEach { imageView ->
                                imageView.setOnClickListener(activity)
                            }
                        }
                    }

                }

            }


        }
    }


    companion object {
        var mDeviceContact: WeakReference<Any>? = null
    }

}