package org.mewx.wenku8.util

data class SaveMigrationDirectorySelection(
    val path: String,
    val lastPathSegment: String,
) {
    val isValid: Boolean =
        lastPathSegment.endsWith(WENKU8_FOLDER_NAME) &&
            !path.contains(DCIM_FOLDER_NAME) &&
            !path.contains(PICTURE_FOLDER_NAME)

    val validPathAnalyticsValue: String = isValid.toString()

    val displayPath: String =
        path.replace(PRIMARY_TREE_PREFIX, "/")

    companion object {
        private const val WENKU8_FOLDER_NAME = "wenku8"
        private const val DCIM_FOLDER_NAME = "DCIM"
        private const val PICTURE_FOLDER_NAME = "Picture"
        private const val PRIMARY_TREE_PREFIX = "/tree/primary:"

        fun from(path: String?, lastPathSegment: String?): SaveMigrationDirectorySelection =
            SaveMigrationDirectorySelection(
                path = path.orEmpty(),
                lastPathSegment = lastPathSegment.orEmpty(),
            )
    }
}
