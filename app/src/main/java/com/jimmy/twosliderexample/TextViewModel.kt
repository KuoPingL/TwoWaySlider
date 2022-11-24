package com.jimmy.twosliderexample

import android.graphics.Typeface
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * @author jimmy
 * Created 2022/9/29 at 5:25 下午
 */
class TextViewModel : ViewModel() {
    val textTypeface: LiveData<Typeface>
    get() = mTextTypeface

    val textSizeSp: LiveData<Int>
    get() = mTextSizeSp

    val shouldShowFontPicker: LiveData<Boolean>
    get() = mShouldShowFontPicker

    fun setTypeface(typeface: Typeface){
        mTextTypeface.value = typeface
    }

    fun showFontPicker() {
        mShouldShowFontPicker.value = true
    }

    fun doneShowingFontPicker() {
        mShouldShowFontPicker.value = false
    }

    fun setTextSizeSp(size: Int) {
        mTextSizeSp.value = size
    }

    fun increaseTextSizeSp() {
        mTextSizeSp.value = mTextSizeSp.value?.inc()
    }

    fun decreaseTextSizeSp() {

        mTextSizeSp.value?.let {
            if(it <= 1) return
        }

        mTextSizeSp.value = mTextSizeSp.value?.dec()
    }

    private val mTextTypeface = MutableLiveData<Typeface>()
    private val mTextSizeSp = MutableLiveData<Int>()
    private val mShouldShowFontPicker = MutableLiveData<Boolean>(false)
}