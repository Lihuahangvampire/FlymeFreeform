package io.github.mangi.flymefreeform.ui

import android.graphics.Color as AndroidColor
import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.ElectricalServices
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.SwipeLeft
import androidx.compose.material.icons.rounded.SwipeRight
import androidx.compose.material.icons.rounded.SwipeUp
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.mangi.flymefreeform.R
import io.github.mangi.flymefreeform.config.ModulePreferences
import io.github.mangi.flymefreeform.config.OutsideTapCloseMode
import io.github.mangi.flymefreeform.config.TriggerShape
import io.github.mangi.flymefreeform.framework.FrameworkConnectionIssue
import io.github.mangi.flymefreeform.framework.FrameworkConnectionState
import io.github.mangi.flymefreeform.framework.FrameworkConnectionStatus
import io.github.mangi.flymefreeform.ui.component.TopBarBackdrop
import io.github.mangi.flymefreeform.ui.component.captureForTopBar
import io.github.mangi.flymefreeform.ui.component.rememberTopBarBackdrop
import io.github.mangi.flymefreeform.ui.component.topBarContainerColor
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlaySpinnerPreference
import top.yukonga.miuix.kmp.preference.SliderPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import kotlin.math.roundToInt

@Composable
internal fun ControlScreen(
    state: FrameworkConnectionState,
    onModuleEnabledChange: (Boolean) -> Unit,
    onLeftCornerEnabledChange: (Boolean) -> Unit,
    onRightCornerEnabledChange: (Boolean) -> Unit,
    onCornerTriggerRangeChange: (Int) -> Unit,
    /** @author bomo 触发热区形状（扇形 / 三角形）变更。 */
    onTriggerShapeChange: (TriggerShape) -> Unit,
    /** @author bomo 三角形热区横向长度变更（沿屏幕底边）。 */
    onTriggerHorizontalDpChange: (Int) -> Unit,
    /** @author bomo 三角形热区纵向高度变更（沿屏幕侧边）。 */
    onTriggerVerticalDpChange: (Int) -> Unit,
    /** @author bomo 「全部」面板缩放百分比变更（设置界面滑条，热生效）。 */
    onPanelScaleChange: (Int) -> Unit,
    /** @author bomo 扇形外圈 / 内圈半径缩放百分比变更（设置界面滑条，下次呼出生效）。 */
    onRadialRadiusChange: (Int) -> Unit,
    /** @author bomo 扇形选中圈描边厚度缩放百分比变更。 */
    onSelectionRingWidthChange: (Int) -> Unit,
    /** @author bomo 扇形选中圈颜色（ARGB）变更。 */
    onSelectionRingColorChange: (Int) -> Unit,
    onOutsideTapCloseModeChange: (OutsideTapCloseMode) -> Unit,
    onHandleSwipeUpToMiniEnabledChange: (Boolean) -> Unit,
    /** @author bomo 外圈固定应用未满时用最近使用补齐（开关）。 */
    onFillOuterWithRecentChange: (Boolean) -> Unit,
    onPauseInLandscapeChange: (Boolean) -> Unit,
    onPauseInGameModeChange: (Boolean) -> Unit,
    onRequestScopes: () -> Unit,
    onNavigateToPinnedApps: () -> Unit,
) {
    val scrollBehavior = MiuixScrollBehavior()
    val backdrop = rememberTopBarBackdrop()
    val topBarColor = topBarContainerColor(backdrop)
    // @author bomo 拖动任一条触发热区滑条时，用「其余项的已确认值 + 正在拖动的草稿值」
    // 拼出预览快照；松手即置空收回预览。三条滑条共用同一个预览通道。
    var cornerRangePreview by remember { mutableStateOf<TriggerRangePreview?>(null) }
    LaunchedEffect(state.canChangeSettings) {
        if (!state.canChangeSettings) cornerRangePreview = null
    }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val windowWidth = maxWidth
        val isWideScreen = windowWidth >= WideWindowMinWidth
        Scaffold(
            contentWindowInsets =
                WindowInsets.systemBars
                    .union(WindowInsets.displayCutout)
                    .union(WindowInsets.ime),
            topBar = {
                TopBarBackdrop(backdrop) {
                    if (isWideScreen) {
                        SmallTopAppBar(
                            title = stringResource(R.string.screen_settings_title),
                            color = topBarColor,
                            scrollBehavior = scrollBehavior,
                        )
                    } else {
                        TopAppBar(
                            title = stringResource(R.string.screen_settings_title),
                            color = topBarColor,
                            scrollBehavior = scrollBehavior,
                        )
                    }
                }
            },
        ) { innerPadding ->
            val layoutDirection = LocalLayoutDirection.current
            val safeStart = innerPadding.calculateStartPadding(layoutDirection)
            val safeEnd = innerPadding.calculateEndPadding(layoutDirection)
            val safeWidth = (windowWidth - safeStart - safeEnd).coerceAtLeast(0.dp)
            val centeredSide = maxOf(ScreenHorizontalMargin, (safeWidth - ScreenContentMaxWidth) / 2)
            Box(modifier = Modifier.fillMaxSize().captureForTopBar(backdrop)) {
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .consumeWindowInsets(innerPadding)
                            .scrollEndHaptic()
                            .overScrollVertical()
                            .nestedScroll(scrollBehavior.nestedScrollConnection),
                    verticalArrangement = Arrangement.spacedBy(CardSpacing),
                    contentPadding =
                        PaddingValues(
                            start = safeStart + centeredSide,
                            top = innerPadding.calculateTopPadding() + ScreenTopSpacing,
                            end = safeEnd + centeredSide,
                            bottom = innerPadding.calculateBottomPadding() + ScreenBottomSpacing,
                        ),
                    overscrollEffect = null,
                ) {
                    item(key = "runtime") { RuntimeCard(state, onRequestScopes) }
                    item(key = "settings") {
                        SettingsCard(
                            state,
                            onModuleEnabledChange,
                            onLeftCornerEnabledChange,
                            onRightCornerEnabledChange,
                            onCornerTriggerRangeChange,
                            onTriggerShapeChange = onTriggerShapeChange,
                            onTriggerHorizontalDpChange = onTriggerHorizontalDpChange,
                            onTriggerVerticalDpChange = onTriggerVerticalDpChange,
                            onCornerRangePreviewChange = { cornerRangePreview = it },
                            onPanelScaleChange = onPanelScaleChange,
                            onRadialRadiusChange = onRadialRadiusChange,
                            onSelectionRingWidthChange = onSelectionRingWidthChange,
                            onSelectionRingColorChange = onSelectionRingColorChange,
                            onNavigateToPinnedApps,
                        )
                    }
                    item(key = "window_interaction") {
                        WindowInteractionCard(
                            state = state,
                            onOutsideTapCloseModeChange = onOutsideTapCloseModeChange,
                            onHandleSwipeUpToMiniEnabledChange =
                                onHandleSwipeUpToMiniEnabledChange,
                        )
                    }
                    item(key = "radial_apps_policy") {
                        RadialAppsPolicyCard(
                            state = state,
                            onFillOuterWithRecentChange = onFillOuterWithRecentChange,
                        )
                    }
                    item(key = "environment") {
                        EnvironmentCard(state, onPauseInLandscapeChange, onPauseInGameModeChange)
                    }
                    item(key = "about") { AboutCard() }
                }
            }
        }
        cornerRangePreview?.let { preview ->
            CornerRangePreview(
                shape = preview.shape,
                rangeDp = preview.rangeDp,
                horizontalDp = preview.horizontalDp,
                verticalDp = preview.verticalDp,
                leftEnabled = state.settings.leftCornerEnabled,
                rightEnabled = state.settings.rightCornerEnabled,
            )
        }
    }
}

@Composable
private fun EnvironmentCard(
    state: FrameworkConnectionState,
    onPauseInLandscapeChange: (Boolean) -> Unit,
    onPauseInGameModeChange: (Boolean) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        SwitchPreference(
            checked = state.settings.pauseInLandscape,
            onCheckedChange = onPauseInLandscapeChange,
            title = stringResource(R.string.pause_in_landscape_title),
            summary = stringResource(R.string.pause_in_landscape_summary),
            enabled = state.canChangeSettings,
            startAction = { PreferenceIcon(Icons.Rounded.ScreenRotation, state.canChangeSettings) },
        )
        SwitchPreference(
            checked = state.settings.pauseInGameMode,
            onCheckedChange = onPauseInGameModeChange,
            title = stringResource(R.string.pause_in_game_mode_title),
            summary = stringResource(R.string.pause_in_game_mode_summary),
            enabled = state.canChangeSettings,
            startAction = { PreferenceIcon(Icons.Rounded.SportsEsports, state.canChangeSettings) },
        )
    }
}

/**
 * @author bomo 扇形应用策略：外圈固定应用未满（少于 6 个）时，用最近使用的应用补齐空缺。
 * 开关写入远端配置，侧边栏进程下次构建目录即生效，无需重启。
 */
@Composable
private fun RadialAppsPolicyCard(
    state: FrameworkConnectionState,
    onFillOuterWithRecentChange: (Boolean) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        SwitchPreference(
            checked = state.settings.fillOuterWithRecent,
            onCheckedChange = onFillOuterWithRecentChange,
            title = stringResource(R.string.fill_outer_with_recent_title),
            summary = stringResource(R.string.fill_outer_with_recent_summary),
            enabled = state.canChangeSettings,
            startAction = { PreferenceIcon(Icons.Rounded.History, state.canChangeSettings) },
        )
    }
}

@Composable
private fun WindowInteractionCard(
    state: FrameworkConnectionState,
    onOutsideTapCloseModeChange: (OutsideTapCloseMode) -> Unit,
    onHandleSwipeUpToMiniEnabledChange: (Boolean) -> Unit,
) {
    val modes = OutsideTapCloseMode.entries
    val items =
        listOf(
            DropdownItem(text = stringResource(R.string.outside_tap_mode_disabled)),
            DropdownItem(text = stringResource(R.string.outside_tap_mode_single)),
            DropdownItem(text = stringResource(R.string.outside_tap_mode_double)),
        )
    Card(modifier = Modifier.fillMaxWidth()) {
        OverlaySpinnerPreference(
            items = items,
            selectedIndex = modes.indexOf(state.settings.outsideTapCloseMode),
            title = stringResource(R.string.outside_tap_close_title),
            summary = stringResource(R.string.outside_tap_close_summary),
            enabled = state.canChangeSettings,
            startAction = { PreferenceIcon(Icons.Rounded.TouchApp, state.canChangeSettings) },
            onSelectedIndexChange = { index ->
                modes.getOrNull(index)?.let(onOutsideTapCloseModeChange)
            },
        )
        SwitchPreference(
            checked = state.settings.handleSwipeUpToMiniEnabled,
            onCheckedChange = onHandleSwipeUpToMiniEnabledChange,
            title = stringResource(R.string.handle_swipe_up_to_mini_title),
            summary = stringResource(R.string.handle_swipe_up_to_mini_summary),
            enabled = state.canChangeSettings,
            startAction = { PreferenceIcon(Icons.Rounded.SwipeUp, state.canChangeSettings) },
        )
    }
}

@Composable
private fun RuntimeCard(state: FrameworkConnectionState, onRequestScopes: () -> Unit) {
    val presentation = frameworkPresentation(state)
    Card(modifier = Modifier.fillMaxWidth()) {
        BasicComponent(
            title = stringResource(R.string.framework_service_title),
            summary = presentation.summary,
            endActions = {
                Text(
                    text = presentation.label,
                    color = MiuixTheme.colorScheme.onSurface,
                    style = MiuixTheme.textStyles.body2,
                )
            },
            startAction = { PreferenceIcon(Icons.Rounded.ElectricalServices) },
        )
        if (state.status == FrameworkConnectionStatus.Connected && state.missingScopes.isNotEmpty()) {
            ArrowPreference(
                title = stringResource(R.string.scope_title),
                summary =
                    if (state.isRequestingScope) stringResource(R.string.scope_requesting)
                    else if (state.issue == FrameworkConnectionIssue.ScopeRequestFailed) {
                        stringResource(R.string.scope_request_failed)
                    } else {
                        stringResource(R.string.scope_missing, state.missingScopes.joinToString(" · "))
                    },
                onClick = onRequestScopes,
                enabled = state.canRequestScope,
                startAction = { PreferenceIcon(Icons.Rounded.AccountTree, state.canRequestScope) },
            )
        } else {
            BasicComponent(
                title = stringResource(R.string.scope_title),
                summary =
                    if (state.status == FrameworkConnectionStatus.Connected) {
                        stringResource(R.string.scope_complete)
                    } else {
                        stringResource(R.string.scope_waiting)
                    },
                startAction = { PreferenceIcon(Icons.Rounded.AccountTree) },
            )
        }
        BasicComponent(
            title = stringResource(R.string.implementation_state_title),
            summary = stringResource(R.string.implementation_state_summary),
            endActions = {
                Text(
                    text = stringResource(R.string.implementation_state_value),
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    style = MiuixTheme.textStyles.body2,
                )
            },
            startAction = { PreferenceIcon(Icons.Rounded.Dashboard) },
        )
    }
}

@Composable
private fun SettingsCard(
    state: FrameworkConnectionState,
    onModuleEnabledChange: (Boolean) -> Unit,
    onLeftCornerEnabledChange: (Boolean) -> Unit,
    onRightCornerEnabledChange: (Boolean) -> Unit,
    onCornerTriggerRangeChange: (Int) -> Unit,
    /** @author bomo 触发热区形状变更。 */
    onTriggerShapeChange: (TriggerShape) -> Unit,
    /** @author bomo 三角形热区横向长度变更。 */
    onTriggerHorizontalDpChange: (Int) -> Unit,
    /** @author bomo 三角形热区纵向高度变更。 */
    onTriggerVerticalDpChange: (Int) -> Unit,
    onCornerRangePreviewChange: (TriggerRangePreview?) -> Unit,
    /** @author bomo 「全部」面板缩放百分比变更。 */
    onPanelScaleChange: (Int) -> Unit,
    /** @author bomo 扇形外圈 / 内圈半径缩放百分比变更。 */
    onRadialRadiusChange: (Int) -> Unit,
    /** @author bomo 扇形选中圈描边厚度缩放百分比变更。 */
    onSelectionRingWidthChange: (Int) -> Unit,
    /** @author bomo 扇形选中圈颜色（ARGB；跟随系统为 [ModulePreferences.SELECTION_RING_COLOR_SYSTEM]）变更。 */
    onSelectionRingColorChange: (Int) -> Unit,
    onNavigateToPinnedApps: () -> Unit,
) {
    // @author bomo 模块开关 summary 不响应 isUpdating：提交瞬间若切换文案
    // （如「正在写入框架配置」），行高变化会引发整个列表上下跳动。
    val moduleSummary =
        when {
            !state.canChangeSettings && state.status != FrameworkConnectionStatus.Connected ->
                stringResource(R.string.module_enabled_summary_waiting)
            state.missingScopes.isNotEmpty() -> stringResource(R.string.module_enabled_summary_scope)
            state.settings.enabled -> stringResource(R.string.module_enabled_summary_on)
            else -> stringResource(R.string.module_enabled_summary_off)
        }
    val appsSummary =
        when {
            !state.settings.pinsSaved -> stringResource(R.string.radial_apps_recent_summary)
            state.settings.pinnedComponents.isEmpty() -> stringResource(R.string.radial_apps_empty_summary)
            else -> stringResource(R.string.radial_apps_count_summary, state.settings.pinnedComponents.size)
        }
    Card(modifier = Modifier.fillMaxWidth()) {
        SwitchPreference(
            checked = state.settings.enabled,
            onCheckedChange = onModuleEnabledChange,
            title = stringResource(R.string.module_enabled_title),
            summary = moduleSummary,
            enabled = state.canChangeSettings,
            startAction = {
                PreferenceIcon(Icons.Rounded.PowerSettingsNew, state.canChangeSettings)
            },
        )
        SwitchPreference(
            checked = state.settings.leftCornerEnabled,
            onCheckedChange = onLeftCornerEnabledChange,
            title = stringResource(R.string.left_corner_title),
            summary = stringResource(R.string.left_corner_summary),
            enabled = state.canChangeSettings,
            startAction = { PreferenceIcon(Icons.Rounded.SwipeRight, state.canChangeSettings) },
        )
        SwitchPreference(
            checked = state.settings.rightCornerEnabled,
            onCheckedChange = onRightCornerEnabledChange,
            title = stringResource(R.string.right_corner_title),
            summary = stringResource(R.string.right_corner_summary),
            enabled = state.canChangeSettings,
            startAction = { PreferenceIcon(Icons.Rounded.SwipeLeft, state.canChangeSettings) },
        )
        TriggerShapePreference(
            shape = state.settings.triggerShape,
            enabled = state.canChangeSettings,
            onShapeChange = onTriggerShapeChange,
        )
        // @author bomo 只显示当前形状用得上的滑条（用户 2026-09-22 要求）：
        // 扇形 → 半径；三角形 → 横向 + 纵向。不做"都画出来再置灰"，
        // 避免用户以为灰色滑条调了会生效。
        if (state.settings.triggerShape == TriggerShape.Triangle) {
            RemoteDpSliderPreference(
                icon = Icons.Rounded.SwapHoriz,
                confirmedValue = state.settings.triggerHorizontalDp,
                isUpdating = state.isUpdating,
                enabled = state.canChangeSettings,
                title = stringResource(R.string.trigger_horizontal_title),
                summary = stringResource(R.string.trigger_horizontal_summary),
                onPreviewChange = { dp ->
                    onCornerRangePreviewChange(
                        dp?.let {
                            TriggerRangePreview(
                                shape = TriggerShape.Triangle,
                                rangeDp = state.settings.cornerTriggerRangeDp,
                                horizontalDp = it,
                                verticalDp = state.settings.triggerVerticalDp,
                            )
                        },
                    )
                },
                onCommit = onTriggerHorizontalDpChange,
                coerce = { ModulePreferences.coerceTriggerExtentDp(it) },
                minDp = ModulePreferences.MIN_TRIGGER_EXTENT_DP,
                maxDp = ModulePreferences.MAX_TRIGGER_EXTENT_DP,
            )
            RemoteDpSliderPreference(
                icon = Icons.Rounded.SwapVert,
                confirmedValue = state.settings.triggerVerticalDp,
                isUpdating = state.isUpdating,
                enabled = state.canChangeSettings,
                title = stringResource(R.string.trigger_vertical_title),
                summary = stringResource(R.string.trigger_vertical_summary),
                onPreviewChange = { dp ->
                    onCornerRangePreviewChange(
                        dp?.let {
                            TriggerRangePreview(
                                shape = TriggerShape.Triangle,
                                rangeDp = state.settings.cornerTriggerRangeDp,
                                horizontalDp = state.settings.triggerHorizontalDp,
                                verticalDp = it,
                            )
                        },
                    )
                },
                onCommit = onTriggerVerticalDpChange,
                coerce = { ModulePreferences.coerceTriggerExtentDp(it) },
                minDp = ModulePreferences.MIN_TRIGGER_EXTENT_DP,
                maxDp = ModulePreferences.MAX_TRIGGER_EXTENT_DP,
            )
        } else {
            RemoteDpSliderPreference(
                icon = Icons.Rounded.Straighten,
                confirmedValue = state.settings.cornerTriggerRangeDp,
                isUpdating = state.isUpdating,
                enabled = state.canChangeSettings,
                title = stringResource(R.string.corner_trigger_range_title),
                summary = stringResource(R.string.corner_trigger_range_summary),
                onPreviewChange = { dp ->
                    onCornerRangePreviewChange(
                        dp?.let {
                            TriggerRangePreview(
                                shape = TriggerShape.Sector,
                                rangeDp = it,
                                horizontalDp = state.settings.triggerHorizontalDp,
                                verticalDp = state.settings.triggerVerticalDp,
                            )
                        },
                    )
                },
                onCommit = onCornerTriggerRangeChange,
            )
        }
        // @author bomo 面板缩放滑条：写入远端配置后，侧边栏进程在**下次打开面板**时读取，
        // 因此调整后无需重启/重装即可看到效果。
        RemotePercentSliderPreference(
            icon = Icons.Rounded.Dashboard,
            confirmedValue = state.settings.panelScalePercent,
            isUpdating = state.isUpdating,
            enabled = state.canChangeSettings,
            title = stringResource(R.string.panel_scale_title),
            summary = stringResource(R.string.panel_scale_summary),
            valueRange =
                ModulePreferences.MIN_PANEL_SCALE_PERCENT.toFloat()..
                    ModulePreferences.MAX_PANEL_SCALE_PERCENT.toFloat(),
            coerceValue = ModulePreferences::coercePanelScalePercent,
            onCommit = onPanelScaleChange,
        )
        // @author bomo 扇形半径滑条：两圈（外圈 + 内圈）同心缩放，写入远端配置后
        // 侧边栏进程在**下次呼出扇形**时读取，调整后无需重启/重装即可看到效果。
        RemotePercentSliderPreference(
            icon = Icons.Rounded.Straighten,
            confirmedValue = state.settings.radialRadiusPercent,
            isUpdating = state.isUpdating,
            enabled = state.canChangeSettings,
            title = stringResource(R.string.radial_radius_title),
            summary = stringResource(R.string.radial_radius_summary),
            valueRange =
                ModulePreferences.MIN_RADIAL_RADIUS_PERCENT.toFloat()..
                    ModulePreferences.MAX_RADIAL_RADIUS_PERCENT.toFloat(),
            coerceValue = ModulePreferences::coerceRadialRadiusPercent,
            onCommit = onRadialRadiusChange,
        )
        // @author bomo 选中圈厚度滑条：默认基准为图标直径 6.8%（约 3.25dp 观感），
        // 该滑条只缩放描边宽度，不改圈半径；写入远端配置后**下次呼出扇形**即生效。
        RemotePercentSliderPreference(
            icon = Icons.Rounded.TouchApp,
            confirmedValue = state.settings.selectionRingWidthPercent,
            isUpdating = state.isUpdating,
            enabled = state.canChangeSettings,
            title = stringResource(R.string.selection_ring_width_title),
            summary = stringResource(R.string.selection_ring_width_summary),
            valueRange =
                ModulePreferences.MIN_SELECTION_RING_WIDTH_PERCENT.toFloat()..
                    ModulePreferences.MAX_SELECTION_RING_WIDTH_PERCENT.toFloat(),
            coerceValue = ModulePreferences::coerceSelectionRingWidthPercent,
            onCommit = onSelectionRingWidthChange,
        )
        // @author bomo 选中圈颜色：色板首项「跟随系统」（系统强调蓝），其余为预设色。
        SelectionColorPreference(
            selectedArgb = state.settings.selectionRingColorArgb,
            enabled = state.canChangeSettings,
            onColorChange = onSelectionRingColorChange,
        )
        ArrowPreference(
            title = stringResource(R.string.radial_apps_title),
            summary = appsSummary,
            onClick = onNavigateToPinnedApps,
            enabled = state.canChangeSettings,
            startAction = { PreferenceIcon(Icons.Rounded.Apps, state.canChangeSettings) },
        )
    }
}

/**
 * @author bomo 扇形选中圈颜色选择：点击行右侧色块展开 HSV 颜色选择器
 * （色相条 + 饱和度 / 亮度平面），拖动即实时预览、松手写入远端配置；
 * 展开区内提供「跟随系统」选项恢复动态取系统强调蓝。
 */
@Composable
private fun SelectionColorPreference(
    selectedArgb: Int,
    enabled: Boolean,
    onColorChange: (Int) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }
    // 松手提交后、远端配置确认前保持预览色，避免色块闪回旧值再跳新值。
    var pendingArgb by remember { mutableStateOf<Int?>(null) }
    val baseArgb =
        if (selectedArgb == ModulePreferences.SELECTION_RING_COLOR_SYSTEM) {
            0xFF0A84FF.toInt()
        } else {
            selectedArgb
        }
    var draftHue by remember { mutableFloatStateOf(0f) }
    var draftSaturation by remember { mutableFloatStateOf(0f) }
    var draftValue by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(baseArgb) {
        if (!isDragging && pendingArgb == null) {
            val hsv = FloatArray(3)
            AndroidColor.colorToHSV(baseArgb, hsv)
            draftHue = hsv[0]
            draftSaturation = hsv[1]
            draftValue = hsv[2]
        }
    }
    // 远端确认后解除 pending，色块自然切换到 selectedArgb（数值相同，无跳变）。
    LaunchedEffect(selectedArgb) {
        if (pendingArgb != null && selectedArgb == pendingArgb) {
            pendingArgb = null
        }
    }
    val draftArgb =
        AndroidColor.HSVToColor(floatArrayOf(draftHue, draftSaturation, draftValue))
    // 拖动中 / 待确认期间用本地预览色，否则用远端配置色（跟随系统时显示渐变蓝）。
    val showingSystemStyle =
        selectedArgb == ModulePreferences.SELECTION_RING_COLOR_SYSTEM &&
            !isDragging &&
            pendingArgb == null
    val showingPreview = isDragging || pendingArgb != null
    val swatchColorArgb = if (showingPreview) draftArgb else selectedArgb
    BasicComponent(
        title = stringResource(R.string.selection_ring_color_title),
        summary = stringResource(R.string.selection_ring_color_summary),
        startAction = { PreferenceIcon(Icons.Rounded.ColorLens, enabled) },
        endActions = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showingSystemStyle) {
                    Text(
                        text = stringResource(R.string.selection_ring_color_system),
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        style = MiuixTheme.textStyles.body2,
                    )
                }
                Box(
                    modifier =
                        Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .then(
                                if (showingSystemStyle) {
                                    Modifier.background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF0A84FF), Color(0xFF6FD0FF)),
                                        ),
                                    )
                                } else {
                                    Modifier.background(Color(swatchColorArgb))
                                },
                            )
                            .border(1.dp, Color(0x33000000), CircleShape)
                            .clickable(enabled = enabled) { expanded = !expanded },
                )
            }
        },
    )
    if (expanded) {
        HsvColorPicker(
            hue = draftHue,
            saturation = draftSaturation,
            value = draftValue,
            enabled = enabled,
            onHueChange = { h ->
                draftHue = h
                isDragging = true
            },
            onSvChange = { s, v ->
                draftSaturation = s
                draftValue = v
                isDragging = true
            },
            onDragEnd = {
                isDragging = false
                val argb =
                    AndroidColor.HSVToColor(floatArrayOf(draftHue, draftSaturation, draftValue))
                pendingArgb = argb
                onColorChange(argb)
            },
        )
        BasicComponent(
            title = stringResource(R.string.selection_ring_color_system),
            summary = stringResource(R.string.selection_ring_color_system_summary),
            startAction = {
                Box(
                    modifier =
                        Modifier
                            .padding(end = 6.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF0A84FF), Color(0xFF6FD0FF)),
                                ),
                            )
                            .clickable(enabled = enabled) {
                                pendingArgb = null
                                isDragging = false
                                onColorChange(ModulePreferences.SELECTION_RING_COLOR_SYSTEM)
                                expanded = false
                            },
                )
            },
            endActions = {
                if (selectedArgb == ModulePreferences.SELECTION_RING_COLOR_SYSTEM) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = MiuixTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            },
        )
    }
}

/** @author bomo HSV 色相条渐变端点（红→黄→绿→青→蓝→品红→红）。 */
private val HUE_GRADIENT_COLORS =
    listOf(
        Color(0xFFFF0000),
        Color(0xFFFFFF00),
        Color(0xFF00FF00),
        Color(0xFF00FFFF),
        Color(0xFF0000FF),
        Color(0xFFFF00FF),
        Color(0xFFFF0000),
    )

/**
 * @author bomo 自由选色器：上部饱和度 / 亮度平面（x = 饱和度，y = 亮度），
 * 下部色相条；拖动 / 点按更新，松手经 [onDragEnd] 提交。
 */
@Composable
private fun HsvColorPicker(
    hue: Float,
    saturation: Float,
    value: Float,
    enabled: Boolean,
    onHueChange: (Float) -> Unit,
    onSvChange: (Float, Float) -> Unit,
    onDragEnd: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(10.dp)),
        ) {
            val planeWidth = constraints.maxWidth.toFloat()
            val planeHeight = constraints.maxHeight.toFloat()
            Canvas(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .pointerInput(enabled) {
                            if (!enabled) return@pointerInput
                            detectDragGestures(
                                onDragStart = { offset ->
                                    onSvChange(
                                        (offset.x / planeWidth).coerceIn(0f, 1f),
                                        (1f - offset.y / planeHeight).coerceIn(0f, 1f),
                                    )
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    onSvChange(
                                        (change.position.x / planeWidth).coerceIn(0f, 1f),
                                        (1f - change.position.y / planeHeight).coerceIn(0f, 1f),
                                    )
                                },
                                onDragEnd = { onDragEnd() },
                                onDragCancel = { onDragEnd() },
                            )
                        },
            ) {
                val hueColor =
                    Color(AndroidColor.HSVToColor(floatArrayOf(hue.coerceIn(0f, 360f), 1f, 1f)))
                drawRect(
                    brush =
                        Brush.linearGradient(
                            listOf(Color.White, hueColor),
                            start = Offset.Zero,
                            end = Offset(planeWidth, 0f),
                        ),
                )
                drawRect(
                    brush =
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black),
                            startY = 0f,
                            endY = planeHeight,
                        ),
                )
                val indicatorX = saturation.coerceIn(0f, 1f) * planeWidth
                val indicatorY = (1f - value.coerceIn(0f, 1f)) * planeHeight
                drawCircle(
                    color = Color.White,
                    radius = 8f,
                    center = Offset(indicatorX, indicatorY),
                    style = Stroke(width = 2.5f),
                )
                drawCircle(
                    color = Color.Black.copy(alpha = 0.5f),
                    radius = 8f,
                    center = Offset(indicatorX, indicatorY),
                    style = Stroke(width = 1f),
                )
            }
        }
        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(22.dp)
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(6.dp)),
        ) {
            val barWidth = constraints.maxWidth.toFloat()
            Canvas(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .pointerInput(enabled) {
                            if (!enabled) return@pointerInput
                            detectDragGestures(
                                onDragStart = { offset ->
                                    onHueChange((offset.x / barWidth * 360f).coerceIn(0f, 360f))
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    onHueChange((change.position.x / barWidth * 360f).coerceIn(0f, 360f))
                                },
                                onDragEnd = { onDragEnd() },
                                onDragCancel = { onDragEnd() },
                            )
                        },
            ) {
                drawRect(brush = Brush.horizontalGradient(HUE_GRADIENT_COLORS))
                val indicatorX = (hue.coerceIn(0f, 360f) / 360f) * barWidth
                drawCircle(
                    color = Color.White,
                    radius = 7f,
                    center = Offset(indicatorX, size.height / 2f),
                    style = Stroke(width = 2.5f),
                )
                drawCircle(
                    color = Color.Black.copy(alpha = 0.5f),
                    radius = 7f,
                    center = Offset(indicatorX, size.height / 2f),
                    style = Stroke(width = 1f),
                )
            }
        }
    }
}

/**
 * @author bomo 触发热区形状选择器；条目顺序即 [TriggerShape.entries] 顺序。
 */
@Composable
private fun TriggerShapePreference(
    shape: TriggerShape,
    enabled: Boolean,
    onShapeChange: (TriggerShape) -> Unit,
) {
    val shapes = TriggerShape.entries
    val items =
        listOf(
            DropdownItem(text = stringResource(R.string.trigger_shape_sector)),
            DropdownItem(text = stringResource(R.string.trigger_shape_triangle)),
        )
    OverlaySpinnerPreference(
        items = items,
        selectedIndex = shapes.indexOf(shape),
        title = stringResource(R.string.trigger_shape_title),
        summary = stringResource(R.string.trigger_shape_summary),
        enabled = enabled,
        startAction = { PreferenceIcon(Icons.Rounded.Category, enabled) },
        onSelectedIndexChange = { index -> shapes.getOrNull(index)?.let(onShapeChange) },
    )
}

/**
 * @author bomo dp 数值滑条。默认按「角落触发范围」（扇形半径）钳制与取值；
 * 三角形热区的横向 / 纵向长度通过 [coerce] 与 [minDp] / [maxDp] 覆盖为同一量纲的另一组范围。
 */
@Composable
private fun RemoteDpSliderPreference(
    icon: ImageVector,
    confirmedValue: Int,
    isUpdating: Boolean,
    enabled: Boolean,
    title: String,
    summary: String,
    onPreviewChange: (Int?) -> Unit,
    onCommit: (Int) -> Unit,
    coerce: (Int) -> Int = { ModulePreferences.coerceCornerTriggerRangeDp(it) },
    minDp: Int = ModulePreferences.MIN_CORNER_TRIGGER_RANGE_DP,
    maxDp: Int = ModulePreferences.MAX_CORNER_TRIGGER_RANGE_DP,
) {
    var draftValue by rememberSaveable { mutableFloatStateOf(confirmedValue.toFloat()) }
    var isDragging by remember { mutableStateOf(false) }
    LaunchedEffect(confirmedValue, isUpdating, enabled) {
        if (!enabled) {
            isDragging = false
            onPreviewChange(null)
        }
        if (!isDragging && !isUpdating) draftValue = confirmedValue.toFloat()
    }
    SliderPreference(
        value = draftValue,
        onValueChange = { value ->
            isDragging = true
            val draft = coerce(value.roundToInt())
            draftValue = draft.toFloat()
            onPreviewChange(draft)
        },
        title = title,
        summary = summary,
        valueText = stringResource(R.string.dp_value, draftValue.roundToInt()),
        enabled = enabled,
        startAction = { PreferenceIcon(icon, enabled) },
        valueRange = minDp.toFloat()..maxDp.toFloat(),
        steps = (maxDp - minDp - 1).coerceAtLeast(0),
        onValueChangeFinished = {
            isDragging = false
            onPreviewChange(null)
            val committed = coerce(draftValue.roundToInt())
            draftValue = committed.toFloat()
            if (committed != confirmedValue) onCommit(committed)
        },
    )
}

/**
 * @author bomo 百分比滑条（「全部」面板缩放专用）。
 *
 * 与 [RemoteDpSliderPreference] 的差异只有钳制函数与文案；刻意独立实现而不改写既有 dp 滑条，
 * 以免影响已发布行为。若以后出现第三个同类滑条，应抽出通用实现合并。
 */
@Composable
private fun RemotePercentSliderPreference(
    icon: ImageVector,
    confirmedValue: Int,
    isUpdating: Boolean,
    enabled: Boolean,
    title: String,
    summary: String,
    /** @author bomo 滑条取值区间与钳制函数（面板缩放 / 扇形半径各有口径，不可共用）。 */
    valueRange: ClosedFloatingPointRange<Float>,
    coerceValue: (Int) -> Int,
    onCommit: (Int) -> Unit,
) {
    var draftValue by rememberSaveable { mutableFloatStateOf(confirmedValue.toFloat()) }
    var isDragging by remember { mutableStateOf(false) }
    LaunchedEffect(confirmedValue, isUpdating, enabled) {
        if (!isDragging && !isUpdating) draftValue = confirmedValue.toFloat()
    }
    SliderPreference(
        value = draftValue,
        onValueChange = { value ->
            isDragging = true
            draftValue = coerceValue(value.roundToInt()).toFloat()
        },
        title = title,
        summary = summary,
        valueText = stringResource(R.string.percent_value, draftValue.roundToInt()),
        enabled = enabled,
        startAction = { PreferenceIcon(icon, enabled) },
        valueRange = valueRange,
        steps = valueRange.endInclusive.toInt() - valueRange.start.toInt() - 1,
        onValueChangeFinished = {
            isDragging = false
            val committed = coerceValue(draftValue.roundToInt())
            draftValue = committed.toFloat()
            if (committed != confirmedValue) onCommit(committed)
        },
    )
}

/** @author bomo 触发热区预览快照：拖动任一条滑条时，用「其余项已确认值 + 本条草稿值」组成。 */
private data class TriggerRangePreview(
    val shape: TriggerShape,
    val rangeDp: Int,
    val horizontalDp: Int,
    val verticalDp: Int,
)

/**
 * @author bomo 触发热区预览。几何与命中判定同源（见 `CornerTriggerRegion`）：
 * 扇形为四分之一圆；三角形以屏幕角落为直角顶点，两条直角边分别沿底边（横向）与侧边（纵向），
 * 斜边朝内。预览只作示意，不参与任何手势判定。
 */
@Composable
private fun CornerRangePreview(
    shape: TriggerShape,
    rangeDp: Int,
    horizontalDp: Int,
    verticalDp: Int,
    leftEnabled: Boolean,
    rightEnabled: Boolean,
) {
    val activeColor = MiuixTheme.colorScheme.primary
    val inactiveColor = MiuixTheme.colorScheme.onSurfaceVariantSummary
    Canvas(modifier = Modifier.fillMaxSize()) {
        val radius = rangeDp.dp.toPx()
        val horizontal = horizontalDp.dp.toPx()
        val vertical = verticalDp.dp.toPx()
        val bottom = size.height
        val arcSize = Size(radius * 2f, radius * 2f)
        val strokeWidth = 2.dp.toPx()
        val dash = PathEffect.dashPathEffect(floatArrayOf(7.dp.toPx(), 5.dp.toPx()))

        fun drawSector(startAngle: Float, topLeft: Offset, enabled: Boolean) {
            val color = if (enabled) activeColor else inactiveColor
            drawArc(
                color = color.copy(alpha = if (enabled) 0.22f else 0.08f),
                startAngle = startAngle,
                sweepAngle = 90f,
                useCenter = true,
                topLeft = topLeft,
                size = arcSize,
            )
            drawArc(
                color = color.copy(alpha = if (enabled) 0.82f else 0.30f),
                startAngle = startAngle,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, pathEffect = dash),
            )
        }

        /** @param cornerX 直角顶点横坐标；[mirror] 为真时（右下角）直角边向左延伸。 */
        fun drawTriangle(cornerX: Float, mirror: Boolean, enabled: Boolean) {
            val color = if (enabled) activeColor else inactiveColor
            val path =
                Path().apply {
                    moveTo(cornerX, bottom)
                    lineTo(if (mirror) cornerX - horizontal else cornerX + horizontal, bottom)
                    lineTo(cornerX, bottom - vertical)
                    close()
                }
            drawPath(path, color = color.copy(alpha = if (enabled) 0.22f else 0.08f))
            drawPath(
                path,
                color = color.copy(alpha = if (enabled) 0.82f else 0.30f),
                style = Stroke(width = strokeWidth, pathEffect = dash),
            )
        }

        when (shape) {
            TriggerShape.Sector -> {
                drawSector(
                    startAngle = 270f,
                    topLeft = Offset(-radius, bottom - radius),
                    enabled = leftEnabled,
                )
                drawSector(
                    startAngle = 180f,
                    topLeft = Offset(size.width - radius, bottom - radius),
                    enabled = rightEnabled,
                )
            }

            TriggerShape.Triangle -> {
                drawTriangle(cornerX = 0f, mirror = false, enabled = leftEnabled)
                drawTriangle(cornerX = size.width, mirror = true, enabled = rightEnabled)
            }
        }
    }
}

@Composable
private fun AboutCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        BasicComponent(
            title = stringResource(R.string.implementation_principle_title),
            summary = stringResource(R.string.implementation_principle_summary),
            startAction = { PreferenceIcon(Icons.Rounded.Code) },
        )
        BasicComponent(
            title = stringResource(R.string.independent_project_title),
            summary = stringResource(R.string.independent_project_summary),
            startAction = { PreferenceIcon(Icons.Rounded.Info) },
        )
    }
}

@Composable
private fun PreferenceIcon(imageVector: ImageVector, enabled: Boolean = true) {
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        modifier = Modifier.padding(end = 6.dp).size(24.dp),
        tint =
            if (enabled) MiuixTheme.colorScheme.onBackground
            else MiuixTheme.colorScheme.disabledOnSecondaryVariant,
    )
}

@Composable
private fun frameworkPresentation(state: FrameworkConnectionState): FrameworkPresentation =
    when (state.status) {
        FrameworkConnectionStatus.Waiting -> FrameworkPresentation(stringResource(R.string.framework_status_waiting), stringResource(R.string.framework_waiting_summary))
        FrameworkConnectionStatus.Connected -> {
            val unknown = stringResource(R.string.framework_unknown_value)
            FrameworkPresentation(
                stringResource(R.string.framework_status_connected),
                stringResource(
                    R.string.framework_connected_summary,
                    state.frameworkName ?: unknown,
                    state.frameworkVersion ?: unknown,
                    state.apiVersion?.toString() ?: unknown,
                ),
            )
        }
        FrameworkConnectionStatus.Incompatible -> FrameworkPresentation(stringResource(R.string.framework_status_incompatible), stringResource(state.issue.incompatibleSummary()))
        FrameworkConnectionStatus.Error -> FrameworkPresentation(stringResource(R.string.framework_status_error), stringResource(state.issue.errorSummary()))
    }

@StringRes
private fun FrameworkConnectionIssue?.incompatibleSummary(): Int =
    when (this) {
        FrameworkConnectionIssue.ServiceApiTooOld -> R.string.framework_api_too_old_summary
        FrameworkConnectionIssue.RemoteCapabilityMissing -> R.string.framework_remote_missing_summary
        FrameworkConnectionIssue.SystemCapabilityMissing -> R.string.framework_system_missing_summary
        FrameworkConnectionIssue.MultipleServices -> R.string.framework_multiple_services_summary
        else -> R.string.framework_connection_failed_summary
    }

@StringRes
private fun FrameworkConnectionIssue?.errorSummary(): Int =
    when (this) {
        FrameworkConnectionIssue.WriteFailed -> R.string.framework_write_failed_summary
        else -> R.string.framework_connection_failed_summary
    }

private data class FrameworkPresentation(val label: String, val summary: String)

internal val ScreenHorizontalMargin = 12.dp
internal val ScreenContentMaxWidth = 600.dp
internal val WideWindowMinWidth = 600.dp
internal val ScreenTopSpacing = 12.dp
internal val ScreenBottomSpacing = 12.dp
private val CardSpacing = 12.dp
