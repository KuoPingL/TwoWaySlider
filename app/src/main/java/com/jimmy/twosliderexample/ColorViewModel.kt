package com.jimmy.twosliderexample

import androidx.annotation.IntDef
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.lang.annotation.RetentionPolicy

/**
 * @author jimmy
 * Created 2022/9/26 at 11:45 下午
 */
class ColorViewModel: ViewModel() {

    val colorMode : LiveData<ColorType>
    get() = mColorMode

    val thumbFromColor: LiveData<Int>
    get() = mThumbFromColor

    val thumbToColor: LiveData<Int>
    get() = mThumbToColor

    val barColor: LiveData<Int>
    get() = mBarColor

    val selectedRangeColor: LiveData<Int>
    get() = mSelectedRangeColor

    val rgb: LiveData<ColorUtil.RGB>
    get() = mRGB

    fun setThumbFromColor(int: Int) {
        mThumbFromColor.value = int
    }

    fun setThumbToColor(int: Int) {
        mThumbToColor.value = int
    }

    fun setBarColor(int: Int) {
        mBarColor.value = int
    }

    fun setSelectedRangeColor(int: Int) {
        mSelectedRangeColor.value = int
    }

    fun selectComponent(colorType: ColorType) {

        when(colorType) {
            ColorType.BAR_COLOR -> {
                mBarColor.value?.let {
                    setRGB(ColorUtil.getRGBFromColor(it))
                }
            }

            ColorType.SELECTED_RANGE_COLOR -> {
                mSelectedRangeColor.value?.let {
                    setRGB(ColorUtil.getRGBFromColor(it))
                }
            }

            ColorType.THUMB_TO_COLOR -> {
                mThumbToColor.value?.let {
                    setRGB(ColorUtil.getRGBFromColor(it))
                }
            }

            ColorType.THUMB_FROM_COLOR -> {
                mThumbFromColor.value?.let {
                    setRGB(ColorUtil.getRGBFromColor(it))
                }
            }
        }

        mColorMode.value = colorType
    }

    fun setRGB(r: Int, g: Int, b: Int) {
        mRGB.value?.apply {
            this.r = r
            this.g = g
            this.b = b

            colorMode.value?.let {
                when(it) {
                    ColorType.BAR_COLOR -> {
                        setBarColor(ColorUtil.getColorFromRGB(this))
                    }

                    ColorType.SELECTED_RANGE_COLOR -> {
                        setSelectedRangeColor(ColorUtil.getColorFromRGB(this))
                    }

                    ColorType.THUMB_FROM_COLOR -> {
                        setThumbFromColor(ColorUtil.getColorFromRGB(this))
                    }

                    ColorType.THUMB_TO_COLOR -> {
                        setThumbToColor(ColorUtil.getColorFromRGB(this))
                    }
                }
            }
        }
    }

    fun setRGB(rgb: ColorUtil.RGB) {
        mRGB.value = rgb
    }




    private val mColorMode = MutableLiveData<ColorType>()
    private val mThumbFromColor = MutableLiveData<Int>()
    private val mThumbToColor = MutableLiveData<Int>()
    private val mBarColor = MutableLiveData<Int>()
    private val mSelectedRangeColor = MutableLiveData<Int>()
    private val mRGB = MutableLiveData<ColorUtil.RGB>()



    enum class ColorType {
        BAR_COLOR,
        THUMB_TO_COLOR,
        THUMB_FROM_COLOR,
        SELECTED_RANGE_COLOR;
    }
}