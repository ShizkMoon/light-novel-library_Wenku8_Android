package org.mewx.wenku8.reader.slider

import kotlin.Metadata
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mewx.wenku8.reader.slider.base.BaseSlider
import org.mewx.wenku8.reader.slider.base.OverlappedSlider
import org.mewx.wenku8.reader.slider.base.PageSlider
import org.mewx.wenku8.reader.slider.base.Slider

class SliderMigrationContractTest {
    @Test
    fun legacySliderStackIsKotlinOwned() {
        assertKotlinClass(Slider::class.java)
        assertKotlinClass(BaseSlider::class.java)
        assertKotlinClass(PageSlider::class.java)
        assertKotlinClass(OverlappedSlider::class.java)
        assertKotlinClass(SlidingAdapter::class.java)
        assertKotlinClass(SlidingLayout::class.java)
    }

    @Test
    fun baseSliderKeepsJavaVisibleDirectionConstants() {
        assertEquals(0, BaseSlider.MOVE_TO_LEFT)
        assertEquals(1, BaseSlider.MOVE_TO_RIGHT)
        assertEquals(4, BaseSlider.MOVE_NO_RESULT)
    }

    private fun assertKotlinClass(clazz: Class<*>) {
        assertNotNull("${clazz.simpleName} should be compiled from Kotlin", clazz.getAnnotation(Metadata::class.java))
    }
}
