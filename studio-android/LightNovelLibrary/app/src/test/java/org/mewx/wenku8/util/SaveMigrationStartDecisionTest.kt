package org.mewx.wenku8.util

import org.junit.Assert.assertEquals
import org.junit.Test

class SaveMigrationStartDecisionTest {
    @Test
    fun skipsBeforeScopedStorageMigrationApi() {
        val decision = SaveMigrationStartDecision.from(
            sdkInt = MIGRATION_API - 1,
            migrationApi = MIGRATION_API,
            tiramisuApi = TIRAMISU_API,
            migrationCompleted = false,
            missingReadExternalStorage = false,
            migrationEligible = true,
        )

        assertEquals(SaveMigrationStartAction.SKIP, decision.action)
    }

    @Test
    fun skipsWhenMigrationAlreadyCompleted() {
        val decision = SaveMigrationStartDecision.from(
            sdkInt = MIGRATION_API,
            migrationApi = MIGRATION_API,
            tiramisuApi = TIRAMISU_API,
            migrationCompleted = true,
            missingReadExternalStorage = false,
            migrationEligible = true,
        )

        assertEquals(SaveMigrationStartAction.SKIP, decision.action)
    }

    @Test
    fun skipsBeforeTiramisuWhenReadExternalStoragePermissionIsMissing() {
        val decision = SaveMigrationStartDecision.from(
            sdkInt = TIRAMISU_API - 1,
            migrationApi = MIGRATION_API,
            tiramisuApi = TIRAMISU_API,
            migrationCompleted = false,
            missingReadExternalStorage = true,
            migrationEligible = true,
        )

        assertEquals(SaveMigrationStartAction.SKIP, decision.action)
    }

    @Test
    fun promptsForDirectoryOnTiramisuAndNewerWhenLegacyExternalSavesExist() {
        val decision = SaveMigrationStartDecision.from(
            sdkInt = TIRAMISU_API,
            migrationApi = MIGRATION_API,
            tiramisuApi = TIRAMISU_API,
            migrationCompleted = false,
            missingReadExternalStorage = true,
            migrationEligible = true,
        )

        assertEquals(SaveMigrationStartAction.PROMPT_FOR_DIRECTORY, decision.action)
    }

    @Test
    fun runsMigrationWhenScopedStorageMigrationCanProceedWithoutDirectoryPrompt() {
        val decision = SaveMigrationStartDecision.from(
            sdkInt = MIGRATION_API,
            migrationApi = MIGRATION_API,
            tiramisuApi = TIRAMISU_API,
            migrationCompleted = false,
            missingReadExternalStorage = false,
            migrationEligible = false,
        )

        assertEquals(SaveMigrationStartAction.RUN_MIGRATION, decision.action)
    }

    private companion object {
        private const val MIGRATION_API = 30
        private const val TIRAMISU_API = 33
    }
}
