package com.jimmy.twosliderexample

import android.util.Log
import org.junit.Test

/**
 * @author jimmy
 * Created 2022/10/3 at 10:06 AM
 */
class ColorConvertionTest {

    @Test
    fun getRed() {
        val r = 255
        val rb = r.shl(16)
        val color = ColorUtil.getRGBFromColor(rb)

        assert(color.r == r) {
            "Result : ${color.r} Expect : $r Byte : $rb"
        }
    }

    @Test
    fun getGreen() {
        val g = 100
        val gb = g.shl(8)
        val color = ColorUtil.getRGBFromColor(gb)
        assert(color.g == g) {
            "Result : ${color.g} Expect : $g Byte : $gb"
        }
    }

    @Test
    fun getBlue() {
        val b = 180
        val color = ColorUtil.getRGBFromColor(b)
        assert(color.b == b) {
            "Result : ${color.b} Expect : $b"
        }
    }

    @Test
    fun getRedByte() {
        val r = 50
        val rgb = ColorUtil.RGB(r, 0, 0)
        val rb = r.shl(16)
        val color = ColorUtil.getColorFromRGB(rgb)
        assert(color == rb) {
            "Result : ${color} Expect : $rb"
        }
    }

    @Test
    fun getRedGreenBlueByte() {
        val r = 50
        val g = 100
        val b = 20
        val rgb = ColorUtil.RGB(r, g, b)
        val rgbb = r.shl(16) or g.shl(8) or b
        val color = ColorUtil.getColorFromRGB(rgb)
        assert(color == rgbb) {
            "Result : ${color} Expect : $rgbb"
        }
    }

}