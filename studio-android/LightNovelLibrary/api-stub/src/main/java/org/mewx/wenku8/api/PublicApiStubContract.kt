package org.mewx.wenku8.api

class PublicApiStubContract private constructor() {
    companion object {
        @JvmField
        val isPublicStub: Boolean = true

        const val failureMessage: String = "stub"
    }
}
