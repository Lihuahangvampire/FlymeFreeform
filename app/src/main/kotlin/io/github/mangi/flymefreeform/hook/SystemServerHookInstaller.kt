package io.github.mangi.flymefreeform.hook

import android.util.Log
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.mangi.flymefreeform.platform.coloros.ColorOsFreeformCoordinator
import java.util.concurrent.atomic.AtomicBoolean

internal class SystemServerHookInstaller(
    private val module: XposedModule,
    private val configuration: ProcessConfiguration,
) {
    private val bound = AtomicBoolean(false)
    private val environment = ModuleEnvironmentState(configuration) { code, exception ->
        module.log(Log.WARN, TAG, code, exception)
    }

    fun install(classLoader: ClassLoader) {
        installResourcesFallback()
        OutsideTapCloseHookInstaller(module, configuration, environment).install(classLoader)
        HandleSwipeUpHookInstaller(module, configuration, environment).install(classLoader)
        try {
            val controllerClass = classLoader.loadClass(FLEXIBLE_TASK_CONTROLLER_CLASS)
            val systemReady = controllerClass.getDeclaredMethod("systemReady", Boolean::class.javaPrimitiveType)
            module
                .hook(systemReady)
                .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                .setId("flymefreeform.system.flexible_ready")
                .intercept { chain ->
                    val result = chain.proceed()
                    val controller = chain.thisObject
                    if (controller != null && bound.compareAndSet(false, true)) {
                        ColorOsFreeformCoordinator(
                            controller = controller,
                            classLoader = classLoader,
                            configuration = configuration,
                            environmentState = environment,
                            logger = { priority, code, throwable ->
                                module.log(priority, TAG, code, throwable)
                            },
                        ).start()
                    }
                    result
                }
            module.log(Log.INFO, TAG, "SYSTEM_FREEFORM_READY_HOOK_INSTALLED")
        } catch (exception: ReflectiveOperationException) {
            module.log(Log.WARN, TAG, "SYSTEM_FREEFORM_TARGET_UNAVAILABLE", exception)
        } catch (exception: LinkageError) {
            module.log(Log.WARN, TAG, "SYSTEM_FREEFORM_TARGET_LINKAGE_FAILED", exception)
        }
    }

    /**
     * @author bomo 资源漂移宽容：侧边栏混淆代码在无障碍/UI 查询里对不存在的资源 ID
     * 调 getText/getString 会抛 Resources$NotFoundException，且未被捕获时会把
     * system_server 主线程带崩（实测：打开自绘面板触发无障碍遍历即重启）。
     * 兜底为返回空串，保持 UI 逻辑继续跑。
     */
    private fun installResourcesFallback() {
        try {
            val resources = Class.forName("android.content.res.Resources")
            val getText = resources.getMethod("getText", Int::class.javaPrimitiveType)
            module
                .hook(getText)
                .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                .setId("flymefreeform.resources.gettext_fallback")
                .intercept { chain ->
                    try {
                        chain.proceed()
                    } catch (exception: android.content.res.Resources.NotFoundException) {
                        ""
                    }
                }
            module.log(Log.INFO, TAG, "RESOURCES_GETTEXT_FALLBACK_INSTALLED")
        } catch (exception: Throwable) {
            module.log(Log.WARN, TAG, "RESOURCES_GETTEXT_FALLBACK_UNAVAILABLE", exception)
        }
    }

    private companion object {
        const val TAG = "FlymeFreeform"
        const val FLEXIBLE_TASK_CONTROLLER_CLASS = "com.android.server.wm.FlexibleTaskController"
    }
}
