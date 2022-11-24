package com.jimmy.twosliderexample

import android.graphics.Color

/**
 * @author jimmy
 * Created 2022/10/2 at 11:24 PM
 */
object ColorUtil {

    class RGB(var r: Int , var g: Int, var b: Int)

    fun getRGBFromColor(color: Int) = color.toRgb()
    fun getColorFromRGB(rgb: RGB) = rgb.toColor()

    private fun Int.toRgb(): RGB {
        val r = this.shr(16) and 0xff
        val g = this.shr(8) and 0xff
        val b = this and 0xff

        return RGB(r,g,b)
    }

    private fun RGB.toColor(): Int {
        return Color.rgb(this.r, this.g, this.b)
    }
}