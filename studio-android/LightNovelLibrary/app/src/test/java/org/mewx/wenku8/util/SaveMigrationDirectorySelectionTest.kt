package org.mewx.wenku8.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveMigrationDirectorySelectionTest {
    @Test
    fun acceptsWenku8RootFolder() {
        val selection = SaveMigrationDirectorySelection.from(
            path = "/tree/primary:wenku8",
            lastPathSegment = "primary:wenku8"
        )

        assertTrue(selection.isValid)
        assertEquals("true", selection.validPathAnalyticsValue)
    }

    @Test
    fun rejectsFoldersThatAreNotNamedWenku8() {
        val selection = SaveMigrationDirectorySelection.from(
            path = "/tree/primary:Download",
            lastPathSegment = "primary:Download"
        )

        assertFalse(selection.isValid)
        assertEquals("false", selection.validPathAnalyticsValue)
    }

    @Test
    fun rejectsCameraAndPictureDirectoriesEvenWhenTheyEndWithWenku8() {
        val dcimSelection = SaveMigrationDirectorySelection.from(
            path = "/tree/primary:DCIM/wenku8",
            lastPathSegment = "primary:DCIM/wenku8"
        )
        val pictureSelection = SaveMigrationDirectorySelection.from(
            path = "/tree/primary:Picture/wenku8",
            lastPathSegment = "primary:Picture/wenku8"
        )

        assertFalse(dcimSelection.isValid)
        assertFalse(pictureSelection.isValid)
    }

    @Test
    fun formatsWrongPathForUserDialog() {
        val selection = SaveMigrationDirectorySelection.from(
            path = "/tree/primary:Download",
            lastPathSegment = "primary:Download"
        )

        assertEquals("/Download", selection.displayPath)
    }
}
