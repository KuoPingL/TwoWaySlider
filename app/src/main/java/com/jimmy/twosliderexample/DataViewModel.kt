package com.jimmy.twosliderexample

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * @author jimmy
 * Created 2022/9/26 at 11:45 下午
 */
class DataViewModel : ViewModel() {

    val errorMsg : LiveData<String?>
    get() = mErrorMsg

    val numberOfStep: LiveData<Int>
    get() = mNumberOfStep

    val startValue: LiveData<Int>
    get() = mStartValue

    val endValue: LiveData<Int>
    get() = mEndValue

    fun setNumberOfStep(step: Int) {
        mNumberOfStep.value = step
    }

    fun increaseNumberOfStep() {
        mNumberOfStep.value = mNumberOfStep.value?.inc()
    }

    fun decreaseNumberOfStep() {
        mNumberOfStep.value = mNumberOfStep.value?.dec()
    }

    fun setStartValue(value: Int) {
        mStartValue.value = value
    }

    fun setEndValue(value: Int) {
        mEndValue.value = value
    }

    fun increaseStartValue() {
        var inc = (mStartValue.value ?: 0) + 1
        if (inc == (mEndValue.value?:0)) {
            inc ++
            mErrorMsg.value = "Start Value cannot be the same as End Value"
        }
        mStartValue.value = inc
    }

    fun decreaseStartValue() {
        var dec = (mStartValue.value ?: 0) - 1
        if (dec == (mEndValue.value?:0)) {
            dec --
            mErrorMsg.value = "Start Value cannot be the same as End Value"
        }

        mStartValue.value = dec
    }

    fun increaseEndValue() {
        var inc = (mEndValue.value ?: 0) + 1
        if (inc == (mStartValue.value?:0)) {
            inc ++
            mErrorMsg.value = "End Value cannot be the same as Start Value"
        }
        mEndValue.value = inc
    }

    fun decreaseEndValue() {
        var dec = (mEndValue.value ?: 0) - 1
        if (dec == (mStartValue.value?:0)) {
            dec --
            mErrorMsg.value = "End Value cannot be the same as Start Value"
        }
        mEndValue.value = dec
    }

    fun showErrorMsg(msg: String) {
        mErrorMsg.value = msg
    }

    fun doneShowingError() {
        mErrorMsg.value = null
    }

    private val mNumberOfStep = MutableLiveData<Int>(0)
    private val mStartValue = MutableLiveData<Int>(0)
    private val mEndValue = MutableLiveData<Int>(0)
    private val mErrorMsg = MutableLiveData<String?>()

}