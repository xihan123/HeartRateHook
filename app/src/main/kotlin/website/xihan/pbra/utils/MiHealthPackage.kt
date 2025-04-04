package website.xihan.pbra.utils

class MiHealthPackage(private val mClassLoader: ClassLoader) {

    init {
        instance = this
    }

    val baseSportVM by Weak { "com.xiaomi.fitness.sport.viewmodel.BaseSportVM" from mClassLoader }

    val aboutActivity by Weak { "com.xiaomi.fitness.about.AboutActivity" from mClassLoader }

    val deviceTabV4ViewModelClass by Weak { "com.xiaomi.fitness.device.manager.ui.tab.DeviceTabV4ViewModel" from mClassLoader }

    companion object {
        @Volatile
        lateinit var instance: MiHealthPackage
    }
}