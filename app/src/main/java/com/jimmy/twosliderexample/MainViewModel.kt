package com.jimmy.twosliderexample

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import java.lang.RuntimeException

/**
 * @author jimmy
 * Created 2022/9/26 at 12:36 下午
 */
class MainViewModel: ViewModel() {


    val fromThumbStep: LiveData<Int>
    get() = mFromThumbStep

    private val mFromThumbStep = MutableLiveData<Int>()

    val toThumbStep: LiveData<Int>
    get() = mToThumbStep

    private val mToThumbStep = MutableLiveData<Int>()



}


