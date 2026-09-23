package io.github.mangi.flymefreeform.apps

internal object AppSelectionPolicy {
    fun <T> radialItems(
        pinsSaved: Boolean,
        availablePins: List<T>,
        recent: List<T>,
        all: List<T>,
        identity: (T) -> Any,
        limit: Int,
    ): List<T> =
        if (pinsSaved) {
            availablePins.distinctBy(identity).take(limit)
        } else {
            (recent + all).distinctBy(identity).take(limit)
        }

    /**
     * @author bomo 双圈固定模式下的扇形条目：
     * 外圈 = 外圈固定项 +（可选）最近使用补齐，内圈 = 内圈固定项（顺序各自保留）。
     *
     * 补齐规则：`fillOuterWithRecent` 为真且外圈固定项不足 [outerLimit] 时，
     * 依次取「最近使用 → 全量」中**未被固定**（既不在外圈也不在内圈）的项填满空缺；
     * 最近使用不足时由全量兜底，仍不足则外圈保持实际数量。
     *
     * @return `Pair(径向顺序列表, 外圈数量)`；外圈数量 = 外圈固定项 + 实际补齐数量，
     * 双圈布局（`AppCatalogSnapshot.outerCount`）以此划分外 / 内圈。
     */
    fun <T> pinnedRadialItems(
        outerPins: List<T>,
        innerPins: List<T>,
        recent: List<T>,
        all: List<T>,
        identity: (T) -> Any,
        outerLimit: Int,
        fillOuterWithRecent: Boolean,
    ): Pair<List<T>, Int> {
        val excluded = (outerPins + innerPins).mapTo(HashSet()) { identity(it) }
        val fillCount = if (fillOuterWithRecent) (outerLimit - outerPins.size).coerceAtLeast(0) else 0
        val fill =
            if (fillCount == 0) {
                emptyList()
            } else {
                (recent + all)
                    .filterNot { identity(it) in excluded }
                    .distinctBy(identity)
                    .take(fillCount)
            }
        return (outerPins + fill + innerPins) to (outerPins.size + fill.size)
    }

    fun <T> panelItems(
        recent: List<T>,
        all: List<T>,
        excluded: Set<Any>,
        identity: (T) -> Any,
    ): List<T> =
        (recent + all).distinctBy(identity).filterNot { identity(it) in excluded }
}
