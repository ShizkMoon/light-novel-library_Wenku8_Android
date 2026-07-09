package org.mewx.wenku8.reader.slider.base

abstract class BaseSlider : Slider {
    companion object {
        const val MOVE_TO_LEFT: Int = 0
        const val MOVE_TO_RIGHT: Int = 1
        const val MOVE_NO_RESULT: Int = 4

        const val MODE_NONE: Int = 0
        const val MODE_MOVE: Int = 1
    }
}
