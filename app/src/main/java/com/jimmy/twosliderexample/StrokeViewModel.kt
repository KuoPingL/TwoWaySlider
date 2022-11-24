package com.jimmy.twosliderexample

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * @author jimmy
 * Created 2022/9/29 at 5:15 下午
 */
class StrokeViewModel : ViewModel() {
    val barStrokeWidth: LiveData<Int>
    get() = mBarStrokeWidth
    val thumbStrokeWidth: LiveData<Int>
    get() = mThumbStrokeWidth
    val selectedRangeStokeWidth: LiveData<Int>
    get() = mSelectedRangeStrokeWidth

    fun setBarStrokeWidth(width: Int) {
        mBarStrokeWidth.value = width
    }

    fun setThumbStrokeWidth(width: Int) {
        mThumbStrokeWidth.value = width
    }

    fun setSelectedRangeStrokeWidth(width: Int) {
        mSelectedRangeStrokeWidth.value = width
    }

    fun increaseBarStrokeWidth() {
        mBarStrokeWidth.value = mBarStrokeWidth.value?.inc()
    }

    fun decreaseBarStrokeWidth() {
        mBarStrokeWidth.value = mBarStrokeWidth.value?.dec()
    }

    fun increaseThumbStrokeWidth() {
        mThumbStrokeWidth.value = mThumbStrokeWidth.value?.inc()
    }

    fun decreaseThumbStrokeWidth() {
        mThumbStrokeWidth.value = mThumbStrokeWidth.value?.dec()
    }

    fun increaseSelectedRangeStrokeWidth() {
        mSelectedRangeStrokeWidth.value = mSelectedRangeStrokeWidth.value?.inc()
    }

    fun decreaseSelectedRangeStrokeWidth() {
        mSelectedRangeStrokeWidth.value = mSelectedRangeStrokeWidth.value?.dec()
    }

    private val mBarStrokeWidth = MutableLiveData<Int>()
    private val mThumbStrokeWidth = MutableLiveData<Int>()
    private val mSelectedRangeStrokeWidth = MutableLiveData<Int>()
}