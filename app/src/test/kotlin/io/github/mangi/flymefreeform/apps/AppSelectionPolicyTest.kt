package io.github.mangi.flymefreeform.apps

import org.junit.Assert.assertEquals
import org.junit.Test

class AppSelectionPolicyTest {
    @Test
    fun firstUseFallsBackToRecentThenAll() {
        assertEquals(
            listOf("recent", "other"),
            AppSelectionPolicy.radialItems(false, emptyList(), listOf("recent"), listOf("recent", "other"), { it }, 6),
        )
    }

    @Test
    fun savedPinsSkipUnavailableEntriesAndNeverFallBack() {
        assertEquals(
            listOf("available"),
            AppSelectionPolicy.radialItems(true, listOf("available"), listOf("recent"), listOf("other"), { it }, 6),
        )
        assertEquals(
            emptyList<String>(),
            AppSelectionPolicy.radialItems(true, emptyList(), listOf("recent"), listOf("other"), { it }, 6),
        )
    }

    /** @author bomo 开关开启：外圈固定项不足 6 个时，用最近使用补齐，且补齐项排除已固定项。 */
    @Test
    fun fillKeepsPinsThenFillsUpToOuterLimitFromRecents() {
        val (radial, outerCount) =
            AppSelectionPolicy.pinnedRadialItems(
                outerPins = listOf("pinned1", "pinned2", "pinned3"),
                innerPins = listOf("inner"),
                recent = listOf("recent1", "recent2", "recent3", "recent4"),
                all = listOf("other"),
                identity = { it },
                outerLimit = 6,
                fillOuterWithRecent = true,
            )

        assertEquals(listOf("pinned1", "pinned2", "pinned3", "recent1", "recent2", "recent3", "inner"), radial)
        assertEquals(6, outerCount)
    }

    /** @author bomo 补齐排除已固定的外圈 / 内圈应用；最近不足时由全量兜底。 */
    @Test
    fun fillExcludesPinnedEntriesAndFallsBackToAll() {
        val (radial, outerCount) =
            AppSelectionPolicy.pinnedRadialItems(
                outerPins = listOf("pinned1"),
                innerPins = listOf("recent1"),
                recent = listOf("recent1", "recent2", "recent3"),
                all = listOf("all1", "all2", "all3", "all4", "all5", "all6"),
                identity = { it },
                outerLimit = 6,
                fillOuterWithRecent = true,
            )

        // recent1 已被内圈固定，跳过；最近剩余 recent2/recent3 不足 5 个，由 all 补足。
        assertEquals(listOf("pinned1", "recent2", "recent3", "all1", "all2", "all3", "recent1"), radial)
        assertEquals(6, outerCount)
    }

    /** @author bomo 开关关闭：外圈保持固定项原样，不补齐。 */
    @Test
    fun fillDisabledKeepsPinsOnly() {
        val (radial, outerCount) =
            AppSelectionPolicy.pinnedRadialItems(
                outerPins = listOf("pinned1", "pinned2"),
                innerPins = listOf("inner"),
                recent = listOf("recent1", "recent2", "recent3", "recent4"),
                all = emptyList(),
                identity = { it },
                outerLimit = 6,
                fillOuterWithRecent = false,
            )

        assertEquals(listOf("pinned1", "pinned2", "inner"), radial)
        assertEquals(2, outerCount)
    }

    /** @author bomo 外圈已满（6 个）时不补齐；外圈无固定项时全部由最近使用补齐。 */
    @Test
    fun fillRespectsOuterLimitAndEmptyPins() {
        val full =
            AppSelectionPolicy.pinnedRadialItems(
                outerPins = listOf("a", "b", "c", "d", "e", "f"),
                innerPins = emptyList(),
                recent = listOf("recent1", "recent2", "recent3", "recent4"),
                all = emptyList(),
                identity = { it },
                outerLimit = 6,
                fillOuterWithRecent = true,
            )
        assertEquals(listOf("a", "b", "c", "d", "e", "f"), full.first)
        assertEquals(6, full.second)

        val empty =
            AppSelectionPolicy.pinnedRadialItems(
                outerPins = emptyList(),
                innerPins = listOf("inner"),
                recent = listOf("recent1", "recent2", "recent3", "recent4", "recent5", "recent6", "recent7"),
                all = emptyList(),
                identity = { it },
                outerLimit = 6,
                fillOuterWithRecent = true,
            )
        assertEquals(listOf("recent1", "recent2", "recent3", "recent4", "recent5", "recent6", "inner"), empty.first)
        assertEquals(6, empty.second)
    }
}
