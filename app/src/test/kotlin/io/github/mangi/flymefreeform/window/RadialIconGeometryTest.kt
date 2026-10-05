package io.github.mangi.flymefreeform.window

import io.github.mangi.flymefreeform.gesture.CornerSide
import io.github.mangi.flymefreeform.gesture.RadialGeometry
import kotlin.math.hypot
import kotlin.math.sin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RadialIconGeometryTest {
    /** 9 个固定应用 + 「更多」= 10 个槽位，是当前设计上限。 */
    private val maxSlots = 10

    private fun fit(width: Float = 400f, height: Float = 890f, density: Float = 1f,
                    insets: OverlaySafeInsets = OverlaySafeInsets(), count: Int = 7) =
        RadialIconGeometry.fit(width, height, density, insets, count)

    @Test
    fun baselineMatchesFixedDimensionsForEveryItemCount() {
        for (count in 1..7) {
            val metrics = fit(count = count)
            assertEquals(236f, metrics.radius, 0.001f)
            assertEquals(47.5f, metrics.iconDiameter, 0.001f)
            assertEquals(metrics.iconDiameter, metrics.plateDiameter, 0f)
            assertEquals(1.5f, metrics.itemPadding, 0.001f)
            assertEquals(1f, metrics.pixelsPerBaseDp, 0.001f)
        }
    }

    @Test
    fun largerScreensScaleRadiusIconsAndHitTargetsTogether() {
        val baseline = fit()
        for (factor in listOf(0.4f, 0.8f, 1.5f, 2.1f)) {
            val metrics = fit(width = 400f * factor, height = 890f * factor)
            assertEquals(baseline.radius * factor, metrics.radius, 0.001f)
            assertEquals(baseline.iconDiameter * factor, metrics.iconDiameter, 0.001f)
            assertEquals(baseline.selectionEnterRadius * factor, metrics.selectionEnterRadius, 0.001f)
            assertEquals(baseline.selectionKeepRadius * factor, metrics.selectionKeepRadius, 0.001f)
        }
    }

    @Test
    fun equivalentDpWindowsHaveEquivalentGeometryAcrossDensities() {
        val expected = fit()
        for (density in listOf(1f, 2.75f, 3f, 3.5f, 4f)) {
            val actual = fit(400f * density, 890f * density, density)
            assertEquals(expected.radius, actual.radius / density, 0.001f)
            assertEquals(expected.iconDiameter, actual.iconDiameter / density, 0.001f)
        }
        // 同一像素窗口改变系统显示大小，屏幕占比保持一致。
        assertEquals(fit(density = 1f), fit(density = 2f))
    }

    /** @author bomo 切换跟手回归：keepRadius（0.9×直径）恒小于相邻槽位弦长，
     *  手指滑到目标图标中心时距原选中已超 keep、必然切换（旧 1.25× 会粘滞到滑过目标）。 */
    @Test
    fun keepRadiusAlwaysBelowAdjacentChord() {
        for (count in 1..maxSlots) {
            val metrics = fit(count = count)
            val stepRadians = 84f / count * kotlin.math.PI.toFloat() / 180f
            val chord = 2f * metrics.radius * sin(stepRadians / 2f)
            assertTrue(
                "keep=${metrics.selectionKeepRadius} chord=$chord count=$count",
                metrics.selectionKeepRadius < chord,
            )
        }
    }

    @Test
    fun rotationKeepsShortEdgeScale() {
        assertEquals(fit(400f, 890f), fit(890f, 400f))
    }

    /** @author bomo 扇形半径缩放：两圈与选中判定半径同比例变化，图标直径不动。 */
    @Test
    fun radiusScaleScalesRingsButNotIcons() {
        val baseline = RadialIconGeometry.fit(400f, 890f, 1f, OverlaySafeInsets(), 7, innerCount = 3)
        val enlarged =
            RadialIconGeometry.fit(400f, 890f, 1f, OverlaySafeInsets(), 7, innerCount = 3, radiusScale = 1.3f)
        assertEquals(baseline.radius * 1.3f, enlarged.radius, 0.001f)
        assertEquals(baseline.innerRadius * 1.3f, enlarged.innerRadius, 0.001f)
        assertEquals(baseline.selectionEnterRadius * 1.3f, enlarged.selectionEnterRadius, 0.001f)
        assertEquals(baseline.selectionKeepRadius * 1.3f, enlarged.selectionKeepRadius, 0.001f)
        assertEquals(baseline.iconDiameter, enlarged.iconDiameter, 0.001f)
        assertEquals(baseline.innerIconDiameter, enlarged.innerIconDiameter, 0.001f)
    }

    /** @author bomo 半径缩小端同比例生效，且默认 1f 时行为与旧版完全一致。 */
    @Test
    fun radiusScaleShrinksRingsAndDefaultIsIdentity() {
        val full = RadialIconGeometry.fit(400f, 890f, 1f, OverlaySafeInsets(), 7, innerCount = 3)
        assertEquals(full, RadialIconGeometry.fit(400f, 890f, 1f, OverlaySafeInsets(), 7, innerCount = 3, radiusScale = 1f))
        val shrunk =
            RadialIconGeometry.fit(400f, 890f, 1f, OverlaySafeInsets(), 7, innerCount = 3, radiusScale = 0.6f)
        assertEquals(full.radius * 0.6f, shrunk.radius, 0.001f)
        assertEquals(full.selectionKeepRadius * 0.6f, shrunk.selectionKeepRadius, 0.001f)
    }

    /** @author bomo 半径放大超出屏幕安全区时整体压缩兜底，外缘不越界。 */
    @Test
    fun radiusScaleOverflowIsClampedBySafeArea() {
        val insets = OverlaySafeInsets(left = 60f, right = 60f, top = 80f, bottom = 120f)
        val baseline = fit(insets = insets)
        val enlarged =
            RadialIconGeometry.fit(400f, 890f, 1f, insets, 7, radiusScale = 1.5f)
        assertTrue(enlarged.radius > baseline.radius)
        val safeWidth = 400f - insets.left - insets.right
        val safeHeight = 890f - insets.top - insets.bottom
        val outerExtent = enlarged.radius + enlarged.iconDiameter / 2f
        assertTrue(outerExtent <= safeWidth + 0.01f)
        assertTrue(outerExtent <= safeHeight + 0.01f)
    }

    @Test
    fun constrainedSpaceShrinksEverythingWithoutDependingOnCount() {
        val insets = OverlaySafeInsets(left = 170f, right = 170f, top = 50f, bottom = 100f)
        val full = fit()
        val narrow = fit(insets = insets)
        assertTrue(narrow.radius < full.radius)
        assertEquals(236f / 47.5f, narrow.radius / narrow.iconDiameter, 0.001f)
        assertEquals(0.9f, narrow.selectionEnterRadius / narrow.iconDiameter, 0.001f)
        // @author bomo keep = 0.9× 图标直径 —— 跟手切换（= enter，更灵敏）且保留防抖/跨圈裕度。
        assertEquals(0.9f, narrow.selectionKeepRadius / narrow.iconDiameter, 0.001f)
        for (count in 1..7) assertEquals(narrow, fit(insets = insets, count = count))
    }

    @Test
    fun crowdedSlotsShrinkIconsAndKeepRadius() {
        val crowded = fit(count = maxSlots)
        // 半径不变，只有图标按弦长收紧：这是"多放几个"而不改扇形外缘的前提。
        assertEquals(236f, crowded.radius, 0.001f)
        assertTrue(crowded.iconDiameter < fit(count = 7).iconDiameter)
        assertTrue(crowded.iconDiameter >= 30f)
        var previous = Float.MAX_VALUE
        for (count in 7..maxSlots) {
            val current = fit(count = count).iconDiameter
            assertTrue(current <= previous)
            previous = current
        }
    }

    @Test
    fun selectionRingsStayInsideBothSafeCornersAndDoNotOverlap() {
        for ((width, height) in listOf(160f to 300f, 400f to 890f, 890f to 400f, 840f to 1100f)) {
            for (insets in listOf(OverlaySafeInsets(), OverlaySafeInsets(30f, 70f, 90f, 120f))) {
                for (count in 1..maxSlots) {
                    val metrics = fit(width, height, insets = insets, count = count)
                    val selectedRadius = metrics.iconDiameter / 2f
                    val padding = metrics.itemPadding
                    for (side in CornerSide.entries) {
                        val layout = RadialGeometry.layout(side,
                            width - insets.left - insets.right, height - insets.top - insets.bottom,
                            metrics.radius, count, insets.left, insets.top)
                        layout.itemCenters.forEachIndexed { index, center ->
                            val extent = selectedRadius + padding
                            assertTrue(center.x - extent >= insets.left - 0.001f)
                            assertTrue(center.x + extent <= width - insets.right + 0.001f)
                            assertTrue(center.y - extent >= insets.top - 0.001f)
                            assertTrue(center.y + extent <= height - insets.bottom + 0.001f)
                            layout.itemCenters.drop(index + 1).forEach { other ->
                                // 0.01dp 容差：弦长由 sin 反推，取等时应视为不重叠。
                                assertTrue(hypot(center.x - other.x, center.y - other.y) + 0.01f >=
                                    selectedRadius + padding + metrics.iconDiameter / 2f)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @author bomo 内圈与外圈必须留足径向间隙（用户 2026-09-22 反馈两圈太近、滑选易误触）。
     * 该间隙在算法里是恒等量（与图标直径无关），这里钉住下限防止以后被调回去。
     */
    @Test
    fun innerRingKeepsEnoughGapFromOuterRing() {
        for (outerCount in 1..maxSlots) {
            for (innerCount in 1..5) {
                val metrics =
                    RadialIconGeometry.fit(
                        400f, 890f, 1f, OverlaySafeInsets(), outerCount, innerCount,
                    )
                val outerInnerEdge = metrics.radius - metrics.iconDiameter / 2f
                val innerOuterEdge = metrics.innerRadius + metrics.innerIconDiameter / 2f
                val gap = outerInnerEdge - innerOuterEdge
                // 18f 为 RING_GAP_DP 的精确值；浮点链可能差出 ~1e-5，留 1e-3 容差。
                assertTrue("gap=$gap outer=$outerCount inner=$innerCount", gap >= 18f - 0.001f)
                assertTrue("inner must be inside outer", metrics.innerRadius > 0f)
                assertTrue(metrics.innerRadius < metrics.radius)
                // 误触的根源不是"看着近"，而是两圈中心距 ≤ 保持半径：
                // RadialGeometry.selection 只要手指还在原选中的 keepRadius 内就"粘住"不换，
                // 中心距一旦 ≤ keepRadius，滑到另一圈图标上仍被判为原选中，跨圈切不过去。
                val ringDistance = metrics.radius - metrics.innerRadius
                assertTrue(
                    "ringDistance=$ringDistance must exceed keepRadius=${metrics.selectionKeepRadius} " +
                        "(outer=$outerCount inner=$innerCount)",
                    ringDistance > metrics.selectionKeepRadius,
                )
            }
        }
    }

    @Test
    fun exhaustedSafeAreaProducesHiddenGeometry() {
        val metrics = fit(insets = OverlaySafeInsets(left = 400f))
        assertEquals(0f, metrics.radius, 0f)
        assertEquals(0f, metrics.iconDiameter, 0f)
    }
}
