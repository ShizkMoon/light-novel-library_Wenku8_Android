package org.mewx.wenku8.util

enum class SaveMigrationStartAction {
    SKIP,
    PROMPT_FOR_DIRECTORY,
    RUN_MIGRATION,
}

data class SaveMigrationStartDecision(
    val action: SaveMigrationStartAction,
) {
    companion object {
        fun from(
            sdkInt: Int,
            migrationApi: Int,
            tiramisuApi: Int,
            migrationCompleted: Boolean,
            missingReadExternalStorage: Boolean,
            migrationEligible: Boolean,
        ): SaveMigrationStartDecision =
            SaveMigrationStartDecision(
                when {
                    sdkInt < migrationApi -> SaveMigrationStartAction.SKIP
                    migrationCompleted -> SaveMigrationStartAction.SKIP
                    sdkInt < tiramisuApi && missingReadExternalStorage -> SaveMigrationStartAction.SKIP
                    sdkInt >= tiramisuApi && migrationEligible -> SaveMigrationStartAction.PROMPT_FOR_DIRECTORY
                    else -> SaveMigrationStartAction.RUN_MIGRATION
                },
            )
    }
}
