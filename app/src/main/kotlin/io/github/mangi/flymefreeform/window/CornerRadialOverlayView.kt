package io.github.mangi.flymefreeform.window

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.RoundedCorner
import android.view.ViewConfiguration
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FitScreen
import androidx.compose.material.icons.rounded.OpenWith
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import top.yukonga.miuix.kmp.basic.Icon
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import io.github.mangi.flymefreeform.config.ModulePreferences
import io.github.mangi.flymefreeform.gesture.CornerSide
import io.github.mangi.flymefreeform.gesture.GesturePoint
import io.github.mangi.flymefreeform.gesture.RadialGeometry
import io.github.mangi.flymefreeform.gesture.RadialLayout
import io.github.mangi.flymefreeform.platform.coloros.AppCatalogSnapshot
import io.github.mangi.flymefreeform.platform.coloros.RadialAppEntry
import io.github.mangi.flymefreeform.ui.theme.CornerOverlayTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.anim.folmeSpring
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.squircle.addSquircleRect
import top.yukonga.miuix.kmp.squircle.isSquircleEnabled
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.PI
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.sin

@SuppressLint("ViewConstructor")
internal class CornerRadialOverlayView(
    context: Context,
    private val listener: Listener,
) : AbstractComposeView(context), LifecycleOwner, SavedStateRegistryOwner {
    interface Listener {
        fun onAppCommitted(entry: RadialAppEntry)

        fun onMorePanelRequested()

        /** @author bomo 自绘「更多」面板拖拽调整几何（宽/高百分比 + 偏移 dp）松手时回调，用于落盘同步设置页。 */
        fun onMorePanelGeometryChanged(widthPercent: Int, heightPercent: Int, offsetXDp: Int, offsetYDp: Int)

        fun onDismissRequested()

        fun onCleanupFailed(throwable: Throwable)
    }

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateController = SavedStateRegistryController.create(this)
    private val metricsState = mutableStateOf<AdaptiveOverlayMetrics?>(null)
    private val layoutState = mutableStateOf(EMPTY_LAYOUT)
    private val selectedIndexState = mutableIntStateOf(NO_SELECTION)
    private val panelModeState = mutableStateOf(false)
    private val backdropOnlyState = mutableStateOf(false)
    private val backdropAlpha = mutableFloatStateOf(1f)
    private var backdropAnimator: ValueAnimator? = null
    private val exitRequestState = mutableStateOf<ExitRequest?>(null)
    private val entryProgress = Animatable(0f)
    private val handoffEntryProgress = mutableFloatStateOf(0f)
    private val handoffLayoutState = mutableStateOf(EMPTY_LAYOUT)
    private val handoffMetricsState = mutableStateOf<RadialVisualMetrics?>(null)
    private var appliedWindowInsets: WindowInsets? = null
    private var catalog = AppCatalogSnapshot()
    private val radialClipPath = Path()
    private val radialImages = mutableStateOf<List<ImageBitmap>>(emptyList())
    private var panelImages: Map<android.content.ComponentName, ImageBitmap> = emptyMap()
    private var side = CornerSide.Right

    /**
     * @author bomo 本次呼出方位（只读）。「全部」面板需要据此决定靠左还是靠右锚定：
     * 原厂 `mIsLeft` 标志位在本模块自建面板路径下不可信（恒为默认值），
     * 唯一可靠的来源就是手势本身。
     */
    val invokedSide: CornerSide
        get() = side
    private var latestX = 0f
    private var latestY = 0f
    private var selectedIndex: Int? = null
    private var dismissing = false
    private var dismissNotified = false
    private var disposed = false
    private var radialRadiusScale = 1f
    /** @author bomo 选中圈描边厚度缩放系数（0.5 ~ 2.5），来自设置「选中圈厚度」。 */
    private var selectionRingWidthScale = 1f
    /** @author bomo 选中圈颜色 ARGB；[ModulePreferences.SELECTION_RING_COLOR_SYSTEM] = 跟随系统。 */
    private var selectionRingColorArgb = ModulePreferences.SELECTION_RING_COLOR_SYSTEM
    /** @author bomo 自绘「更多」面板自定义几何（默认值 = 自适应几何计算）。 */
    private var morePanelWidthPercent = ModulePreferences.DEFAULT_MORE_PANEL_WIDTH_PERCENT
    private var morePanelHeightPercent = ModulePreferences.DEFAULT_MORE_PANEL_HEIGHT_PERCENT
    private var morePanelCornerRadiusDp = ModulePreferences.DEFAULT_MORE_PANEL_CORNER_RADIUS_DP
    private var morePanelOffsetXDp = ModulePreferences.DEFAULT_MORE_PANEL_OFFSET_X_DP
    private var morePanelOffsetYDp = ModulePreferences.DEFAULT_MORE_PANEL_OFFSET_Y_DP
    /** @author bomo 悬停超时（毫秒；0 = 禁用自动悬停）。 */
    private var hoverTimeoutMs = ModulePreferences.DEFAULT_HOVER_TIMEOUT_MS
    /** @author bomo 内圈惯性转动阻尼系数（百分比，95 = 0.95/帧）。 */
    private var innerSpinDampingPercent = ModulePreferences.DEFAULT_INNER_SPIN_DAMPING_PERCENT
    /** @author bomo 内圈转动立体感开关（默认关）。 */
    private var innerSpinDepthEffect = ModulePreferences.DEFAULT_INNER_SPIN_DEPTH_EFFECT
    /** @author bomo 是否已进入悬停模式（外圈静止、内圈可滚动转动）。 */
    private var hoverMode = false
    /** @author bomo 内圈角度偏移（deg）：悬停模式下随手指转动、惯性衰减、吸附档位。 */
    private var innerAngleOffset = 0f
    /** @author bomo 内圈惯性转动角速度（deg/s）：松手记录、逐帧衰减。 */
    private var innerSpinVelocity = 0f
    /** @author bomo 内圈是否正在惯性滚动中。 */
    private var innerSpinAnimating = false
    /** @author bomo 悬停模式下内圈是否正在被拖动。 */
    private var innerSpinDragging = false
    /** @author bomo 悬停模式是否已在本次呼出内触发过进入提示（避免重复脉动）。 */
    private var hoverHintShown = false
    /** @author bomo 悬停拖动起点（用于区分点击与拖动）。 */
    private var innerSpinDownX = 0f
    private var innerSpinDownY = 0f
    /** @author bomo 上一次拖动点（用于计算角速度）。 */
    private var innerSpinLastX = 0f
    private var innerSpinLastY = 0f
    private var innerSpinLastTime = 0L
    /** @author bomo 内圈惯性/吸附动画器。 */
    private var innerSpinAnimator: ValueAnimator? = null
    private var innerSpinLastAnimTime = 0L
    /** @author bomo 悬停滚轮模式下，内圈 itemCenters 对应的 radialApps 下标（顺序一致）。 */
    private var innerSpinTargets: List<Int> = emptyList()
    /** @author bomo 进入悬停视觉提示的脉动幅度（0~0.12，一次性呼吸脉冲）。 */
    private val hoverHintPulse = mutableFloatStateOf(0f)
    /** @author bomo 定制前的自适应几何（拖拽调整的基准，避免基于上次定制结果反复缩放）。 */
    private var basePanelMetrics: AdaptiveOverlayMetrics? = null
    private var lastTickUptime = 0L
    private val timeout = Runnable(::requestDismiss)
    private val dismissFallback = Runnable(::completeRadialExit)
    /** @author bomo 悬停计时器：超时未选择 → 进入悬停模式（外圈静止、内圈可转动）。 */
    private val hoverTimeout = Runnable(::enterHoverMode)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateController.savedStateRegistry

    init {
        savedStateController.performAttach()
        savedStateController.performRestore(null)
        setViewTreeLifecycleOwner(this)
        setViewTreeSavedStateRegistryOwner(this)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        isFocusableInTouchMode = true
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
    }

    fun updateRadialAppearance(next: AppCatalogSnapshot) {
        if (disposed || dismissing || panelModeState.value) return
        // 只替换同一批应用的图像，不在滑选途中交换目标或重置入场动画。
        if (catalog.radialApps.map { it.component } != next.radialApps.map { it.component }) return
        radialImages.value = next.radialApps.map { it.icon.asImageBitmap() }
    }

    fun begin(
        side: CornerSide,
        catalog: AppCatalogSnapshot,
        x: Float,
        y: Float,
        /** @author bomo 扇形外圈 / 内圈半径缩放系数（1 = 原尺寸），来自设置「扇形半径」。 */
        radiusScale: Float = 1f,
        /** @author bomo 选中圈描边厚度缩放系数（1 = 默认），来自设置「选中圈厚度」。 */
        selectionRingWidthScale: Float = 1f,
        /** @author bomo 选中圈颜色 ARGB；[ModulePreferences.SELECTION_RING_COLOR_SYSTEM] = 跟随系统。 */
        selectionRingColorArgb: Int = ModulePreferences.SELECTION_RING_COLOR_SYSTEM,
        /** @author bomo 自绘「更多」面板自定义几何（宽/高百分比 + 圆角 dp + 偏移 dp；默认自适应）。 */
        morePanelWidthPercent: Int = ModulePreferences.DEFAULT_MORE_PANEL_WIDTH_PERCENT,
        morePanelHeightPercent: Int = ModulePreferences.DEFAULT_MORE_PANEL_HEIGHT_PERCENT,
        morePanelCornerRadiusDp: Int = ModulePreferences.DEFAULT_MORE_PANEL_CORNER_RADIUS_DP,
        morePanelOffsetXDp: Int = ModulePreferences.DEFAULT_MORE_PANEL_OFFSET_X_DP,
        morePanelOffsetYDp: Int = ModulePreferences.DEFAULT_MORE_PANEL_OFFSET_Y_DP,
        /** @author bomo 悬停超时（毫秒；0 = 禁用自动悬停）。 */
        hoverTimeoutMs: Int = ModulePreferences.DEFAULT_HOVER_TIMEOUT_MS,
        /** @author bomo 内圈惯性转动阻尼系数（百分比，95 = 0.95/帧）。 */
        innerSpinDampingPercent: Int = ModulePreferences.DEFAULT_INNER_SPIN_DAMPING_PERCENT,
        /** @author bomo 内圈转动立体感开关（默认关）。 */
        innerSpinDepthEffect: Boolean = ModulePreferences.DEFAULT_INNER_SPIN_DEPTH_EFFECT,
    ) {
        check(!isAttachedToWindow) { "Overlay must be initialized before it is attached" }
        this.side = side
        this.catalog = catalog
        radialRadiusScale = radiusScale.coerceIn(RADIAL_RADIUS_SCALE_MIN, RADIAL_RADIUS_SCALE_MAX)
        this.selectionRingWidthScale =
            selectionRingWidthScale.coerceIn(SELECTION_RING_WIDTH_SCALE_MIN, SELECTION_RING_WIDTH_SCALE_MAX)
        this.selectionRingColorArgb = selectionRingColorArgb
        this.morePanelWidthPercent =
            morePanelWidthPercent.coerceIn(
                ModulePreferences.MIN_MORE_PANEL_WIDTH_PERCENT,
                ModulePreferences.MAX_MORE_PANEL_WIDTH_PERCENT,
            )
        this.morePanelHeightPercent =
            morePanelHeightPercent.coerceIn(
                ModulePreferences.MIN_MORE_PANEL_HEIGHT_PERCENT,
                ModulePreferences.MAX_MORE_PANEL_HEIGHT_PERCENT,
            )
        this.morePanelCornerRadiusDp =
            morePanelCornerRadiusDp.coerceIn(
                ModulePreferences.MIN_MORE_PANEL_CORNER_RADIUS_DP,
                ModulePreferences.MAX_MORE_PANEL_CORNER_RADIUS_DP,
            )
        this.morePanelOffsetXDp =
            morePanelOffsetXDp.coerceIn(
                ModulePreferences.MIN_MORE_PANEL_OFFSET_X_DP,
                ModulePreferences.MAX_MORE_PANEL_OFFSET_X_DP,
            )
        this.morePanelOffsetYDp =
            morePanelOffsetYDp.coerceIn(
                ModulePreferences.MIN_MORE_PANEL_OFFSET_Y_DP,
                ModulePreferences.MAX_MORE_PANEL_OFFSET_Y_DP,
            )
        this.hoverTimeoutMs = ModulePreferences.coerceHoverTimeoutMs(hoverTimeoutMs)
        this.innerSpinDampingPercent =
            ModulePreferences.coerceInnerSpinDampingPercent(innerSpinDampingPercent)
        this.innerSpinDepthEffect = innerSpinDepthEffect
        // @author bomo 每次呼出重置悬停状态（不跨呼出残留转动角度/速度/提示）。
        hoverMode = false
        innerAngleOffset = 0f
        innerSpinVelocity = 0f
        innerSpinAnimating = false
        innerSpinDragging = false
        hoverHintShown = false
        latestX = x
        latestY = y
        radialImages.value = catalog.radialApps.map { entry -> entry.icon.asImageBitmap() }
        panelImages = catalog.panelApps.associate { entry -> entry.component to entry.icon.asImageBitmap() }
        selectedIndex = null
        selectedIndexState.intValue = NO_SELECTION
        handoffEntryProgress.floatValue = 0f
        handoffLayoutState.value = EMPTY_LAYOUT
        handoffMetricsState.value = null
        panelModeState.value = false
        exitRequestState.value = null
        dismissing = false
        dismissNotified = false
        val displayMetrics = resources.displayMetrics
        updateLayout(
            requestedWidth = displayMetrics.widthPixels,
            requestedHeight = displayMetrics.heightPixels,
            safeInsets = OverlaySafeInsets(),
        )
        removeCallbacks(timeout)
        postDelayed(timeout, GESTURE_TIMEOUT_MS)
        // @author bomo 启动悬停计时器：超时未选择 → 进入悬停模式（0 = 禁用）。
        removeCallbacks(hoverTimeout)
        if (hoverTimeoutMs > 0) {
            postDelayed(hoverTimeout, hoverTimeoutMs.toLong())
        }
    }

    fun updateGesture(x: Float, y: Float): Int? {
        latestX = x
        latestY = y
        if (panelModeState.value || dismissing) return selectedIndex
        removeCallbacks(timeout)
        postDelayed(timeout, GESTURE_TIMEOUT_MS)
        // @author bomo 悬停模式下手势语义由内圈转动接管；非悬停模式重置悬停计时器，
        // 让「持续滑动 = 仍在选择」不会误触发悬停。
        if (hoverMode) {
            handleInnerSpinDrag(x, y)
            return selectedIndex
        }
        removeCallbacks(hoverTimeout)
        if (hoverTimeoutMs > 0) {
            postDelayed(hoverTimeout, hoverTimeoutMs.toLong())
        }
        updateGestureFromLatestPoint()
        return selectedIndex
    }

    fun finishGesture() {
        // @author bomo 悬停模式松手语义不同：拖动 → 惯性吸附；点击 → 启动；否则保持悬停。
        if (hoverMode) {
            finishInnerSpinGesture()
            return
        }
        val selection = selectedIndex
        val target = selection?.let(::itemTarget)
        when {
            selection == null -> dismissAnimated()
            target == null -> showMorePanel()
            else -> dismissAnimated(catalog.radialApps[target])
        }
    }

    fun cancelGesture() {
        dismissAnimated()
    }

    fun dismissAnimated() = dismissAnimated(pendingCommit = null)

    fun disposeOverlay(): Throwable? {
        if (disposed) return null
        disposed = true
        backdropAnimator?.cancel()
        backdropAnimator = null
        innerSpinAnimator?.cancel()
        innerSpinAnimator = null
        removeCallbacks(timeout)
        removeCallbacks(dismissFallback)
        removeCallbacks(hoverTimeout)
        var failure: Throwable? = null
        try {
            if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            }
        } catch (exception: RuntimeException) {
            failure = mergeCleanupFailure(failure, exception)
        } catch (error: LinkageError) {
            failure = mergeCleanupFailure(failure, error)
        }
        try {
            disposeComposition()
        } catch (exception: RuntimeException) {
            failure = mergeCleanupFailure(failure, exception)
        } catch (error: LinkageError) {
            failure = mergeCleanupFailure(failure, error)
        }
        try {
            if (lifecycleRegistry.currentState != Lifecycle.State.DESTROYED) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            }
        } catch (exception: RuntimeException) {
            failure = mergeCleanupFailure(failure, exception)
        } catch (error: LinkageError) {
            failure = mergeCleanupFailure(failure, error)
        }
        return failure
    }

    @Composable
    override fun Content() {
        CornerOverlayTheme {
            val metrics = metricsState.value
            val layout = layoutState.value
            if (metrics != null) {
                OverlayContent(metrics = metrics, layout = layout)
            }
        }
    }

    @Composable
    private fun OverlayContent(
        metrics: AdaptiveOverlayMetrics,
        layout: RadialLayout,
    ) {
        val panelProgress = remember { Animatable(0f) }
        val panelContentProgress = remember { Animatable(0f) }
        val radialHandoffProgress = remember { Animatable(0f) }
        val exitProgress = remember { Animatable(0f) }
        val panelInputEnabled = remember { mutableStateOf(false) }
        val itemRings =
            remember(layout.itemCenters.size) {
                List(layout.itemCenters.size) { Animatable(0f) }
            }
        val animationsEnabled = remember { ValueAnimator.areAnimatorsEnabled() }

        val exitRequest = exitRequestState.value
        LaunchedEffect(panelModeState.value, exitRequest, backdropOnlyState.value) {
            if (panelModeState.value || exitRequest != null || backdropOnlyState.value) return@LaunchedEffect
            if (!animationsEnabled) {
                entryProgress.snapTo(1f)
            } else {
                entryProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(RadialEntryMotion.DURATION_MILLIS.toInt(), easing = LinearEasing),
                )
            }
        }
        LaunchedEffect(selectedIndexState.intValue, itemRings) {
            val selected = selectedIndexState.intValue
            coroutineScope {
                itemRings.forEachIndexed { index, ring ->
                    launch {
                        val target = if (index == selected) 1f else 0f
                        if (!animationsEnabled) {
                            ring.snapTo(target)
                        } else {
                            ring.animateTo(
                                targetValue = target,
                                animationSpec = tween(
                                    RadialEntryMotion.SELECTION_DURATION_MILLIS.toInt(),
                                    easing = { RadialEntryMotion.selectionProgress(it) },
                                ),
                            )
                        }
                    }
                }
            }
        }
        LaunchedEffect(panelModeState.value) {
            if (panelModeState.value) {
                panelInputEnabled.value = false
                if (!animationsEnabled) {
                    radialHandoffProgress.snapTo(1f)
                    panelProgress.snapTo(1f)
                    panelContentProgress.snapTo(1f)
                    panelInputEnabled.value = true
                } else {
                    coroutineScope {
                        launch {
                            radialHandoffProgress.animateTo(
                                targetValue = 1f,
                                animationSpec =
                                    tween(
                                        durationMillis = RadialHandoffMotion.DURATION_MILLIS.toInt(),
                                        easing = LinearEasing,
                                    ),
                            )
                        }
                        launch {
                            panelProgress.animateTo(
                                targetValue = 1f,
                                animationSpec =
                                    folmeSpring(
                                        damping = PANEL_EXPAND_DAMPING,
                                        response = PANEL_EXPAND_RESPONSE_SECONDS,
                                        visibilityThreshold = PANEL_VISIBILITY_THRESHOLD,
                                    ),
                            )
                        }
                        launch {
                            panelContentProgress.animateTo(
                                targetValue = 1f,
                                animationSpec =
                                    tween(
                                        durationMillis = PANEL_CONTENT_FADE_MILLIS,
                                        delayMillis = PANEL_CONTENT_DELAY_MILLIS,
                                        easing = LinearOutSlowInEasing,
                                    ),
                            )
                            panelInputEnabled.value = true
                        }
                    }
                }
            }
        }
        LaunchedEffect(exitRequest) {
            if (exitRequest != null) {
                exitProgress.snapTo(0f)
                exitProgress.animateTo(
                    targetValue = 1f,
                    animationSpec =
                        tween(
                            durationMillis = RadialDismissMotion.DURATION_MILLIS.toInt(),
                            easing = LinearEasing,
                        ),
                )
                completeRadialExit()
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            val radialLayout = if (panelModeState.value) handoffLayoutState.value else layout
            val radialMetrics =
                if (panelModeState.value) {
                    handoffMetricsState.value ?: metrics.radial
                } else {
                    metrics.radial
                }
            RadialCanvas(
                metrics = radialMetrics,
                layout = radialLayout,
                panelProgress = panelProgress,
                radialHandoffProgress = radialHandoffProgress,
                exitProgress = exitProgress,
                itemRings = itemRings,
                animationsEnabled = animationsEnabled,
            )
            if (panelModeState.value) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                awaitEachGesture {
                                    awaitFirstDown(requireUnconsumed = false)
                                    requestDismiss()
                                }
                            },
                )
                MoreAppsPanel(
                    metrics = metrics.panel,
                    panelProgress = panelProgress,
                    contentProgress = panelContentProgress,
                    inputEnabled = panelInputEnabled.value,
                    animationsEnabled = animationsEnabled,
                )
            }
        }
    }

    @Composable
    private fun RadialCanvas(
        metrics: RadialVisualMetrics,
        layout: RadialLayout,
        panelProgress: Animatable<Float, *>,
        radialHandoffProgress: Animatable<Float, *>,
        exitProgress: Animatable<Float, *>,
        itemRings: List<Animatable<Float, *>>,
        animationsEnabled: Boolean,
    ) {
        // @author bomo 选中圈颜色：设置「选中圈颜色」（跟随系统 = 动态取系统强调蓝，失败回退默认蓝）。
        val selectionRingColor = remember(selectionRingColorArgb) { resolveSelectionRingColor() }
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            if (backdropOnlyState.value) {
                drawRect(Color.Black, alpha = OverlayBackdrop.MAX_ALPHA * backdropAlpha.floatValue)
                return@Canvas
            }
            val panel =
                if (panelModeState.value && !animationsEnabled) {
                    1f
                } else {
                    panelProgress.value.coerceIn(0f, 1f)
                }
            val handoff =
                if (panelModeState.value && !animationsEnabled) {
                    1f
                } else {
                    radialHandoffProgress.value
                }
            val exitRequest = exitRequestState.value
            val entrySample = RadialEntryMotion.sample(
                when {
                    !animationsEnabled -> 1f
                    exitRequest != null -> exitRequest.entryProgress
                    panelModeState.value -> handoffEntryProgress.floatValue
                    else -> entryProgress.value
                },
            )
            val exitSample = RadialDismissMotion.sample(exitProgress.value)
            val handoffVisuals = RadialHandoffMotion.sample(
                frozenRevealProgress = entrySample.contentAlpha,
                handoffProgress = handoff,
                panelProgress = panel,
            )
            // 提早松手时从当前入场帧衔接退出，不跳到完整展开位置。
            val dismissalBlend = exitProgress.value.coerceIn(0f, 1f)
            val dismissal = if (exitRequest == null) null else RadialEntryVisuals(
                contentAlpha = entrySample.contentAlpha * exitSample.contentAlpha,
                radialProgress = exitSample.radialProgress + (entrySample.radialProgress - 1f) * (1f - dismissalBlend),
                horizontalOvershoot = exitSample.horizontalOvershoot + entrySample.horizontalOvershoot * (1f - dismissalBlend),
                rotationDegrees = exitSample.rotationDegrees + entrySample.rotationDegrees * (1f - dismissalBlend),
                iconScale = exitSample.iconScale + (entrySample.iconScale - 1f) * (1f - dismissalBlend),
            )
            val visual = dismissal ?: entrySample
            val scrimAlpha = if (panelModeState.value) handoffVisuals.scrimProgress else visual.contentAlpha
            drawRect(Color.Black, alpha = OverlayBackdrop.MAX_ALPHA * scrimAlpha)
            val alpha = visual.contentAlpha * if (panelModeState.value) handoffVisuals.contentAlpha else 1f
            if (alpha <= 0f) return@Canvas
            drawRadialItems(
                metrics = metrics,
                layout = layout,
                motion = visual,
                contentAlpha = alpha,
                contentScale = if (panelModeState.value) handoffVisuals.contentScale else 1f,
                itemRings = itemRings,
                selectionRingColor = selectionRingColor,
            )
        }
    }

    private fun DrawScope.drawRadialItems(
        metrics: RadialVisualMetrics,
        layout: RadialLayout,
        motion: RadialEntryVisuals,
        contentAlpha: Float,
        contentScale: Float,
        itemRings: List<Animatable<Float, *>>,
        selectionRingColor: Color,
    ) {
        val direction = if (layout.side == CornerSide.Left) 1f else -1f
        val overshoot = direction * RadialEntryMotion.HORIZONTAL_OVERSHOOT_DP * metrics.pixelsPerBaseDp * motion.horizontalOvershoot
        layout.itemCenters.forEachIndexed { index, destination ->
            val centerX = layout.origin.x + (destination.x - layout.origin.x) * motion.radialProgress + overshoot
            val centerY = layout.origin.y + (destination.y - layout.origin.y) * motion.radialProgress
            val scale = motion.iconScale * contentScale
            val ringProgress = itemRings.getOrNull(index)?.value ?: 0f
            // @author bomo 选中项图标随蓝圈同步放大：同一 ringProgress（130ms 选中动画）驱动，
            // 图标从原尺寸扩到 1.3 倍，圈始终包在放大后的图标外缘，形成「图标+圈一起长大」。
            val iconEnlarge =
                if (selectedIndexState.intValue == index) {
                    1f + (SELECTION_RING_ENLARGE_SCALE - 1f) * ringProgress
                } else {
                    1f
                }
            val target = itemTarget(index)
            val outerCount = outerApps().size
            val isInnerItem = target != null && target >= outerCount
            // @author bomo 悬停滚轮：进入提示呼吸脉动 + 可选立体感（窗口正面项放大）。
            val spinScale =
                if (isInnerItem && innerSpinEnabled()) {
                    (1f + hoverHintPulse.value) * innerSpinDepthScale(index - outerCount - 1)
                } else {
                    1f
                }
            val diameter =
                (if (isInnerItem) metrics.innerIconDiameter else metrics.iconDiameter) *
                    scale * iconEnlarge * spinScale
            rotate(motion.rotationDegrees, pivot = Offset(centerX, centerY)) {
                if (target == null) {
                    drawMoreItem(centerX, centerY, diameter, contentAlpha)
                } else {
                    radialImages.value.getOrNull(target)?.let { image ->
                        drawSystemImage(image, centerX, centerY, diameter, contentAlpha)
                    }
                }
                // @author bomo 描边宽度 = 默认基准（图标直径 6.8%）× 设置「选中圈厚度」缩放 × 选中动画。
                val strokeWidth =
                    metrics.selectionRingMaxWidth * selectionRingWidthScale * scale * ringProgress
                if (strokeWidth > 0f) {
                    // @author bomo 选中圈：内径全程连接图标外径，描边完全落在图标外侧。
                    // 修复：旧实现把已含 iconEnlarge 的直径再乘一次 SELECTION_RING_ENLARGE_SCALE
                    // （双重放大），p=1 时圈内缘落在 1.69× 图标半径处，与图标外径（1.3×）
                    // 之间留约 9dp 缝隙。现改为 innerEdge = 图标当前外径（随放大动画同步），
                    // 描边中心 = innerEdge + strokeWidth/2，圈始终紧贴放大后的图标。
                    val iconRadius = diameter / 2f
                    val restRadius = iconRadius / iconEnlarge
                    val innerEdge = restRadius + (iconRadius - restRadius) * ringProgress
                    val ringOuter = innerEdge + strokeWidth / 2f
                    drawCircle(
                        color = selectionRingColor,
                        radius = ringOuter,
                        center = Offset(centerX, centerY),
                        alpha = RadialEntryMotion.SELECTION_RING_ALPHA * contentAlpha,
                        style = Stroke(width = strokeWidth),
                    )
                }
            }
        }
    }

    /**
     * @author bomo 「更多」图标：2×2 宫格，三格为圆角空心方块、一格为放大镜（参考图样式）。
     * 格心间距 0.82× 格子（紧凑），方块圆角、描边加粗，替代原「三个点」。
     */
    private fun DrawScope.drawMoreItem(
        centerX: Float,
        centerY: Float,
        diameter: Float,
        alpha: Float,
    ) {
        drawCircle(
            color = Color.White,
            radius = diameter / 2f,
            center = Offset(centerX, centerY),
            alpha = alpha * PLATE_ALPHA,
        )
        val lineColor = Color(0xFF37373C)
        val unit = diameter / 2f // 半格（格心到边缘的距离基准）
        val compact = MORE_GRID_COMPACT_FRACTION // 格心间距系数：<1 让四格更紧凑
        for (row in 0..1) {
            for (col in 0..1) {
                val cx = centerX + (col - 0.5f) * unit * compact
                val cy = centerY + (row - 0.5f) * unit * compact
                if (row == 1 && col == 1) {
                    drawMagnifierGlyph(cx, cy, unit, lineColor, alpha)
                } else {
                    drawSquareGlyph(cx, cy, unit, lineColor, alpha)
                }
            }
        }
    }

    /** 圆角空心方块（宫格图标）：边长为格子的 46%，圆角为边长的 24%，描边加粗为格子的 11%。 */
    private fun DrawScope.drawSquareGlyph(
        cx: Float,
        cy: Float,
        unit: Float,
        color: Color,
        alpha: Float,
    ) {
        val side = unit * 0.46f
        drawRoundRect(
            color = color,
            topLeft = Offset(cx - side / 2f, cy - side / 2f),
            size = Size(side, side),
            cornerRadius = CornerRadius(side * 0.24f),
            alpha = alpha,
            style = Stroke(width = unit * 0.11f),
        )
    }

    /** 放大镜：镜片偏左上、手柄向右下 45°，与参考图一致；描边与方块同粗。 */
    private fun DrawScope.drawMagnifierGlyph(
        cx: Float,
        cy: Float,
        unit: Float,
        color: Color,
        alpha: Float,
    ) {
        val lensR = unit * 0.19f
        val lensCenter = Offset(cx - unit * 0.10f, cy - unit * 0.10f)
        drawCircle(
            color = color,
            radius = lensR,
            center = lensCenter,
            alpha = alpha,
            style = Stroke(width = unit * 0.11f),
        )
        val handleStart =
            Offset(lensCenter.x + lensR * 0.72f, lensCenter.y + lensR * 0.72f)
        val handleEnd = Offset(cx + unit * 0.20f, cy + unit * 0.20f)
        drawLine(
            color = color,
            start = handleStart,
            end = handleEnd,
            strokeWidth = unit * 0.12f,
            alpha = alpha,
        )
    }

    private fun DrawScope.drawSystemImage(
        image: ImageBitmap,
        centerX: Float,
        centerY: Float,
        size: Float,
        alpha: Float,
    ) {
        if (image.width <= 0 || image.height <= 0 || size <= 0f) return
        val aspectRatio = image.width.toFloat() / image.height
        val drawWidth = if (aspectRatio >= 1f) size else size * aspectRatio
        val drawHeight = if (aspectRatio >= 1f) size / aspectRatio else size
        val destinationSize = IntSize(drawWidth.roundToInt().coerceAtLeast(1), drawHeight.roundToInt().coerceAtLeast(1))
        radialClipPath.rewind()
        radialClipPath.addOval(Rect(centerX - size / 2f, centerY - size / 2f, centerX + size / 2f, centerY + size / 2f))
        clipPath(radialClipPath) {
            drawImage(
                image = image,
                dstOffset =
                    IntOffset(
                        (centerX - destinationSize.width / 2f).roundToInt(),
                        (centerY - destinationSize.height / 2f).roundToInt(),
                    ),
                dstSize = destinationSize,
                alpha = alpha,
                filterQuality = FilterQuality.High,
            )
        }
    }

    @Composable
    private fun MoreAppsPanel(
        metrics: PanelVisualMetrics,
        panelProgress: Animatable<Float, *>,
        contentProgress: Animatable<Float, *>,
        inputEnabled: Boolean,
        animationsEnabled: Boolean,
    ) {
        val density = androidx.compose.ui.platform.LocalDensity.current
        val squircleEnabled = isSquircleEnabled()
        val panelColor = MiuixTheme.colorScheme.surface.copy(alpha = PANEL_SURFACE_ALPHA)
        val labelColor = MiuixTheme.colorScheme.onSurface
        val bounds = metrics.bounds
        val width = with(density) { bounds.width.toDp() }
        val height = with(density) { bounds.height.toDp() }
        val cornerRadius = with(density) { metrics.cornerRadius.toDp() }
        val horizontalPadding = with(density) { metrics.contentHorizontalPadding.toDp() }
        val verticalPadding = with(density) { metrics.topPadding.toDp() }
        val cellHeight = with(density) { metrics.cellHeight.toDp() }
        // @author bomo 图标放大但不影响布局：仅放大图标绘制尺寸，格子/间距/行高不变。
        val iconSize = with(density) { metrics.iconDiameter.toDp() } * PANEL_ICON_SIZE_BOOST
        // @author bomo 图标与名称间距：基于图标中心到标签的偏移计算，再额外收窄 3dp 让名称更贴近图标。
        val labelGap = with(density) { (metrics.labelTopOffset - metrics.iconDiameter / 2f).toDp() } - 3.dp
        val labelSize = with(density) { metrics.labelTextSize.toSp() }
        // @author bomo 顶栏搜索：按应用名（含中文）或拼音首字母过滤。
        var query by remember { mutableStateOf("") }
        var showKeyboard by remember { mutableStateOf(false) }
        // @author bomo 系统输入法模式：搜索框点击后优先调用系统 IME，未激活时回退自绘键盘。
        var useSystemIme by remember { mutableStateOf(false) }
        // @author bomo 面板编辑模式：开启后显示灰色轮廓线 + 右下角缩放手柄 + 中心四向移动箭头。
        var panelEditMode by remember { mutableStateOf(false) }
        val filteredApps =
            remember(catalog.panelApps, query) {
                val q = query.trim()
                if (q.isEmpty()) {
                    catalog.panelApps
                } else {
                    val qc = q.uppercase()
                    catalog.panelApps.filter { entry ->
                        val label = entry.label
                        val initials = pinyinInitialsOf(label)
                        if (qc.length == 1) {
                            // @author bomo 单字符：严格匹配首字母（拼音首字母或英文首字母/前缀），
                            // 不再用「名称包含」以免混入首字母不符的应用（如输入 R 出现首字母非 R）。
                            initials.firstOrNull()?.toString() == qc ||
                                label.startsWith(q, ignoreCase = true)
                        } else {
                            // 多字符：拼音首字母序列前缀（如 "wx"→微信）/ 名称前缀 / 名称包含。
                            initials.startsWith(qc) ||
                                label.startsWith(q, ignoreCase = true) ||
                                label.contains(q, ignoreCase = true)
                        }
                    }
                }
            }
        // @author bomo 按首字母分组：字母（A-Z 后 #）→ 该字母下的全部应用，
        // 组内保持 filteredApps 相对顺序（最近使用在该字母内仍靠前）。
        val grouped: List<Pair<Char, List<RadialAppEntry>>> = remember(filteredApps) {
            val letters = LinkedHashSet<Char>()
            filteredApps.forEach { entry -> letters.add(initialOf(entry.label)) }
            letters.sortedWith { first, second ->
                when {
                    first == '#' && second != '#' -> 1
                    second == '#' && first != '#' -> -1
                    else -> first.compareTo(second)
                }
            }.map { letter -> letter to filteredApps.filter { initialOf(it.label) == letter } }
        }
        val letters = grouped.map { it.first }
        // 每个字母组标题行在网格中的 item 下标（累计前导标题行数；「最近使用」区占据 1 个标题 + 两行格子）。
        // @author bomo 偏移基准 = 实际渲染的最近使用 item 数（recentApps 不足 columns*RECENT_ROWS 时按实际），
        // 且 columns 变化（面板宽度调整）时重算——修复修改长宽后下次呼出索引与分组错位。
        val headerTargets: List<Int> = remember(grouped, query, catalog.recentApps, metrics.columns) {
            val list = mutableListOf<Int>()
            var acc = 0
            if (query.isEmpty() && catalog.recentApps.isNotEmpty()) {
                val recentCells = minOf(catalog.recentApps.size, metrics.columns * RECENT_ROWS)
                acc += recentCells + 1
            }
            grouped.forEach { (_, apps) ->
                list.add(acc)
                acc += apps.size + 1
            }
            list
        }
        // @author bomo 索引条数据：最近使用区用「*」符号（置于字母之前），点击滚动到最近区顶部。
        val starShown = query.isEmpty() && catalog.recentApps.isNotEmpty()
        val indexLetters = remember(starShown, letters) {
            if (starShown) listOf('*') + letters else letters
        }
        val indexTargets = remember(starShown, headerTargets) {
            if (starShown) listOf(0) + headerTargets else headerTargets
        }
        val gridState = rememberLazyGridState()
        val scope = rememberCoroutineScope()
        // @author bomo 索引高亮直接由滚动位置派生（每帧同步，无 LaunchedEffect 滞后）：
        // 当前「完整可见第一行」所属分组字母自动点亮；点击索引只触发滚动，高亮自动跟随。
        val firstVisibleItem by remember {
            derivedStateOf {
                val index = gridState.firstVisibleItemIndex
                val offset = gridState.firstVisibleItemScrollOffset
                if (offset > metrics.cellHeight / 2f) index + 1 else index
            }
        }
        val activeIndex by remember(indexLetters, indexTargets) {
            derivedStateOf {
                val first = firstVisibleItem
                var best = 0
                for (i in indexTargets.indices) {
                    if (indexTargets[i] <= first) best = i
                }
                best
            }
        }
        // @author bomo 空结果防越界：过滤为空时 indexLetters 为空，activeIndex 默认 0 会越界崩溃。
        val activeLetter = indexLetters.getOrNull(activeIndex) ?: '#'

        Box(
            modifier =
                Modifier
                    .absoluteOffset { IntOffset(bounds.left.roundToInt(), bounds.top.roundToInt()) }
                    .requiredSize(width = width, height = height)
                    .panelReveal(
                        progress = {
                            if (animationsEnabled) panelProgress.value else 1f
                        },
                        seedSize = metrics.iconDiameter,
                        cornerRadius = metrics.cornerRadius,
                        anchorOnLeft = side == CornerSide.Left,
                        squircleEnabled = squircleEnabled,
                    )
                    .squircleSurface(color = panelColor, cornerRadius = cornerRadius)
                    .then(
                        if (panelEditMode) {
                            Modifier.border(
                                width = 1.5.dp,
                                color = Color(0xFF9E9E9E).copy(alpha = 0.85f),
                                shape = RoundedCornerShape(cornerRadius),
                            )
                        } else {
                            Modifier
                        },
                    )
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            resetPanelTimeout()
                            waitForUpOrCancellation()
                        }
                    },
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // @author bomo 顶栏（仿系统智能侧边栏 UI）：关闭 + 搜索框。
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(PANEL_TOP_BAR_HEIGHT)
                            .padding(
                                start = horizontalPadding + 4.dp,
                                end = horizontalPadding,
                            ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // @author bomo 设置按钮（左）：单击切换编辑模式；双击恢复默认长宽及位置。
                    Box(
                        modifier =
                            Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(17.dp))
                                .background(
                                    if (panelEditMode) {
                                        MiuixTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    } else {
                                        labelColor.copy(alpha = 0.08f)
                                    },
                                )
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = { panelEditMode = !panelEditMode },
                                        onDoubleTap = { resetPanelGeometry() },
                                    )
                                },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "设置",
                            modifier = Modifier.size(18.dp),
                            tint =
                                if (panelEditMode) MiuixTheme.colorScheme.primary
                                else labelColor.copy(alpha = 0.65f),
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    // @author bomo 搜索框：点击优先调用系统输入法（IME），
                    // 窗口可聚焦（无 ALT_FOCUSABLE_IM）+ 焦点就绪后 showSoftInput；
                    // IME 未激活（异常场景）时回退模块自绘字母键盘（A-Z + 删除/清空）。
                    // 无按压高亮（indication=null），避免深色背景上出现黑色阴影。
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(34.dp)
                                .clip(RoundedCornerShape(17.dp))
                                .background(labelColor.copy(alpha = 0.08f))
                                .padding(horizontal = 12.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) {
                                    if (showKeyboard) showKeyboard = false
                                    useSystemIme = true
                                },
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (useSystemIme) {
                            AndroidView(
                                modifier = Modifier.fillMaxSize(),
                                factory = { ctx ->
                                    EditText(ctx).apply {
                                        setTextColor(labelColor.toArgb())
                                        hint = "搜索应用"
                                        setHintTextColor(labelColor.copy(alpha = 0.4f).toArgb())
                                        textSize = 12f
                                        isSingleLine = true
                                        setText(query)
                                        setSelection(query.length)
                                        background = null
                                        setPadding(0, 0, 0, 0)
                                        gravity = android.view.Gravity.START or android.view.Gravity.CENTER_VERTICAL
                                        isFocusableInTouchMode = true
                                        requestFocus()
                                        addTextChangedListener(
                                            object : TextWatcher {
                                                override fun beforeTextChanged(
                                                    s: CharSequence?,
                                                    start: Int,
                                                    count: Int,
                                                    after: Int,
                                                ) {}

                                                override fun onTextChanged(
                                                    s: CharSequence?,
                                                    start: Int,
                                                    before: Int,
                                                    count: Int,
                                                ) {}

                                                override fun afterTextChanged(e: Editable?) {
                                                    query = e?.toString() ?: ""
                                                }
                                            },
                                        )
                                        setOnEditorActionListener { _, _, _ ->
                                            hideIme(this)
                                            useSystemIme = false
                                            true
                                        }
                                        post {
                                            // @author bomo 视图 attach 后立即请求焦点并唤起 IME
                                            //（不等 onWindowFocusChanged，面板打开时窗口可能已聚焦、变化不触发）。
                                            requestFocus()
                                            runCatching {
                                                val imm =
                                                    ctx.getSystemService(Context.INPUT_METHOD_SERVICE)
                                                        as InputMethodManager
                                                imm.showSoftInput(this, 0)
                                            }
                                            // 窗口焦点变化兜底（首次 attach 后窗口获得焦点时再唤一次）。
                                            viewTreeObserver.addOnWindowFocusChangeListener { hasFocus ->
                                                if (hasFocus) {
                                                    runCatching {
                                                        val imm =
                                                            ctx.getSystemService(Context.INPUT_METHOD_SERVICE)
                                                                as InputMethodManager
                                                        imm.showSoftInput(this, 0)
                                                    }
                                                }
                                            }
                                            // @author bomo 兜底：IME 仍未激活则回退自绘键盘。
                                            Handler(Looper.getMainLooper()).postDelayed({
                                                runCatching {
                                                    val imm =
                                                        ctx.getSystemService(Context.INPUT_METHOD_SERVICE)
                                                            as InputMethodManager
                                                    if (!imm.isActive(this)) {
                                                        useSystemIme = false
                                                        showKeyboard = true
                                                    }
                                                }
                                            }, 800L)
                                        }
                                    }
                                },
                                update = { et ->
                                    if (et.text.toString() != query) {
                                        et.setText(query)
                                        et.setSelection(query.length)
                                    }
                                },
                            )
                        } else if (query.isEmpty()) {
                            Text(
                                text = "搜索应用",
                                color = labelColor.copy(alpha = 0.4f),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        } else {
                            Text(
                                text = query,
                                color = labelColor,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    // @author bomo 关闭按钮（右）：与左侧设置按钮同规格（18dp 灰色图标，34dp 圆角底），保持对称。
                    Box(
                        modifier =
                            Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(17.dp))
                                .background(labelColor.copy(alpha = 0.08f))
                                .clickable { listener.onDismissRequested() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "关闭",
                            modifier = Modifier.size(18.dp),
                            tint = labelColor.copy(alpha = 0.65f),
                        )
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(metrics.columns),
                modifier =
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val progress =
                                if (animationsEnabled) {
                                    contentProgress.value.coerceIn(0f, 1f)
                                } else {
                                    1f
                                }
                            alpha = progress
                            translationY =
                                (1f - progress) *
                                    metrics.iconDiameter *
                                    PANEL_CONTENT_TRANSLATION_FRACTION
                            compositingStrategy = CompositingStrategy.ModulateAlpha
                        },
                state = gridState,
                contentPadding =
                    // @author bomo 索引条跟随呼出方向：左侧呼出 → 索引在左（网格右侧留白）；右侧呼出 → 索引在右。
                    PaddingValues(
                        start =
                            if (side == CornerSide.Left) {
                                horizontalPadding + PANEL_INDEX_BAR_WIDTH
                            } else {
                                horizontalPadding
                            },
                        top = verticalPadding,
                        end =
                            if (side == CornerSide.Left) {
                                horizontalPadding
                            } else {
                                horizontalPadding + PANEL_INDEX_BAR_WIDTH
                            },
                        bottom = verticalPadding,
                    ),
                verticalArrangement = Arrangement.Top,
                userScrollEnabled = inputEnabled,
            ) {
                // @author bomo 空结果提示：搜索无匹配时不显示空网格（同时避免空索引导致崩溃）。
                if (filteredApps.isEmpty() && query.isNotBlank()) {
                    item(key = "more_panel_no_result", span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 56.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "未找到「${query.trim()}」",
                                color = labelColor.copy(alpha = 0.5f),
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
                // @author bomo 「最近使用」区：与下方 A-Z 分组同样的格子分布，只显示两行（[RECENT_ROWS] × 列数）。
                if (query.isEmpty() && catalog.recentApps.isNotEmpty()) {
                    item(
                        key = "recent_header",
                        span = { GridItemSpan(maxLineSpan) },
                    ) {
                        // @author bomo 「最近使用」标题行：与字母组标题同样式。
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(start = horizontalPadding + 4.dp)
                                    .padding(vertical = 6.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Text(
                                text = "最近使用",
                                color = labelColor.copy(alpha = 0.75f),
                                fontSize = labelSize,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.sp,
                                maxLines = 1,
                            )
                        }
                    }
                    items(
                        items = catalog.recentApps.take(metrics.columns * RECENT_ROWS),
                        key = { entry -> entry.component.flattenToShortString() + "_recent" },
                    ) { entry ->
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(cellHeight)
                                    .clickable(enabled = inputEnabled) {
                                        listener.onAppCommitted(entry)
                                    },
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Image(
                                bitmap = panelImages[entry.component] ?: entry.icon.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.size(iconSize),
                                filterQuality = FilterQuality.High,
                            )
                            Spacer(modifier = Modifier.height(labelGap))
                            Text(
                                text = entry.label,
                                modifier = Modifier.fillMaxWidth(),
                                color = labelColor,
                                fontSize = labelSize,
                                letterSpacing = 0.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
                grouped.forEach { (letter, apps) ->
                    item(
                        key = "more_panel_header_$letter",
                        span = { GridItemSpan(maxLineSpan) },
                    ) {
                        // @author bomo 字母组标题行：左上角显示首字母；上下留间距增大组与组之间的距离。
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(start = horizontalPadding + 4.dp)
                                    .padding(vertical = 6.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Text(
                                text = letter.toString(),
                                color = labelColor.copy(alpha = 0.75f),
                                fontSize = labelSize,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.sp,
                                maxLines = 1,
                            )
                        }
                    }
                    items(
                        items = apps,
                        key = { entry -> entry.component.flattenToShortString() },
                    ) { entry ->
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(cellHeight)
                                    .clickable(enabled = inputEnabled) {
                                        listener.onAppCommitted(entry)
                                    },
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            panelImages[entry.component]?.let { image ->
                                Image(
                                    bitmap = image,
                                    contentDescription = null,
                                    modifier = Modifier.size(iconSize),
                                    filterQuality = FilterQuality.High,
                                )
                            }
                            Spacer(modifier = Modifier.height(labelGap))
                            Text(
                                text = entry.label,
                                modifier = Modifier.fillMaxWidth(),
                                color = labelColor,
                                fontSize = labelSize,
                                letterSpacing = 0.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
            if (letters.size > 1) {
                BoxWithConstraints(
                    modifier =
                        Modifier
                            // @author bomo 索引条跟随呼出方向：左侧呼出 → 靠左；右侧呼出 → 靠右。
                            .align(
                                if (side == CornerSide.Left) {
                                    Alignment.CenterStart
                                } else {
                                    Alignment.CenterEnd
                                },
                            )
                            .fillMaxHeight()
                            .width(PANEL_INDEX_BAR_WIDTH),
                ) {
                    val barHeight = constraints.maxHeight.toFloat()
                    // @author bomo 以默认高度（16dp 间距可容纳全部字母）为标准：
                    // 高度增加 → 间距均匀增大填满（全部显示）；高度减小 → 保持 16dp 间距、减少可见数量。
                    val minStride = 16f * density.density
                    val vPadPx = verticalPadding.value * density.density
                    val availableHeight = (barHeight - vPadPx * 2).coerceAtLeast(1f)
                    val fullCount = indexLetters.size.coerceAtLeast(1)
                    val stride =
                        if (availableHeight >= fullCount * minStride) {
                            availableHeight / fullCount
                        } else {
                            minStride
                        }
                    val visibleCount =
                        if (stride >= minStride) fullCount else (availableHeight / stride).toInt().coerceAtLeast(1)
                    // @author bomo 高度不足时索引条为「跟随当前字母滑动」的窗口：
                    // 当前字母居中，保证滚动到底部也能看到/点到对应字母。
                    val activeIndex = activeLetter?.let { indexLetters.indexOf(it) } ?: 0
                    val startIndex =
                        if (visibleCount < fullCount) {
                            (activeIndex - visibleCount / 2).coerceIn(0, fullCount - visibleCount)
                        } else {
                            0
                        }
                    val textMeasurer = rememberTextMeasurer()
                    val primaryColor = MiuixTheme.colorScheme.primary
                    // @author bomo 当前分组字母放大一点（activeLetter 高亮 + 12sp，其余 10sp）。
                    val layouts: List<TextLayoutResult> =
                        remember(indexLetters, activeLetter, primaryColor, labelColor, startIndex, visibleCount) {
                            (startIndex until startIndex + visibleCount).map { i ->
                                val letter = indexLetters[i]
                                val active = letter == activeLetter
                                textMeasurer.measure(
                                    AnnotatedString(letter.toString()),
                                    style =
                                        TextStyle(
                                            color =
                                                if (active) {
                                                    primaryColor
                                                } else {
                                                    labelColor.copy(alpha = 0.65f)
                                                },
                                            fontSize = if (active) 12.sp else 10.sp,
                                            fontWeight =
                                                if (active) FontWeight.SemiBold
                                                else FontWeight.Medium,
                                        ),
                                )
                            }
                        }
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(
                                    // @author bomo 索引靠边方向跟随呼出方向：靠左 → start 留白；靠右 → end 留白。
                                    start = if (side == CornerSide.Left) 2.dp else 0.dp,
                                    end = if (side == CornerSide.Left) 0.dp else 2.dp,
                                    top = verticalPadding,
                                    bottom = verticalPadding,
                                )
                                .pointerInput(indexLetters, indexTargets, visibleCount, stride, startIndex, metrics) {
                                    if (!inputEnabled) return@pointerInput
                                    // @author bomo 点击/滑动索引滚动（rel35 组合，方向跟随版手感）：
                                    // 1) 距离远（含从下往上）时先瞬时跳到目标前一段，让目标附近 item 先渲染，
                                    //    避免 LazyGrid 长距离预估高度修正导致的分段滚动；
                                    // 2) 再以短距离动画精确到位（目标已渲染、无预估误差）；
                                    // 3) 兜底：动画后若首个可见 item 仍不是目标，瞬时校正一次保证对齐。
                                    suspend fun smoothScrollTo(target: Int) {
                                        val padPx = PANEL_INDEX_TARGET_PADDING_DP.value * density.density
                                        val preTarget = (target - PANEL_INDEX_PRE_RENDER_ITEMS).coerceAtLeast(0)
                                        if (abs(gridState.firstVisibleItemIndex - target) > PANEL_INDEX_PRE_RENDER_ITEMS * 2) {
                                            gridState.scrollToItem(preTarget, 0)
                                        }
                                        gridState.animateScrollToItem(target, padPx.toInt())
                                        if (gridState.firstVisibleItemIndex != target) {
                                            gridState.scrollToItem(target, padPx.toInt())
                                        }
                                    }
                                    fun jumpTo(index: Int) {
                                        val target = indexTargets[index]
                                        scope.launch { smoothScrollTo(target) }
                                    }
                                    fun selectAt(y: Float) {
                                        val index =
                                            (startIndex + ((y - vPadPx) / stride).toInt())
                                                .coerceIn(startIndex, startIndex + visibleCount - 1)
                                        jumpTo(index)
                                    }
                                    detectDragGestures(
                                        onDragStart = { offset -> selectAt(offset.y) },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            selectAt(change.position.y)
                                        },
                                    )
                                }
                                .pointerInput(indexLetters, indexTargets, visibleCount, stride, startIndex, metrics) {
                                    if (!inputEnabled) return@pointerInput
                                    // @author bomo 点击索引：同样走 smoothScrollTo（条件预跳 + 短距动画 + 兜底校正）。
                                    suspend fun smoothScrollTo(target: Int) {
                                        val padPx = PANEL_INDEX_TARGET_PADDING_DP.value * density.density
                                        val preTarget = (target - PANEL_INDEX_PRE_RENDER_ITEMS).coerceAtLeast(0)
                                        if (abs(gridState.firstVisibleItemIndex - target) > PANEL_INDEX_PRE_RENDER_ITEMS * 2) {
                                            gridState.scrollToItem(preTarget, 0)
                                        }
                                        gridState.animateScrollToItem(target, padPx.toInt())
                                        if (gridState.firstVisibleItemIndex != target) {
                                            gridState.scrollToItem(target, padPx.toInt())
                                        }
                                    }
                                    detectTapGestures { offset ->
                                        val index =
                                            (startIndex + ((offset.y - vPadPx) / stride).toInt())
                                                .coerceIn(startIndex, startIndex + visibleCount - 1)
                                        val target = indexTargets[index]
                                        scope.launch { smoothScrollTo(target) }
                                    }
                                },
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            layouts.forEachIndexed { i, layout ->
                                val centerY = vPadPx + stride * i + stride / 2f
                                drawText(
                                    textLayoutResult = layout,
                                    topLeft =
                                        Offset(
                                            (size.width - layout.size.width) / 2f,
                                            centerY - layout.size.height / 2f,
                                        ),
                                )
                            }
                        }
                    }
                }
                }
                }
            }
            // @author bomo 面板编辑模式：中心四向箭头（移动位置）+ 右下角手柄（调整长宽）。
            if (panelEditMode) {
                // 中心四向箭头：按压拖动 → 面板水平/垂直偏移（与设置页「偏移」滑条同步）。
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.Center)
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.38f))
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragEnd = { commitPanelGeometry() },
                                    onDragCancel = { commitPanelGeometry() },
                                ) { change, dragAmount ->
                                    change.consume()
                                    val densityPx = density.density
                                    val baseBounds =
                                        basePanelMetrics?.panel?.bounds ?: return@detectDragGestures
                                    val baseWidth = baseBounds.right - baseBounds.left
                                    val baseHeight = baseBounds.bottom - baseBounds.top
                                    val width = baseWidth * morePanelWidthPercent / 100f
                                    val height = baseHeight * morePanelHeightPercent / 100f
                                    // @author bomo 动态可移动范围：面板顶可达屏顶边距、底可达屏底边距，
                                    // 修复面板缩小后无法上移（固定 ±160dp 钳制不够）。
                                    val screenW = resources.displayMetrics.widthPixels.toFloat()
                                    val screenH = resources.displayMetrics.heightPixels.toFloat()
                                    val marginPx = 8f * densityPx
                                    val centerX = (baseBounds.left + baseBounds.right) / 2f
                                    val minOffsetXDp =
                                        ((marginPx - centerX + width / 2f) / densityPx).roundToInt()
                                    val maxOffsetXDp =
                                        ((screenW - marginPx - centerX + width / 2f) / densityPx).roundToInt()
                                    val minOffsetYDp =
                                        ((marginPx - baseBounds.bottom + height) / densityPx).roundToInt()
                                    val maxOffsetYDp =
                                        ((screenH - marginPx - baseBounds.bottom) / densityPx).roundToInt()
                                    morePanelOffsetXDp =
                                        (morePanelOffsetXDp + (dragAmount.x / densityPx).roundToInt())
                                            .coerceIn(minOffsetXDp, maxOffsetXDp)
                                    morePanelOffsetYDp =
                                        (morePanelOffsetYDp + (dragAmount.y / densityPx).roundToInt())
                                            .coerceIn(minOffsetYDp, maxOffsetYDp)
                                    refreshPanelMetrics()
                                }
                            },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.OpenWith,
                        contentDescription = "移动面板",
                        modifier = Modifier.size(24.dp),
                        tint = Color.White,
                    )
                }
                // 右下角手柄：按压拖动 → 面板宽/高百分比（与设置页「宽度/高度」滑条同步）。
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 10.dp, bottom = 10.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.38f))
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragEnd = { commitPanelGeometry() },
                                    onDragCancel = { commitPanelGeometry() },
                                ) { change, dragAmount ->
                                    change.consume()
                                    val baseBounds =
                                        basePanelMetrics?.panel?.bounds ?: return@detectDragGestures
                                    val baseWidth = baseBounds.right - baseBounds.left
                                    val baseHeight = baseBounds.bottom - baseBounds.top
                                    val densityPx = density.density
                                    val oldWidthPercent = morePanelWidthPercent
                                    val oldHeightPercent = morePanelHeightPercent
                                    morePanelWidthPercent =
                                        ((baseWidth * oldWidthPercent / 100f + dragAmount.x) /
                                            baseWidth * 100f)
                                            .roundToInt()
                                            .coerceIn(
                                                ModulePreferences.MIN_MORE_PANEL_WIDTH_PERCENT,
                                                ModulePreferences.MAX_MORE_PANEL_WIDTH_PERCENT,
                                            )
                                    morePanelHeightPercent =
                                        ((baseHeight * oldHeightPercent / 100f + dragAmount.y) /
                                            baseHeight * 100f)
                                            .roundToInt()
                                            .coerceIn(
                                                ModulePreferences.MIN_MORE_PANEL_HEIGHT_PERCENT,
                                                ModulePreferences.MAX_MORE_PANEL_HEIGHT_PERCENT,
                                            )
                                    // @author bomo 移动边跟随手指：宽度右边缘跟手（左边缘不动 → offsetX 补偿半量）、
                                    // 高度底边缘跟手（顶边缘不动 → offsetY 补偿全量）。
                                    val widthDeltaPx =
                                        baseWidth * (morePanelWidthPercent - oldWidthPercent) / 100f
                                    val heightDeltaPx =
                                        baseHeight * (morePanelHeightPercent - oldHeightPercent) / 100f
                                    // @author bomo 补偿后偏移的动态范围：面板缩小后允许继续上移/左移到屏边界。
                                    val curWidth = baseWidth * morePanelWidthPercent / 100f
                                    val curHeight = baseHeight * morePanelHeightPercent / 100f
                                    val screenW = resources.displayMetrics.widthPixels.toFloat()
                                    val screenH = resources.displayMetrics.heightPixels.toFloat()
                                    val marginPx = 8f * densityPx
                                    val centerX = (baseBounds.left + baseBounds.right) / 2f
                                    val minOffsetXDp =
                                        ((marginPx - centerX + curWidth / 2f) / densityPx).roundToInt()
                                    val maxOffsetXDp =
                                        ((screenW - marginPx - centerX + curWidth / 2f) / densityPx).roundToInt()
                                    val minOffsetYDp =
                                        ((marginPx - baseBounds.bottom + curHeight) / densityPx).roundToInt()
                                    val maxOffsetYDp =
                                        ((screenH - marginPx - baseBounds.bottom) / densityPx).roundToInt()
                                    morePanelOffsetXDp =
                                        (morePanelOffsetXDp + (widthDeltaPx / 2f / densityPx).roundToInt())
                                            .coerceIn(minOffsetXDp, maxOffsetXDp)
                                    morePanelOffsetYDp =
                                        (morePanelOffsetYDp + (heightDeltaPx / densityPx).roundToInt())
                                            .coerceIn(minOffsetYDp, maxOffsetYDp)
                                    refreshPanelMetrics()
                                }
                            },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FitScreen,
                        contentDescription = "调整大小",
                        modifier = Modifier.size(22.dp),
                        tint = Color.White,
                    )
                }
            }
            // @author bomo 键盘覆盖式浮在面板底部：不参与 Column 布局，
            // 因此弹出键盘不会压缩内容区（右侧 A-Z 索引条保持原尺寸）。
            if (showKeyboard) {
                PanelKeyboard(
                    query = query,
                    onKey = { ch -> query += ch },
                    onBackspace = { query = query.dropLast(1) },
                    onClear = { query = "" },
                    onDone = { showKeyboard = false },
                    labelColor = labelColor,
                    primaryColor = MiuixTheme.colorScheme.primary,
                    panelColor = panelColor,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }

    /** @author bomo 自绘搜索键盘：system_server 窗口无法连接输入法，用模块自绘字母键盘输入。 */
    @Composable
    private fun PanelKeyboard(
        query: String,
        onKey: (Char) -> Unit,
        onBackspace: () -> Unit,
        onClear: () -> Unit,
        onDone: () -> Unit,
        labelColor: Color,
        primaryColor: Color,
        panelColor: Color,
        modifier: Modifier = Modifier,
    ) {
        Column(
            modifier =
                modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(panelColor.copy(alpha = 0.96f))
                    .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (query.isEmpty()) "拼音首字母" else query,
                    color = labelColor.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "清空",
                    color = primaryColor,
                    fontSize = 11.sp,
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onClear() }
                            .padding(horizontal = 5.dp, vertical = 3.dp),
                )
                Text(
                    text = "完成",
                    color = primaryColor,
                    fontSize = 11.sp,
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onDone() }
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                )
            }
            listOf("ABCDEF", "GHIJKL", "MNOPQR", "STUVWX").forEach { row ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    row.forEach { ch ->
                        PanelKeyboardKey(
                            text = ch.toString(),
                            labelColor = labelColor,
                            primaryColor = primaryColor,
                            modifier = Modifier.weight(1f),
                            onClick = { onKey(ch) },
                        )
                    }
                }
            }
            // 最后一行：Y / Z 各一列，删除占剩余四列（宽删除键，避免一排四个重复删除）。
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                PanelKeyboardKey(
                    text = "Y",
                    labelColor = labelColor,
                    primaryColor = primaryColor,
                    modifier = Modifier.weight(1f),
                    onClick = { onKey('Y') },
                )
                PanelKeyboardKey(
                    text = "Z",
                    labelColor = labelColor,
                    primaryColor = primaryColor,
                    modifier = Modifier.weight(1f),
                    onClick = { onKey('Z') },
                )
                PanelKeyboardKey(
                    text = "删除",
                    labelColor = labelColor,
                    primaryColor = primaryColor,
                    modifier = Modifier.weight(4f),
                    onClick = onBackspace,
                )
            }
        }
    }

    @Composable
    private fun PanelKeyboardKey(
        text: String,
        labelColor: Color,
        primaryColor: Color,
        modifier: Modifier,
        onClick: () -> Unit,
    ) {
        Box(
            modifier =
                modifier
                    .padding(horizontal = 2.dp)
                    .height(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(labelColor.copy(alpha = 0.12f))
                    .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                color = labelColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }

    /** @author bomo 分组字母：ASCII 字母取大写，中文按拼音首字母（见 [InitialIndex]）。 */
    private fun initialOf(label: String): Char = InitialIndex.initialOf(label)

    /** @author bomo 隐藏系统输入法（IME 完成/面板关闭时调用）。 */
    private fun hideIme(view: android.view.View) {
        runCatching {
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    /** @author bomo 应用名称的拼音首字母序列（用于搜索匹配，如「微信」→ "WX"、「YouTube」→ "YOUTUBE"）。 */
    private fun pinyinInitialsOf(label: String): String =
        buildString {
            label.forEach { ch ->
                val code = ch.code
                when {
                    code in 'A'.code..'Z'.code -> append(ch)
                    code in 'a'.code..'z'.code -> append((code - 32).toChar())
                    code > 0xFF -> InitialIndex.pinyinInitialOf(ch)?.let(::append)
                }
            }
        }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!disposed && !lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        }
        post {
            if (!disposed && isAttachedToWindow) {
                performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
            }
        }
    }

    override fun onDetachedFromWindow() {
        val cleanupFailure = disposeOverlay()
        super.onDetachedFromWindow()
        cleanupFailure?.let(listener::onCleanupFailed)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (oldw != 0 && (oldw != w || oldh != h)) {
            requestDismiss()
            return
        }
        updateLayout()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateLayout()
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        appliedWindowInsets = insets
        val result = super.onApplyWindowInsets(insets)
        updateLayout()
        return result
    }

    override fun dispatchKeyEventPreIme(event: KeyEvent): Boolean {
        if (handleBack(event)) return true
        return super.dispatchKeyEventPreIme(event)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (handleBack(event)) return true
        return super.dispatchKeyEvent(event)
    }

    private fun handleBack(event: KeyEvent): Boolean {
        if (!panelModeState.value || event.keyCode != KeyEvent.KEYCODE_BACK) return false
        if (event.action == KeyEvent.ACTION_UP) requestDismiss()
        return true
    }

    private fun updateLayout() {
        if (width <= 0 || height <= 0 || disposed) return
        updateLayout(
            requestedWidth = width,
            requestedHeight = height,
            safeInsets = currentSafeInsets(),
        )
    }

    private fun updateLayout(
        requestedWidth: Int,
        requestedHeight: Int,
        safeInsets: OverlaySafeInsets,
    ) {
        if (requestedWidth <= 0 || requestedHeight <= 0 || disposed) return
        val radialInsets = currentRadialInsets()
        val safeWidth = (requestedWidth - radialInsets.left - radialInsets.right).coerceAtLeast(1f)
        val safeHeight = (requestedHeight - radialInsets.top - radialInsets.bottom).coerceAtLeast(1f)
        val systemIconDiameter = systemIconSize()
        // @author bomo 内外圈数量一律取「实际」列表长度，不要用外圈上限常量推导。
        // 原实现用外圈上限（ModulePreferences.OUTER_PINNED_APPS = 6）算内圈数，
        // 于是径向条目总数 ≤ 6 时
        // radialInnerCount 被算成 0 → RadialIconGeometry 的内圈半径保持 0f →
        // 视图 `innerRadius <= 0f` 的门槛直接不布内圈。
        // 表现为「外圈不到 6 个」或「内圈只有 1 个」时内圈整体消失（用户 2026-09-22 报告）。
        val outerApps = outerApps()
        val innerApps = innerApps()
        val metrics =
            AdaptiveOverlayGeometry.calculate(
                width = requestedWidth.toFloat(),
                height = requestedHeight.toFloat(),
                safeInsets = safeInsets,
                systemIconSize = systemIconDiameter,
                density = resources.displayMetrics.density,
                radialItemCount = outerApps.size + 1,
                radialInnerCount = innerApps.size,
                radialInsets = radialInsets,
                fontScale = resources.configuration.fontScale,
                panelItemCount = catalog.panelApps.size,
                anchorOnLeft = side == CornerSide.Left,
                radiusScale = radialRadiusScale,
            )
        basePanelMetrics = metrics
        metricsState.value =
            applyMorePanelCustomization(metrics, resources.displayMetrics.density)
        // 双圈：外圈 = 前 catalog.outerCount 个应用（含「更多」槽），其余进内圈。
        val outerLayout =
            RadialGeometry.layout(
                side = side,
                width = safeWidth,
                height = safeHeight,
                offsetX = radialInsets.left,
                offsetY = radialInsets.top,
                radius = metrics.radial.radius,
                itemCount = outerApps.size + 1,
            )
        val innerSpin =
            computeInnerCenters(
                innerApps = innerApps,
                outerCount = outerApps.size,
                safeWidth = safeWidth,
                safeHeight = safeHeight,
                radialInsets = radialInsets,
                innerRadius = metrics.radial.innerRadius,
            )
        innerSpinTargets = innerSpin.second
        layoutState.value =
            RadialLayout(
                side = side,
                origin = outerLayout.origin,
                radius = outerLayout.radius,
                itemCenters = outerLayout.itemCenters + innerSpin.first,
            )
        if (!panelModeState.value && !dismissing) updateGestureFromLatestPoint()
    }

    /** 外圈应用：目录顺序的前 outerCount 个（由配置决定，见 AppCatalogSnapshot.outerCount）。 */
    private fun outerApps(): List<RadialAppEntry> =
        catalog.radialApps.take(catalog.outerCount)

    /** 内圈应用：外圈之后的部分。 */
    private fun innerApps(): List<RadialAppEntry> =
        catalog.radialApps.drop(catalog.outerCount)

    /**
     * 合并布局下标 → 应用下标（[catalog.radialApps] 内）；返回 null 表示「更多」槽。
     * 合并顺序：外圈应用 0..n-1、「更多」在 n、内圈应用 n+1..。
     */
    private fun slotTarget(index: Int): Int? {
        val outerCount = outerApps().size
        return when {
            index < outerCount -> index
            index == outerCount -> null
            else -> outerCount + (index - outerCount - 1)
        }
    }

    /**
     * @author bomo 合并布局下标 → [catalog.radialApps] 下标。
     * 悬停滚轮模式下内圈 itemCenters 是「窗口内应用」的乱序子集，必须用动态映射；
     * 其余情况沿用静态 [slotTarget]。
     */
    private fun itemTarget(index: Int): Int? {
        val outerCount = outerApps().size
        if (innerSpinEnabled() && index > outerCount) {
            return innerSpinTargets.getOrNull(index - outerCount - 1)
        }
        return slotTarget(index)
    }

    /** @author bomo 悬停模式且内圈 ≥5 时启用滚轮转动；内圈 <5 均匀铺满不滚。 */
    private fun innerSpinEnabled(): Boolean =
        hoverMode && innerApps().size >= INNER_SPIN_MAX_SLOTS

    /**
     * @author bomo 应用 k 在滚轮窗口内的位置（度，[0,84)）；窗口外返回 null。
     * 应用首尾相接成环：`((k + 0.5) * step + offset) mod (N * step)`，
     * 滑动 offset 变化时一侧滑出、另一侧补齐，实现无缝循环。
     */
    private fun innerSpinWindowPosition(k: Int, count: Int): Float? {
        if (count <= 0) return null
        val ringLength = count * INNER_SPIN_STEP_DEGREES
        val raw = (k + 0.5f) * INNER_SPIN_STEP_DEGREES + innerAngleOffset
        val wrapped = ((raw % ringLength) + ringLength) % ringLength
        return wrapped.takeIf { it < INNER_SPAN_DEGREES }
    }

    /** @author bomo 窗口内位置 pos（度）→ 实际显示角度（度，含 side 基准）。 */
    private fun innerSpinDisplayAngle(pos: Float): Float =
        if (side == CornerSide.Left) {
            -INNER_SAFE_DEGREES - pos
        } else {
            -180f + INNER_SAFE_DEGREES + pos
        }

    /**
     * @author bomo 计算内圈中心点 + 对应 [catalog.radialApps] 下标。
     * 悬停滚轮：只返回窗口内应用（按窗口位置升序，保证 z-order / 选中顺序）；
     * 非悬停：沿用 [RadialGeometry.layoutInner] 均匀铺满全部内圈应用。
     */
    private fun computeInnerCenters(
        innerApps: List<RadialAppEntry>,
        outerCount: Int,
        safeWidth: Float,
        safeHeight: Float,
        radialInsets: OverlaySafeInsets,
        innerRadius: Float,
    ): Pair<List<GesturePoint>, List<Int>> {
        if (innerApps.isEmpty() || innerRadius <= 0f) {
            return emptyList<GesturePoint>() to emptyList<Int>()
        }
        if (!innerSpinEnabled()) {
            val centers =
                RadialGeometry
                    .layoutInner(
                        side = side,
                        width = safeWidth,
                        height = safeHeight,
                        radiusInner = innerRadius,
                        itemCount = innerApps.size,
                        offsetX = radialInsets.left,
                        offsetY = radialInsets.top,
                    ).itemCenters
            return centers to innerApps.indices.map { outerCount + it }
        }
        val origin =
            GesturePoint(
                radialInsets.left + if (side == CornerSide.Left) 0f else safeWidth,
                radialInsets.top + safeHeight,
            )
        val items =
            innerApps.indices
                .mapNotNull { k ->
                    innerSpinWindowPosition(k, innerApps.size)?.let { pos -> pos to k }
                }
                .sortedBy { it.first }
        val centers =
            items.map { (pos, _) ->
                val radians = innerSpinDisplayAngle(pos) * PI.toFloat() / 180f
                GesturePoint(
                    x = origin.x + cos(radians) * innerRadius,
                    y = origin.y + sin(radians) * innerRadius,
                )
            }
        val targets = items.map { (_, k) -> outerCount + k }
        return centers to targets
    }

    /** @author bomo 进入悬停模式：外圈静止、内圈切换为滚轮式展示并做一次进入提示。 */
    private fun enterHoverMode() {
        if (disposed || dismissing || panelModeState.value || hoverMode) return
        hoverMode = true
        selectedIndex = null
        selectedIndexState.intValue = NO_SELECTION
        refreshInnerSpinLayout()
        if (!hoverHintShown) {
            hoverHintShown = true
            triggerHoverHint()
        }
    }

    /** @author bomo 进入悬停视觉提示：内圈整体做一次呼吸脉动。 */
    private fun triggerHoverHint() {
        hoverHintPulse.floatValue = 0f
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 360L
            addUpdateListener {
                val t = it.animatedValue as Float
                hoverHintPulse.floatValue = HOVER_HINT_PULSE_AMPLITUDE * (1f - abs(2f * t - 1f))
            }
            start()
        }
    }

    /** @author bomo 悬停模式手势：位移小 = 点击意图（高亮手指下应用），位移大 = 拖动滚动。 */
    private fun handleInnerSpinDrag(x: Float, y: Float) {
        if (!innerSpinEnabled()) {
            // 内圈 <5：均匀铺满不滚，但可点击启动 → 沿用选中高亮。
            updateGestureFromLatestPoint()
            return
        }
        if (!innerSpinDragging) {
            if (innerSpinDownX == 0f && innerSpinDownY == 0f) {
                innerSpinDownX = x
                innerSpinDownY = y
                innerSpinLastX = x
                innerSpinLastY = y
                innerSpinLastTime = SystemClock.uptimeMillis()
            }
            val slopPx = ViewConfiguration.get(context).scaledTouchSlop.toFloat()
            if (hypot(x - innerSpinDownX, y - innerSpinDownY) > slopPx) {
                innerSpinDragging = true
                selectedIndex = null
                selectedIndexState.intValue = NO_SELECTION
            } else {
                updateGestureFromLatestPoint()
            }
        }
        if (innerSpinDragging) {
            stepInnerSpinDrag(x, y)
        }
    }

    /** @author bomo 悬停拖动：手指极角变化映射为滚动偏移，并记录角速度供松手惯性使用。 */
    private fun stepInnerSpinDrag(x: Float, y: Float) {
        val origin = layoutState.value.origin
        val now = SystemClock.uptimeMillis()
        val dt = ((now - innerSpinLastTime) / 1000f).coerceAtLeast(0.001f)
        val prev = atan2(innerSpinLastY - origin.y, innerSpinLastX - origin.x)
        val curr = atan2(y - origin.y, x - origin.x)
        val deltaDeg = normalizeAngleDelta(curr - prev)
        val signed = if (side == CornerSide.Left) -deltaDeg else deltaDeg
        val clamped = signed.coerceIn(-INNER_SPIN_MAX_DELTA_DEGREES, INNER_SPIN_MAX_DELTA_DEGREES)
        innerAngleOffset += clamped
        innerSpinVelocity = clamped / dt
        refreshInnerSpinLayout()
        innerSpinLastX = x
        innerSpinLastY = y
        innerSpinLastTime = now
    }

    private fun normalizeAngleDelta(deltaRad: Float): Float {
        var d = deltaRad
        while (d > PI.toFloat()) d -= 2f * PI.toFloat()
        while (d < -PI.toFloat()) d += 2f * PI.toFloat()
        return Math.toDegrees(d.toDouble()).toFloat()
    }

    /** @author bomo 悬停模式松手：拖动 → 惯性+吸附；点击 → 启动高亮应用；否则保持悬停。 */
    private fun finishInnerSpinGesture() {
        if (innerSpinDragging) {
            resetInnerSpinDragTracking()
            startInnerSpinInertia()
            return
        }
        val target = selectedIndex?.let(::itemTarget)
        resetInnerSpinDragTracking()
        if (target != null && target in catalog.radialApps.indices) {
            dismissAnimated(catalog.radialApps[target])
        }
        // 未选中任何应用 → 保持悬停（外圈静止、内圈可继续转动）。
    }

    private fun resetInnerSpinDragTracking() {
        innerSpinDownX = 0f
        innerSpinDownY = 0f
        innerSpinLastX = 0f
        innerSpinLastY = 0f
        innerSpinLastTime = 0L
        innerSpinDragging = false
    }

    /** @author bomo 松手后惯性转动：角速度逐帧按阻尼系数衰减，低到阈值进入档位吸附。 */
    private fun startInnerSpinInertia() {
        innerSpinAnimator?.cancel()
        innerSpinAnimating = true
        innerSpinLastAnimTime = SystemClock.uptimeMillis()
        innerSpinAnimator =
            ValueAnimator.ofFloat(0f, 1f).apply {
                duration = Long.MAX_VALUE / 2
                repeatCount = ValueAnimator.INFINITE
                addUpdateListener {
                    val now = SystemClock.uptimeMillis()
                    val dt =
                        ((now - innerSpinLastAnimTime) / 1000f)
                            .coerceAtMost(INNER_SPIN_MAX_FRAME_SECONDS)
                    innerSpinLastAnimTime = now
                    stepInnerSpinInertia(dt)
                }
                start()
            }
    }

    private fun stepInnerSpinInertia(dt: Float) {
        val damping = innerSpinDampingPercent / 100f
        innerSpinVelocity *= damping
        if (abs(innerSpinVelocity) < INNER_SPIN_ADSORB_VELOCITY_DEG) {
            startInnerSpinSnap()
            return
        }
        innerAngleOffset += innerSpinVelocity * dt
        refreshInnerSpinLayout()
    }

    /** @author bomo 吸附到最近档位（短弹簧动画），完成后规范化偏移避免累积误差。 */
    private fun startInnerSpinSnap() {
        val start = innerAngleOffset
        val target = round(innerAngleOffset / INNER_SPIN_STEP_DEGREES) * INNER_SPIN_STEP_DEGREES
        if (abs(target - start) < 0.01f) {
            normalizeInnerSpinOffset()
            return
        }
        innerSpinAnimator?.cancel()
        innerSpinAnimator =
            ValueAnimator.ofFloat(0f, 1f).apply {
                duration = INNER_SPIN_SNAP_MS
                addUpdateListener {
                    val t = it.animatedValue as Float
                    innerAngleOffset = start + (target - start) * snapEasing(t)
                    refreshInnerSpinLayout()
                    if (t >= 1f) {
                        normalizeInnerSpinOffset()
                    }
                }
                start()
            }
    }

    private fun normalizeInnerSpinOffset() {
        val count = innerApps().size
        if (count > 0) {
            val ringLength = count * INNER_SPIN_STEP_DEGREES
            var offset = ((innerAngleOffset % ringLength) + ringLength) % ringLength
            if (offset > ringLength / 2f) offset -= ringLength
            innerAngleOffset = offset
        }
        innerSpinVelocity = 0f
        innerSpinAnimating = false
        innerSpinAnimator?.cancel()
        innerSpinAnimator = null
        refreshInnerSpinLayout()
    }

    /** @author bomo 吸附短弹簧缓动（easeOutBack：快速到位 + 轻微回弹）。 */
    private fun snapEasing(t: Float): Float {
        val c1 = 1.70158f
        val c3 = c1 + 1f
        val x = t - 1f
        return 1f + c3 * x * x * x + c1 * x * x
    }

    /** @author bomo 转动立体感：窗口正面（中间档位）放大，两端缩小；关闭开关时恒为 1。 */
    private fun innerSpinDepthScale(innerOrdinal: Int): Float {
        if (!innerSpinDepthEffect) return 1f
        val count = innerSpinTargets.size
        if (count <= 1) return 1f
        val center = (count - 1) / 2f
        val distance = abs(innerOrdinal - center) / center.coerceAtLeast(1f)
        return 1f + INNER_SPIN_DEPTH_AMPLITUDE * (1f - distance)
    }

    /** @author bomo 悬停滚轮模式重算内圈布局（跟手 / 惯性 / 吸附每帧调用）。 */
    private fun refreshInnerSpinLayout() {
        if (disposed || panelModeState.value) return
        val metrics = metricsState.value ?: return
        if (metrics.radial.innerRadius <= 0f) return
        val radialInsets = currentRadialInsets()
        val safeWidth =
            (resources.displayMetrics.widthPixels - radialInsets.left - radialInsets.right)
                .coerceAtLeast(1f)
        val safeHeight =
            (resources.displayMetrics.heightPixels - radialInsets.top - radialInsets.bottom)
                .coerceAtLeast(1f)
        val outerApps = outerApps()
        val outerLayout =
            RadialGeometry.layout(
                side = side,
                width = safeWidth,
                height = safeHeight,
                offsetX = radialInsets.left,
                offsetY = radialInsets.top,
                radius = metrics.radial.radius,
                itemCount = outerApps.size + 1,
            )
        val innerSpin =
            computeInnerCenters(
                innerApps = innerApps(),
                outerCount = outerApps.size,
                safeWidth = safeWidth,
                safeHeight = safeHeight,
                radialInsets = radialInsets,
                innerRadius = metrics.radial.innerRadius,
            )
        innerSpinTargets = innerSpin.second
        layoutState.value =
            RadialLayout(
                side = side,
                origin = outerLayout.origin,
                radius = outerLayout.radius,
                itemCenters = outerLayout.itemCenters + innerSpin.first,
            )
    }

    /**
     * @author bomo 自绘「更多」面板自定义几何：按设置覆盖宽度/高度百分比、圆角与水平/垂直偏移。
     * 全部为默认值时原样返回（保持自适应几何）。宽度以面板中心为锚缩放，高度以底边为锚缩放，
     * 偏移在此基础上平移整个面板；圆角 0 表示跟随自适应几何。
     */
    private fun applyMorePanelCustomization(metrics: AdaptiveOverlayMetrics, density: Float): AdaptiveOverlayMetrics {
        if (morePanelWidthPercent == ModulePreferences.DEFAULT_MORE_PANEL_WIDTH_PERCENT &&
            morePanelHeightPercent == ModulePreferences.DEFAULT_MORE_PANEL_HEIGHT_PERCENT &&
            morePanelCornerRadiusDp == ModulePreferences.DEFAULT_MORE_PANEL_CORNER_RADIUS_DP &&
            morePanelOffsetXDp == ModulePreferences.DEFAULT_MORE_PANEL_OFFSET_X_DP &&
            morePanelOffsetYDp == ModulePreferences.DEFAULT_MORE_PANEL_OFFSET_Y_DP
        ) {
            return metrics
        }
        val base = metrics.panel.bounds
        val baseWidth = base.right - base.left
        val baseHeight = base.bottom - base.top
        // @author bomo 面板长宽可调上限参考手机分辨率：尺寸不超出屏幕（各留 ~8dp 边距）。
        val screenW = resources.displayMetrics.widthPixels.toFloat()
        val screenH = resources.displayMetrics.heightPixels.toFloat()
        val maxPanelW = (screenW - 8f * density * 2).coerceAtLeast(baseWidth)
        val maxPanelH = (screenH - 8f * density * 2).coerceAtLeast(baseHeight)
        val width = (baseWidth * morePanelWidthPercent / 100f).coerceAtMost(maxPanelW)
        val height = (baseHeight * morePanelHeightPercent / 100f).coerceAtMost(maxPanelH)
        val centerX = (base.left + base.right) / 2f
        val offsetX = morePanelOffsetXDp * density
        val offsetY = morePanelOffsetYDp * density
        val left = centerX - width / 2f + offsetX
        val top = base.bottom - height + offsetY
        // @author bomo 面板宽度变化 → 列数自适应（单格最小宽度不变，格数随宽度增减）。
        val panelColumns =
            kotlin.math.min(
                catalog.panelApps.size.coerceAtLeast(1),
                kotlin.math.floor((width - metrics.panel.contentHorizontalPadding * 2) / metrics.panel.minCellWidth)
                    .toInt()
                    .coerceAtLeast(1),
            )
        return metrics.copy(
            panel =
                metrics.panel.copy(
                    bounds = OverlayBounds(left, top, left + width, top + height),
                    cornerRadius =
                        if (morePanelCornerRadiusDp > 0) {
                            morePanelCornerRadiusDp * density
                        } else {
                            metrics.panel.cornerRadius
                        },
                    columns = panelColumns,
                ),
        )
    }

    /**
     * @author bomo 以定制前的自适应几何为基准重算面板几何（拖拽调整过程中实时调用）。
     * 不累积：每次都以 [basePanelMetrics] 为输入，仅由当前字段（宽/高百分比 + 偏移）决定结果。
     */
    private fun refreshPanelMetrics() {
        val base = basePanelMetrics ?: return
        metricsState.value = applyMorePanelCustomization(base, resources.displayMetrics.density)
    }

    /** @author bomo 面板拖拽松手：把当前几何回调给上层落盘（同步设置页「更多面板自定义」）。 */
    private fun commitPanelGeometry() {
        listener.onMorePanelGeometryChanged(
            morePanelWidthPercent,
            morePanelHeightPercent,
            morePanelOffsetXDp,
            morePanelOffsetYDp,
        )
    }

    /** @author bomo 恢复「更多」面板默认长宽及位置（双击顶栏设置按钮触发），并落盘同步。 */
    private fun resetPanelGeometry() {
        morePanelWidthPercent = ModulePreferences.DEFAULT_MORE_PANEL_WIDTH_PERCENT
        morePanelHeightPercent = ModulePreferences.DEFAULT_MORE_PANEL_HEIGHT_PERCENT
        morePanelOffsetXDp = ModulePreferences.DEFAULT_MORE_PANEL_OFFSET_X_DP
        morePanelOffsetYDp = ModulePreferences.DEFAULT_MORE_PANEL_OFFSET_Y_DP
        refreshPanelMetrics()
        commitPanelGeometry()
    }

    private fun updateGestureFromLatestPoint() {
        val metrics = metricsState.value ?: return
        val layout = layoutState.value
        if (layout.itemCenters.isEmpty()) return
        val outerCount = outerApps().size
        val next =
            if (hoverMode) {
                // @author bomo 悬停模式：外圈静止，仅在内圈窗口内选择（映射回完整下标）。
                val innerStart = outerCount + 1
                val innerCount = layout.itemCenters.size - innerStart
                if (innerCount <= 0) {
                    null
                } else {
                    val innerLayout =
                        RadialLayout(
                            side = layout.side,
                            origin = layout.origin,
                            radius = metrics.radial.innerRadius,
                            itemCenters = layout.itemCenters.drop(innerStart),
                        )
                    RadialGeometry
                        .selection(
                            layout = innerLayout,
                            x = latestX,
                            y = latestY,
                            previous = selectedIndex?.minus(innerStart)?.takeIf { it >= 0 },
                            enterRadius = metrics.radial.selectionEnterRadius,
                            keepRadius = metrics.radial.selectionKeepRadius,
                        )?.plus(innerStart)
                }
            } else {
                RadialGeometry.selection(
                    layout = layout,
                    x = latestX,
                    y = latestY,
                    previous = selectedIndex,
                    enterRadius = metrics.radial.selectionEnterRadius,
                    keepRadius = metrics.radial.selectionKeepRadius,
                )
            }
        if (next == selectedIndex) return
        selectedIndex = next
        selectedIndexState.intValue = next ?: NO_SELECTION
        val now = SystemClock.uptimeMillis()
        if (next != null && now - lastTickUptime >= TICK_INTERVAL_MS) {
            performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            lastTickUptime = now
        }
    }

    private fun showMorePanel() {
        if (dismissing || panelModeState.value) return
        selectedIndex = null
        handoffEntryProgress.floatValue = entryProgress.value
        handoffLayoutState.value = layoutState.value
        handoffMetricsState.value = metricsState.value?.radial
        removeCallbacks(timeout)
        listener.onMorePanelRequested()
    }

    fun showBuiltInMorePanel() {
        if (disposed || !isAttachedToWindow) return
        backdropOnlyState.value = false
        panelModeState.value = true
        removeCallbacks(timeout)
        postDelayed(timeout, PANEL_TIMEOUT_MS)
        requestFocus()
    }

    fun retainBackdropForPanel() {
        backdropAlpha.floatValue = 1f
        backdropOnlyState.value = true
    }

    fun beginBackdropExit() {
        if (disposed || !backdropOnlyState.value) return
        backdropAnimator?.cancel()
        if (!ValueAnimator.areAnimatorsEnabled()) {
            backdropAlpha.floatValue = 0f
            return
        }
        backdropAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 500L
            addUpdateListener {
                val alpha = AllAppsPanelMotion.exit(it.currentPlayTime / 1000f).alpha
                backdropAlpha.floatValue = alpha
                if (alpha <= AllAppsPanelMotion.EXIT_ALPHA_THRESHOLD) {
                    backdropAlpha.floatValue = 0f
                    cancel()
                }
            }
            start()
        }
    }

    fun hideBackdropAfterFrame(onHidden: () -> Unit) {
        backdropAnimator?.cancel()
        backdropAnimator = null
        if (disposed || !isAttachedToWindow) {
            onHidden()
            return
        }
        // 必须等透明帧提交后才让截图等工具执行，不能只确认状态变量已经更新。
        if (isHardwareAccelerated) {
            viewTreeObserver.registerFrameCommitCallback { post { onHidden() } }
        } else {
            postOnAnimation { postOnAnimation { onHidden() } }
        }
        backdropAlpha.floatValue = 0f
        invalidate()
    }

    private fun dismissAnimated(pendingCommit: RadialAppEntry?) {
        if (dismissing || disposed) return
        dismissing = true
        removeCallbacks(timeout)
        removeCallbacks(hoverTimeout)
        innerSpinAnimator?.cancel()
        innerSpinAnimator = null
        exitRequestState.value = ExitRequest(pendingCommit, entryProgress.value)
        removeCallbacks(dismissFallback)
        if (!ValueAnimator.areAnimatorsEnabled()) {
            completeRadialExit()
        } else {
            val durationScale = ValueAnimator.getDurationScale().coerceAtLeast(1f)
            val fallbackDelay =
                (RadialDismissMotion.DURATION_MILLIS * durationScale + DISMISS_FALLBACK_GRACE_MS)
                    .roundToInt()
                    .toLong()
            postDelayed(dismissFallback, fallbackDelay)
        }
    }

    private fun completeRadialExit() {
        if (dismissNotified || disposed) return
        removeCallbacks(dismissFallback)
        val pendingCommit = exitRequestState.value?.pendingCommit
        exitRequestState.value = null
        if (pendingCommit == null) {
            requestDismiss()
        } else {
            dismissNotified = true
            removeCallbacks(timeout)
            listener.onAppCommitted(pendingCommit)
        }
    }

    private fun resetPanelTimeout() {
        if (!panelModeState.value || disposed) return
        removeCallbacks(timeout)
        postDelayed(timeout, PANEL_TIMEOUT_MS)
    }

    private fun requestDismiss() {
        if (dismissNotified || disposed) return
        dismissNotified = true
        removeCallbacks(timeout)
        removeCallbacks(dismissFallback)
        listener.onDismissRequested()
    }

    private fun mergeCleanupFailure(
        current: Throwable?,
        next: Throwable,
    ): Throwable =
        current?.also { previous -> previous.addSuppressed(next) } ?: next

    private fun currentRadialInsets(): OverlaySafeInsets {
        val windowInsets = appliedWindowInsets ?: rootWindowInsets ?: return OverlaySafeInsets()
        // 扇形属于角落手势本身；系统手势保留区及屏幕圆角不是整页绘制边距。
        // 只避让实际系统栏、缺口和键盘，正常竖屏圆心落在物理侧边与导航栏顶边。
        val insets = windowInsets.getInsets(
            WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout() or WindowInsets.Type.ime(),
        )
        return OverlaySafeInsets(insets.left.toFloat(), insets.top.toFloat(), insets.right.toFloat(), insets.bottom.toFloat())
    }

    private fun currentSafeInsets(): OverlaySafeInsets {
        val windowInsets = appliedWindowInsets ?: rootWindowInsets ?: return OverlaySafeInsets()
        val drawingInsets =
            windowInsets.getInsets(
                WindowInsets.Type.systemBars() or
                    WindowInsets.Type.displayCutout() or
                    WindowInsets.Type.ime(),
            )
        val gestureInsets =
            windowInsets.getInsets(
                WindowInsets.Type.systemGestures() or
                    WindowInsets.Type.mandatorySystemGestures(),
            )
        val topLeft = windowInsets.getRoundedCorner(RoundedCorner.POSITION_TOP_LEFT)
        val topRight = windowInsets.getRoundedCorner(RoundedCorner.POSITION_TOP_RIGHT)
        val bottomLeft = windowInsets.getRoundedCorner(RoundedCorner.POSITION_BOTTOM_LEFT)
        val bottomRight = windowInsets.getRoundedCorner(RoundedCorner.POSITION_BOTTOM_RIGHT)
        return OverlaySafeInsets(
            left = maxOf(drawingInsets.left, gestureInsets.left).toFloat(),
            top =
                maxOf(
                    drawingInsets.top,
                    gestureInsets.top,
                    topLeft?.radius ?: 0,
                    topRight?.radius ?: 0,
                ).toFloat(),
            right = maxOf(drawingInsets.right, gestureInsets.right).toFloat(),
            bottom =
                maxOf(
                    drawingInsets.bottom,
                    gestureInsets.bottom,
                    bottomLeft?.radius ?: 0,
                    bottomRight?.radius ?: 0,
                ).toFloat(),
        )
    }

    private fun systemIconSize(): Float =
        try {
            context
                .getSystemService(ActivityManager::class.java)
                ?.launcherLargeIconSize
                ?.toFloat()
                ?: 0f
        } catch (_: RuntimeException) {
            0f
        }

    /**
     * @author bomo 选中圈颜色：设置「选中圈颜色」为具体 ARGB 时直接用；
     * [ModulePreferences.SELECTION_RING_COLOR_SYSTEM]（跟随系统）时取系统强调蓝
     * （ColorOS 动态取色，`system_accent1_500`），解析失败回退 [FALLBACK_SELECTION_RING_COLOR]。
     * 框架资源对所有进程可读。
     */
    private fun resolveSelectionRingColor(): Color {
        val configured = selectionRingColorArgb
        if (configured != ModulePreferences.SELECTION_RING_COLOR_SYSTEM) return Color(configured)
        val resources = Resources.getSystem()
        val accentId = resources.getIdentifier("system_accent1_500", "color", "android")
        if (accentId != 0) {
            return try {
                Color(resources.getColor(accentId, null))
            } catch (_: Throwable) {
                FALLBACK_SELECTION_RING_COLOR
            }
        }
        return FALLBACK_SELECTION_RING_COLOR
    }

    private data class ExitRequest(val pendingCommit: RadialAppEntry?, val entryProgress: Float)

    private companion object {
        val EMPTY_LAYOUT =
            RadialLayout(
                side = CornerSide.Right,
                origin = io.github.mangi.flymefreeform.gesture.GesturePoint(0f, 0f),
                radius = 0f,
                itemCenters = emptyList(),
            )
        const val NO_SELECTION = -1
        const val PLATE_ALPHA = 235f / 255f
        const val PANEL_SURFACE_ALPHA = 253f / 255f
        const val PANEL_EXPAND_DAMPING = 0.9f
        const val PANEL_EXPAND_RESPONSE_SECONDS = 0.3f
        const val PANEL_VISIBILITY_THRESHOLD = 0.0001f
        const val PANEL_CONTENT_DELAY_MILLIS = 45
        const val PANEL_CONTENT_FADE_MILLIS = 180
        const val PANEL_CONTENT_TRANSLATION_FRACTION = 0.12f
        const val TICK_INTERVAL_MS = 80L
        const val GESTURE_TIMEOUT_MS = 5_000L
        const val PANEL_TIMEOUT_MS = 15_000L
        const val DISMISS_FALLBACK_GRACE_MS = 260L
        /** @author bomo 选中项的放大尺度（1.3 = 放大 30%）：图标与紧贴它的选中圈同步放大。 */
        const val SELECTION_RING_ENLARGE_SCALE = 1.3f
        /** @author bomo 「更多」宫格格心间距系数（<1 = 四格更紧凑；1 = 铺满圆盘）。 */
        const val MORE_GRID_COMPACT_FRACTION = 0.72f
        /** @author bomo 「更多」面板顶栏（关闭 + 搜索框）高度。 */
        val PANEL_TOP_BAR_HEIGHT = 52.dp
        /** @author bomo 「更多」面板右侧 A-Z 首字母索引条宽度。 */
        val PANEL_INDEX_BAR_WIDTH = 22.dp
        /** @author bomo 「更多」面板顶部「最近使用」区行数（两行，格子与 A-Z 分组一致）。 */
        const val RECENT_ROWS = 2
        /** @author bomo 「更多」面板图标放大系数：仅放大图标尺寸，不影响格子布局。 */
        const val PANEL_ICON_SIZE_BOOST = 1.3f
        /** @author bomo 点击索引跳转时预渲染的目标前移格数：先瞬时跳到附近再短距离动画，避免分段滚动。 */
        const val PANEL_INDEX_PRE_RENDER_ITEMS = 8
        /** @author bomo 索引点击后目标标题距内容区顶的留白：避免标题顶死贴边，保留原版（rel35）手感。 */
        val PANEL_INDEX_TARGET_PADDING_DP = 8.dp
        /** @author bomo 系统强调蓝解析失败时的回退色（ColorOS 风格蓝）。 */
        val FALLBACK_SELECTION_RING_COLOR = Color(0xFF0A84FF)
        /** @author bomo 扇形半径缩放系数的钳制区间（对应设置百分比 60% ~ 150%）。 */
        const val RADIAL_RADIUS_SCALE_MIN = 0.6f
        const val RADIAL_RADIUS_SCALE_MAX = 1.5f
        /** @author bomo 选中圈描边厚度缩放系数的钳制区间（对应设置百分比 50% ~ 250%）。 */
        const val SELECTION_RING_WIDTH_SCALE_MIN = 0.5f
        const val SELECTION_RING_WIDTH_SCALE_MAX = 2.5f
        /** @author bomo 内圈滚轮转动：可视档位上限（内圈 ≥5 固定显示 5 个，<5 均匀铺满）。 */
        const val INNER_SPIN_MAX_SLOTS = 5
        /** @author bomo 内圈滚轮档位间距（度）：84° 弧 / 5 档。 */
        const val INNER_SPIN_STEP_DEGREES = 16.8f
        /** @author bomo 内圈弧张角（度），与 RadialGeometry.SPAN_DEGREES 一致。 */
        const val INNER_SPAN_DEGREES = 84f
        /** @author bomo 内圈弧安全角（度），与 RadialGeometry.SAFE_DEGREES 一致。 */
        const val INNER_SAFE_DEGREES = 3f
        /** @author bomo 惯性吸附角速度阈值（deg/s）：低于此值直接进入吸附。 */
        const val INNER_SPIN_ADSORB_VELOCITY_DEG = 30f
        /** @author bomo 单帧最大角度增量（deg）：防止极端快速滑动一帧跳过多档。 */
        const val INNER_SPIN_MAX_DELTA_DEGREES = 32f
        /** @author bomo 吸附短弹簧动画时长（ms）。 */
        const val INNER_SPIN_SNAP_MS = 130L
        /** @author bomo 惯性单帧最大时间步（s）：防止掉帧导致速度积分爆炸。 */
        const val INNER_SPIN_MAX_FRAME_SECONDS = 0.05f
        /** @author bomo 进入悬停视觉提示的脉动放大幅度（比例）。 */
        const val HOVER_HINT_PULSE_AMPLITUDE = 0.12f
        /** @author bomo 转动立体感的正面项放大幅度（比例；窗口中间最大，两端为 0）。 */
        const val INNER_SPIN_DEPTH_AMPLITUDE = 0.15f
    }
}

private fun Modifier.panelReveal(
    progress: () -> Float,
    seedSize: Float,
    cornerRadius: Float,
    anchorOnLeft: Boolean,
    squircleEnabled: Boolean,
): Modifier =
    drawWithCache {
        val revealPath = Path()
        onDrawWithContent {
            val revealProgress = progress()
            val revealWidth = PanelRevealMotion.extent(size.width, seedSize, revealProgress)
            val revealHeight = PanelRevealMotion.extent(size.height, seedSize, revealProgress)
            val revealLeft =
                PanelRevealMotion.horizontalOffset(size.width, revealWidth, anchorOnLeft)
            val revealTop = PanelRevealMotion.verticalOffset(size.height, revealHeight)
            revealPath.rewind()
            revealPath.addSquircleRect(
                width = revealWidth,
                height = revealHeight,
                cornerRadius = cornerRadius,
                squircleEnabled = squircleEnabled,
            )
            translate(left = revealLeft, top = revealTop) {
                clipPath(revealPath) {
                    translate(left = -revealLeft, top = -revealTop) {
                        this@onDrawWithContent.drawContent()
                    }
                }
            }
        }
    }
