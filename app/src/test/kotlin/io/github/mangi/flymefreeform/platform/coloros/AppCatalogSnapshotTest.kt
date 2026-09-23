package io.github.mangi.flymefreeform.platform.coloros

import io.github.mangi.flymefreeform.config.ModuleSettingsSnapshot
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppCatalogSnapshotTest {
    @Test
    fun lateCatalogCannotReplaceNewerSelection() {
        val requested = ModuleSettingsSnapshot()
        val completed = AppCatalogSnapshot(settings = requested)
        assertTrue(completed.matches(requested))
        assertFalse(completed.matches(requested.copy(pinsSaved = true)))
    }

    @Test
    fun unrelatedGestureSettingsDoNotInvalidatePreparedIcons() {
        val requested = ModuleSettingsSnapshot()
        val completed = AppCatalogSnapshot(settings = requested)
        assertTrue(completed.matches(requested.copy(cornerTriggerRangeDp = 100, enabled = true)))
    }

    /** @author bomo 外圈补齐开关变化会使已就绪的目录失效（需按新策略重建）。 */
    @Test
    fun fillOuterWithRecentSwitchInvalidatesPreparedCatalog() {
        val requested = ModuleSettingsSnapshot()
        val completed = AppCatalogSnapshot(settings = requested)
        assertFalse(completed.matches(requested.copy(fillOuterWithRecent = true)))
    }
}
