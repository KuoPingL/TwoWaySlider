package com.jimmy.twosliderexample

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.slider.Slider
import com.google.android.material.slider.Slider.OnChangeListener

/**
 * @author jimmy
 * Created 2022/9/26 at 12:21 下午
 *
 * This fragment will contain UI for setting the colors in
 * - Thumb
 * - Selected Range
 * - Bar
 *
 */
class ColorFragment: Fragment(R.layout.fragment_color), OnChangeListener, OnClickListener {

    private val _colorViewModel: ColorViewModel by lazy {
        ViewModelProvider(requireActivity())[ColorViewModel::class.java]
    }

    private lateinit var _title: TextView

    private lateinit var _barView: View
    private lateinit var _thumbToView: View
    private lateinit var _thumbFromView: View
    private lateinit var _selectedRangeView: View

    private lateinit var _rSlider: Slider
    private lateinit var _gSlider: Slider
    private lateinit var _bSlider: Slider

    private var _startUpdateColor = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _title = view.findViewById(R.id.title)

        _rSlider = view.findViewById<Slider?>(R.id.slider_red).also {
            it.addOnChangeListener(this)
        }
        _gSlider = view.findViewById<Slider?>(R.id.slider_green).also {
            it.addOnChangeListener(this)
        }
        _bSlider = view.findViewById<Slider?>(R.id.slider_blue).also {
            it.addOnChangeListener(this)
        }

        _barView = view.findViewById<View?>(R.id.view_bar)
        _thumbToView = view.findViewById(R.id.view_thumb_to)
        _thumbFromView = view.findViewById(R.id.view_thumb_from)
        _selectedRangeView = view.findViewById(R.id.view_selected_range)

        _barView.also { it.setOnClickListener(this) }
        _thumbToView.also { it.setOnClickListener(this) }
        _thumbFromView.also { it.setOnClickListener(this) }
        _selectedRangeView.also { it.setOnClickListener(this) }


        _colorViewModel.rgb.observe(viewLifecycleOwner) {
            _rSlider.value = it.r.toFloat()
            _gSlider.value = it.g.toFloat()
            _bSlider.value = it.b.toFloat()
        }

        _colorViewModel.barColor.observe(viewLifecycleOwner) {
            _barView.setBackgroundColor(it)
        }

        _colorViewModel.selectedRangeColor.observe(viewLifecycleOwner) {
            _selectedRangeView.setBackgroundColor(it)
        }

        _colorViewModel.thumbToColor.observe(viewLifecycleOwner) {
            _thumbToView.setBackgroundColor(it)
        }

        _colorViewModel.thumbFromColor.observe(viewLifecycleOwner) {
            _thumbFromView.setBackgroundColor(it)
        }

        _colorViewModel.colorMode.observe(viewLifecycleOwner) {
            it?.let {
                _startUpdateColor = true
            }
        }

        _colorViewModel.selectComponent(_colorViewModel.colorMode.value ?: ColorViewModel.ColorType.BAR_COLOR)


    }

    override fun onValueChange(slider: Slider, value: Float, fromUser: Boolean) {

        val r = _rSlider.value.toInt()
        val g = _gSlider.value.toInt()
        val b = _bSlider.value.toInt()

        var mode = ""

        _colorViewModel.colorMode.value?.let {
            when(it) {
                ColorViewModel.ColorType.BAR_COLOR -> {
                    mode = "BAR"
                }

                ColorViewModel.ColorType.SELECTED_RANGE_COLOR -> {
                    mode = "SELECTED RANGE"
                }

                ColorViewModel.ColorType.THUMB_TO_COLOR -> {
                    mode = "THUMB TO"
                }

                ColorViewModel.ColorType.THUMB_FROM_COLOR -> {
                    mode = "THUMB FROM"
                }
            }
        }

        _title.text = "$mode : ($r, $g, $b)"

        if (!_startUpdateColor) return

        _colorViewModel.setRGB(r, g ,b)
    }

    override fun onClick(v: View?) {
        _startUpdateColor = false
        v?.let {
            when(it) {
                _barView -> {
                    _colorViewModel.selectComponent(ColorViewModel.ColorType.BAR_COLOR)
                }

                _thumbFromView -> {
                    _colorViewModel.selectComponent(ColorViewModel.ColorType.THUMB_FROM_COLOR)
                }

                _thumbToView -> {
                    _colorViewModel.selectComponent(ColorViewModel.ColorType.THUMB_TO_COLOR)
                }

                _selectedRangeView -> {
                    _colorViewModel.selectComponent(ColorViewModel.ColorType.SELECTED_RANGE_COLOR)
                }

                else -> {}
            }
        }
    }
}