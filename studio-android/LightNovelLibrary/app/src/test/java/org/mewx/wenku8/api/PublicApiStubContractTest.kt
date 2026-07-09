package org.mewx.wenku8.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mewx.wenku8.network.LightNetwork
import org.mewx.wenku8.network.LightUserSession

class PublicApiStubContractTest {
    @Test
    fun publicStubContractDocumentsUnavailableApiBehavior() {
        assertTrue(PublicApiStubContract.isPublicStub)
        assertEquals("stub", PublicApiStubContract.failureMessage)
        assertEquals("Unknown", Wenku8API.BASE_URL)
        assertEquals("Unknown", Wenku8API.REGISTER_URL)
    }

    @Test
    fun publicStubNetworkDoesNotPerformRequests() {
        assertEquals("a+b", LightNetwork.encodeToHttp("a b"))
        assertNull(LightNetwork.LightHttpPostConnection("https://example.test", Wenku8API.getMewxNovelList(Wenku8API.NovelSortedBy.lastUpdate, 1, Wenku8API.AppLanguage.SC)))
        assertNull(LightNetwork.LightHttpDownload("https://example.test/file"))
    }

    @Test
    fun publicStubSessionNeverLogsInButKeepsLocalCredentials() {
        LightUserSession.setUserInfo("name", "pwd")
        LightUserSession.setSession("session")

        assertEquals("name", LightUserSession.getUsernameOrEmail())
        assertEquals("pwd", LightUserSession.getPassword())
        assertEquals("session", LightUserSession.getSession())
        assertFalse(LightUserSession.getLogStatus())
        assertTrue(LightUserSession.isUserInfoSet())

        val loginResult = LightUserSession.doLoginFromGiven("next", null, null)

        assertEquals(Wenku8Error.ErrorCode.SYSTEM_4_NOT_LOGGED_IN, loginResult)
        assertEquals("next", LightUserSession.getUsernameOrEmail())
        assertEquals("", LightUserSession.getPassword())
        assertFalse(LightUserSession.getLogStatus())
    }
}
