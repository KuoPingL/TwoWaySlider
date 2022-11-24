package com.jimmy.twosliderexample

import android.content.res.Resources
import android.graphics.Rect
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.arch.core.executor.TaskExecutor
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.*
import com.google.android.material.tabs.TabLayout
import com.jimmy.two_way_slider.TwoWaySlider

class MainActivity : AppCompatActivity() {
    private val _mainViewModel: MainViewModel by viewModels()
    private val _colorViewModel: ColorViewModel by viewModels()
    private val _dataViewModel: DataViewModel by viewModels()
    private val _strokeViewModel: StrokeViewModel by viewModels()
    private val _textViewModel: TextViewModel by viewModels()

    private lateinit var _viewpager: ViewPager
    private lateinit var _textView: TextView
    private lateinit var _nextBtn: AppCompatImageButton
    private lateinit var _previousBtn: AppCompatImageButton
    private lateinit var _twoWaySlider: TwoWaySlider

    private val _fragments: Array<Fragment> by lazy {
        Array(4) {index ->
            when(index) {
                1 -> DataFragment()
                2 -> StrokeFragment()
                3 -> TextFragment()
                else -> ColorFragment()
            }
        }
    }

    private val _titles: Array<String> by lazy {
        Array(4) {index ->
            when(index) {
                1 -> "Data"
                2 -> "Stroke"
                3 -> "Text"
                else -> "Color"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        _viewpager = findViewById<ViewPager?>(R.id.viewpager).apply {
            pageMargin = 200.fromDpToPx().toInt()
            adapter = ViewPagerAdapter()

            addOnPageChangeListener(object : SimpleOnPageChangeListener() {

                var selectedPage = 0

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {

                    // position : the index of the fragment we are scrolling
                    var title = _titles[position]

                    // positionOffset goes from 0f to 1f
                    if (selectedPage <= position) {
                        // scrolling forward
                        _textView.rotationX = 180f * positionOffset

                    } else {
                        // scrolling backward
                        title = _titles[selectedPage]
                        _textView.rotationX = 180f * (1f - positionOffset)

                    }
                    _textView.text = title

                    Log.d("ON_PAGE_SCROLLED", "position : $position, offset : $positionOffset, pixel : $positionOffsetPixels")
                }

                override fun onPageSelected(position: Int) {
                    if (selectedPage != position) {
                        _textView.text = _titles[position]
                        Log.d("ON_PAGE_SELECTED", "position : $position")
                        selectedPage = position
                    }

                    when(position) {
                        0 -> _previousBtn.isEnabled = false
                        _titles.size - 1 -> _nextBtn.isEnabled = false
                        else -> {
                            _previousBtn.isEnabled = true
                            _nextBtn.isEnabled = true
                        }
                    }
                }

            })
        }

        _textView = findViewById<TextView?>(R.id.textview_mode).apply {
            pivotX = 0.5f
            pivotY = 0.5f
        }

        _twoWaySlider = findViewById(R.id.slider_main)

        _previousBtn = findViewById<AppCompatImageButton?>(R.id.btn_back).apply {
            setOnClickListener {
                _viewpager.setCurrentItem(_viewpager.currentItem - 1, true)
            }
            isEnabled = false
        }

        _nextBtn = findViewById<AppCompatImageButton?>(R.id.btn_next).apply {
            setOnClickListener {
                _viewpager.setCurrentItem(_viewpager.currentItem + 1, true)
            }
        }

        setupMainViewModel()
        setupColorViewModel()
        setupDataViewModel()
        setupStrokeViewModel()
        setupTextViewModel()


    }

    private fun setupMainViewModel() {
        _mainViewModel.fromThumbStep.observe(this) {

        }
        _mainViewModel.toThumbStep.observe(this) {

        }
    }

    private fun setupColorViewModel() {

        _colorViewModel.setBarColor(_twoWaySlider.barColor)
        _colorViewModel.setThumbToColor(_twoWaySlider.toThumbColor)
        _colorViewModel.setThumbFromColor(_twoWaySlider.fromThumbColor)
        _colorViewModel.setSelectedRangeColor(_twoWaySlider.selectedRangeColor)

        _colorViewModel.thumbFromColor.observe(this){
            _twoWaySlider.fromThumbColor = it
        }

        _colorViewModel.thumbToColor.observe(this) {
            _twoWaySlider.toThumbColor = it
        }

        _colorViewModel.barColor.observe(this) {
            _twoWaySlider.barColor = it
        }

        _colorViewModel.selectedRangeColor.observe(this) {
            _twoWaySlider.selectedRangeColor = it
        }
    }

    private fun setupDataViewModel() {

        _dataViewModel.setStartValue(_twoWaySlider.startValue)
        _dataViewModel.setEndValue(_twoWaySlider.endValue)
        _dataViewModel.setNumberOfStep(_twoWaySlider.numberOfSteps)

        _dataViewModel.startValue.observe(this) {
            if (_twoWaySlider.endValue == it) {
                _twoWaySlider.numberOfSteps = TwoWaySlider.DEFAULT_NUMBER_OF_STEPS
            }
            _twoWaySlider.startValue = it
        }

        _dataViewModel.endValue.observe(this){
            if (_twoWaySlider.startValue == it) {
                _twoWaySlider.numberOfSteps = TwoWaySlider.DEFAULT_NUMBER_OF_STEPS
            }
            _twoWaySlider.endValue = it
        }

        _dataViewModel.numberOfStep.observe(this) {
            _twoWaySlider.numberOfSteps = it
        }
    }

    private fun setupStrokeViewModel() {
        _strokeViewModel.setBarStrokeWidth(_twoWaySlider.barStrokeWidth)
        _strokeViewModel.setThumbStrokeWidth(_twoWaySlider.thumbSize)
        _strokeViewModel.setSelectedRangeStrokeWidth(_twoWaySlider.selectedRangeStrokeWidth)

        _strokeViewModel.barStrokeWidth.observe(this) {
            _twoWaySlider.setBarStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, it.toFloat(), Resources.getSystem().displayMetrics))
        }

        _strokeViewModel.thumbStrokeWidth.observe(this) {
            _twoWaySlider.setThumbSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, it.toFloat(), Resources.getSystem().displayMetrics))
        }

        _strokeViewModel.selectedRangeStokeWidth.observe(this) {
            _twoWaySlider.setSelectedRangeStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, it.toFloat(), Resources.getSystem().displayMetrics))
        }
    }

    private fun setupTextViewModel() {
        _textViewModel.setTextSizeSp(_twoWaySlider.textSize)
        _textViewModel.setTypeface(_twoWaySlider.textTypeface)

        _textViewModel.textSizeSp.observe(this) {
            if (it <= 0) {
                _twoWaySlider.textSize = 1;
            }
            else _twoWaySlider.textSize = it
        }

        _textViewModel.textTypeface.observe(this) {
            _twoWaySlider.textTypeface = it
        }
    }

    inner class ViewPagerAdapter: FragmentPagerAdapter(supportFragmentManager) {
        override fun getCount(): Int {
            return _fragments.size
        }

        override fun getItem(position: Int): Fragment {
            return _fragments[position]
        }

    }
}