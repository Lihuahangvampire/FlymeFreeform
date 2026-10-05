package io.github.mangi.flymefreeform.config

import android.content.ComponentName

/** App 与 Hook 进程共享的框架配置协议；已发布的名称和类型不可随意复用。 */
internal object ModulePreferences {
    const val GROUP = "module_runtime"
    const val KEY_MODULE_ENABLED = "enabled"
    const val KEY_LEFT_CORNER_ENABLED = "corner_left_enabled"
    const val KEY_RIGHT_CORNER_ENABLED = "corner_right_enabled"
    const val KEY_CORNER_TRIGGER_RANGE_DP = "corner_trigger_range_dp"

    /** @author bomo 触发热区形状（存 [TriggerShape.storedValue]）。 */
    const val KEY_TRIGGER_SHAPE = "trigger_shape_v1"

    /** @author bomo 触发热区横向长度（沿屏幕底边，dp）；矩形 / 三角形使用。 */
    const val KEY_TRIGGER_HORIZONTAL_DP = "trigger_horizontal_dp_v1"

    /** @author bomo 触发热区纵向高度（沿屏幕侧边，dp）；矩形 / 三角形使用。 */
    const val KEY_TRIGGER_VERTICAL_DP = "trigger_vertical_dp_v1"
    const val KEY_CORNER_PINS = "corner_pins_v1"
    const val KEY_OUTSIDE_TAP_CLOSE_MODE = "outside_tap_close_mode_v1"
    const val KEY_HANDLE_SWIPE_UP_TO_MINI_ENABLED = "handle_swipe_up_to_mini_enabled"
    const val KEY_PAUSE_IN_LANDSCAPE = "pause_in_landscape"
    const val KEY_PAUSE_IN_GAME_MODE = "pause_in_game_mode"

    /** 最近以小窗打开的应用（system_server 写入，侧边栏进程读取后渲染「最近小窗」区块）。 */
    const val KEY_RECENT_FREEFORM = "recent_freeform_v1"

    /** 内圈固定应用（v2 配置，与外圈 KEY_CORNER_PINS 相互独立）。 */
    const val KEY_CORNER_INNER_PINS = "corner_inner_pins_v1"

    /**
     * @author bomo 外圈固定应用未满（少于 [OUTER_PINNED_APPS]）时，用最近使用的应用补齐
     * （排除已固定的外圈 / 内圈应用）。默认关闭，保持「固定几个就显示几个」的既有行为。
     */
    const val KEY_FILL_OUTER_WITH_RECENT = "fill_outer_with_recent_v1"
    const val DEFAULT_FILL_OUTER_WITH_RECENT = false

    /**
     * 侧边栏工具目录（侧边栏进程写入，App 与 system_server 读取）。
     * 工具（小布识屏 / 屏幕翻译 / 截屏等）不是独立应用，无法用 LauncherApps 枚举，
     * 只能由侧边栏进程从原厂 ToolEntryHelper 导出。
     */
    const val KEY_TOOL_CATALOG = "tool_catalog_v1"

    /**
     * @author bomo 「全部」面板整体缩放百分比（80 = 原尺寸的 80%）。
     * 写入方是模块 App（设置界面的滑条），侧边栏进程开面板时按只读方式取值，
     * 因此改完滑条**下次打开面板即生效**，无需重启。
     */
    const val KEY_PANEL_SCALE_PERCENT = "panel_scale_percent_v1"
    const val DEFAULT_PANEL_SCALE_PERCENT = 80
    const val MIN_PANEL_SCALE_PERCENT = 50
    const val MAX_PANEL_SCALE_PERCENT = 100

    /**
     * @author bomo 扇形外圈 / 内圈半径缩放百分比（100 = 原尺寸）。
     * 写入方是模块 App（设置界面的滑条），侧边栏进程每次呼出扇形时按只读方式取值，
     * 因此改完滑条**下次呼出即生效**，无需重启。两圈同心缩放，间距随比例等比变化。
     */
    const val KEY_RADIAL_RADIUS_PERCENT = "radial_radius_percent_v1"
    const val DEFAULT_RADIAL_RADIUS_PERCENT = 100
    const val MIN_RADIAL_RADIUS_PERCENT = 60
    const val MAX_RADIAL_RADIUS_PERCENT = 150

    /**
     * @author bomo 扇形选中圈描边厚度缩放百分比（100 = 默认 3.25dp 观感）。
     * 写入方是模块 App（设置界面的滑条），侧边栏进程每次呼出扇形时按只读方式取值，
     * 改完滑条**下次呼出即生效**。范围 50% ~ 250%。
     */
    const val KEY_SELECTION_RING_WIDTH_PERCENT = "selection_ring_width_percent_v1"
    const val DEFAULT_SELECTION_RING_WIDTH_PERCENT = 100
    const val MIN_SELECTION_RING_WIDTH_PERCENT = 50
    const val MAX_SELECTION_RING_WIDTH_PERCENT = 250

    /**
     * @author bomo 扇形选中圈颜色（ARGB Int；[SELECTION_RING_COLOR_SYSTEM] 表示跟随系统强调蓝）。
     * 写入方是模块 App（设置界面的色板），侧边栏进程每次呼出扇形时按只读方式取值。
     */
    const val KEY_SELECTION_RING_COLOR = "selection_ring_color_v1"

    /** 选中圈颜色取「跟随系统」时的特殊存储值（默认；解析失败回退 ColorOS 风格蓝）。 */
    const val SELECTION_RING_COLOR_SYSTEM = -2

    /**
     * @author bomo 选中圈可调色板（不含「跟随系统」项）：ARGB Int 列表。
     * 顺序即设置界面色块展示顺序；「跟随系统」色块固定在列表最前。
     */
    val SELECTION_RING_COLOR_PRESETS: List<Int> =
        listOf(
            0xFF0A84FF.toInt(), // ColorOS 风格蓝
            0xFFFFFFFF.toInt(), // 白
            0xFF34C759.toInt(), // 绿
            0xFFFF9500.toInt(), // 橙
            0xFFFF3B30.toInt(), // 红
            0xFFAF52DE.toInt(), // 紫
        )

    const val DEFAULT_ENABLED = false
    const val DEFAULT_CORNER_ENABLED = true
    const val DEFAULT_CORNER_TRIGGER_RANGE_DP = 84
    val DEFAULT_OUTSIDE_TAP_CLOSE_MODE = OutsideTapCloseMode.SingleTap
    const val DEFAULT_HANDLE_SWIPE_UP_TO_MINI_ENABLED = true
    const val DEFAULT_PAUSE_IN_LANDSCAPE = true
    const val DEFAULT_PAUSE_IN_GAME_MODE = true
    const val MIN_CORNER_TRIGGER_RANGE_DP = 24
    const val MAX_CORNER_TRIGGER_RANGE_DP = 160

    /**
     * @author bomo 触发热区横/纵向长度（三角形用）的取值范围（dp）。
     * **与扇形半径共用同一上下限**（用户 2026-09-22 定）：三者在热区里同属"延伸长度"，
     * 各留一套数字只会让口径漂移；上限一致后，三角形拉到最大与扇形最大半径等价。
     */
    const val MIN_TRIGGER_EXTENT_DP = MIN_CORNER_TRIGGER_RANGE_DP
    const val MAX_TRIGGER_EXTENT_DP = MAX_CORNER_TRIGGER_RANGE_DP

    /**
     * 扇形最多固定几个应用（不含「更多」）：外圈最多 6 个 + 内圈最多 5 个 = 11。
     * 固定顺序即圈层顺序：前 6 个在外圈，第 7~11 个进内圈。
     */
    const val MAX_PINNED_APPS = 11

    /** 外圈应用上限。 */
    const val OUTER_PINNED_APPS = 6

    /** 内圈应用上限。 */
    const val MAX_INNER_APPS = 5

    /** 「最近小窗」区块最多展示几个最近应用；超出按时间截断（最近的在最左）。 */
    const val MAX_RECENT_FREEFORM = 8

    /**
     * 工具在固定列表里的伪包名：`ComponentName(TOOL_PACKAGE, alias)`。
     * 复用既有 ComponentName 存储与去重逻辑，不新增一套固定项格式。
     */
    const val TOOL_PACKAGE = "io.github.mangi.flymefreeform.tool"

    fun isToolComponent(component: ComponentName): Boolean =
        component.packageName == TOOL_PACKAGE && component.className.isNotEmpty()

    fun coerceCornerTriggerRangeDp(value: Int): Int =
        value.coerceIn(MIN_CORNER_TRIGGER_RANGE_DP, MAX_CORNER_TRIGGER_RANGE_DP)

    /** @author bomo 触发热区横/纵向长度钳制（三角形用）。 */
    fun coerceTriggerExtentDp(value: Int): Int =
        value.coerceIn(MIN_TRIGGER_EXTENT_DP, MAX_TRIGGER_EXTENT_DP)

    /** @author bomo 面板缩放百分比钳制；下限防止条目小到点不中，上限即原尺寸。 */
    fun coercePanelScalePercent(value: Int): Int =
        value.coerceIn(MIN_PANEL_SCALE_PERCENT, MAX_PANEL_SCALE_PERCENT)

    /** @author bomo 扇形半径缩放百分比钳制；下限避免图标贴得太近，上限由屏幕安全区兜底。 */
    fun coerceRadialRadiusPercent(value: Int): Int =
        value.coerceIn(MIN_RADIAL_RADIUS_PERCENT, MAX_RADIAL_RADIUS_PERCENT)

    /** @author bomo 选中圈描边厚度缩放百分比钳制；下限保证选中圈可见，上限避免盖满图标。 */
    fun coerceSelectionRingWidthPercent(value: Int): Int =
        value.coerceIn(MIN_SELECTION_RING_WIDTH_PERCENT, MAX_SELECTION_RING_WIDTH_PERCENT)
}
