package cn.xihan.heartratehook

import com.alibaba.fastjson2.toJSONString
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.param.PackageParam
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.lazyModules
import org.luckypray.dexkit.DexKitBridge
import java.lang.reflect.Modifier


/**
 * @项目名 : HeartRateHook
 * @作者 : MissYang
 * @创建时间 : 2024/8/7 20:26
 * @介绍 :
 */
@InjectYukiHookWithXposed
class HookEntry : IYukiHookXposedInit {

    init {
        System.loadLibrary("dexkit")
    }

    override fun onInit() = YukiHookAPI.configs {
        YLog.Configs.apply {
            tag = "yuki"
            isEnable = BuildConfig.DEBUG
        }
    }

    override fun onHook() = YukiHookAPI.encase {
        if (packageName !in packageNames) return@encase
        loadApp(name = packageName) {
            onAppLifecycle {
                onCreate {
                    startKoin {
                        androidLogger()
                        androidContext(this@onCreate)
                        lazyModules(appModule)
                    }
                    Ktor.startHeartRateUpdates()
                }
            }
            DexKitBridge.create(appInfo.sourceDir).use { bridge ->
                when (packageName) {
                    "com.mi.health" -> hookMiLife(bridge)
                    "com.xiaomi.hm.health" -> hookZeppLife(bridge = bridge)
                }

            }
        }
    }

    companion object {
        val packageNames = listOf(
            "com.mi.health",
            "com.xiaomi.hm.health"
        )
    }

}

fun PackageParam.hookZeppLife(bridge: DexKitBridge) {
    val innerString = listOf("onHrChanged: status - ", "| hr - ")
    bridge.findClass {
        searchPackages("com.huami.moving.sport.ui.activity")
        matcher {
            methods {
                add {
                    usingStrings = innerString
                }
            }
        }
    }.firstNotNullOfOrNull { classData ->

        classData.findMethod {
            matcher {
                modifiers = Modifier.PUBLIC
                paramCount = 2
                returnType = "void"
            }
        }.firstNotNullOfOrNull { methodData ->
            methodData.className.toClass().method {
                name = methodData.methodName
                paramCount(methodData.paramTypeNames.size)
                returnType = UnitType
            }.hook().after {
                val heartRate = args[1].safeCast<Int>() ?: return@after
                if (heartRate < 0) return@after
                "当前心率: $heartRate".loge()
                Ktor.sendHeartRate(heartRate)
            }
        }

    }

}

fun PackageParam.hookMiLife(bridge: DexKitBridge) {
    "com.xiaomi.fitness.sport.viewmodel.BaseSportVM".toClass().method {
        name = "onSuccess"
        param("com.xiaomi.fitness.sport.bean.SportingData".toClass())
        returnType = UnitType
    }.hook().after {
        val sportingData = args.firstOrNull() ?: return@after
        val list = sportingData.getParam<List<*>>("list") ?: return@after
        val heartRateData = list.toJSONString()
        val heartRateModel = kJson.decodeFromString<List<Models>>(heartRateData)
        val heartRate =
            heartRateModel.firstOrNull { it.dataDes == "心率" }?.data?.toIntOrNull()
                ?: return@after
        if (heartRate < 0) return@after
        "当前心率: $heartRate".loge()
        Ktor.sendHeartRate(heartRate)
    }
}
