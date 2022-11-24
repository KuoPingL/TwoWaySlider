package com.jimmy.twosliderexample

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.getSystemService
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.jimmy.two_way_slider.TwoWaySlider

/**
 * @author jimmy
 * Created 2022/9/26 at 12:20 下午
 *
 * This fragment contains UI for changing the Stroke width or size of the followings :
 * - Bar
 * - Thumb
 * - Tick
 *
 */
class StrokeFragment: Fragment(R.layout.fragment_stroke), OnClickListener {
    private val _viewModel : StrokeViewModel by lazy {
        ViewModelProvider(requireActivity()).get(StrokeViewModel::class.java)
    }

    private lateinit var _barEditText: AppCompatEditText
    private lateinit var _thumbEditText: AppCompatEditText
    private lateinit var _selectedRangeEditText: AppCompatEditText

    private lateinit var _barIncrementBtn: AppCompatButton
    private lateinit var _barDecrementBtn: AppCompatButton
    private lateinit var _thumbIncrementBtn: AppCompatButton
    private lateinit var _thumbDecrementBtn: AppCompatButton
    private lateinit var _selectedRangeIncrementBtn: AppCompatButton
    private lateinit var _selectedRangeDecrementBtn: AppCompatButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _barEditText = view.findViewById<AppCompatEditText?>(R.id.edittext_bar_stroke_width).apply {
            setOnFocusChangeListener { v, hasFocus ->
                // when clicked on +/- buttons
                if (!hasFocus) {
                    val editText = v as AppCompatEditText
                    val input = editText.text?.toString()
                    if (input == null || input.isEmpty() || input.toInt() < TwoWaySlider.DEFAULT_BAR_STROKE_WIDTH) {
                        editText.setText(TwoWaySlider.DEFAULT_BAR_STROKE_WIDTH.toString())
                    }
                    _viewModel.setBarStrokeWidth(editText.text.toString().toInt())
                }
            }

            addTextChangedListener {
                it?.toString()?.let { str ->
                    if (str.startsWith("0") && str.length > 1) {
                        _barEditText.setText(str.removePrefix("0"))
                    }
                }
            }
        }

        _thumbEditText = view.findViewById<AppCompatEditText?>(R.id.edittext_thumb_stroke_width).apply {
            setOnFocusChangeListener { v, hasFocus ->
                // when clicked on +/- buttons
                if (!hasFocus) {
                    val editText = v as AppCompatEditText
                    val input = editText.text?.toString()
                    if (input == null || input.isEmpty() || input.toInt() < TwoWaySlider.DEFAULT_THUMB_SIZE) {
                        editText.setText(TwoWaySlider.DEFAULT_THUMB_SIZE.toString())
                    }
                    _viewModel.setThumbStrokeWidth(editText.text.toString().toInt())
                }
            }

            addTextChangedListener {
                it?.toString()?.let { str ->
                    if (str.startsWith("0") && str.length > 1) {
                        _thumbEditText.setText(str.removePrefix("0"))
                    }
                }
            }
        }

        _selectedRangeEditText = view.findViewById<AppCompatEditText?>(R.id.edittext_selected_range).apply {
            setOnFocusChangeListener { v, hasFocus ->
                // when clicked on +/- buttons
                if (!hasFocus) {
                    val editText = v as AppCompatEditText
                    val input = editText.text?.toString()
                    if (input == null || input.isEmpty() || input.toInt() < 0) {
                        editText.setText("0")
                    }

                    _viewModel.setSelectedRangeStrokeWidth(editText.text.toString().toInt())
                }

                addTextChangedListener {
                    it?.toString()?.let { str ->
                        if (str.startsWith("0") && str.length > 1) {
                            _selectedRangeEditText.setText(str.removePrefix("0"))
                        }
                    }
                }

                setOnEditorActionListener { v, actionId, event ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        val imm = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(v.windowToken, 0)
                        _selectedRangeEditText.clearFocus()
                        _selectedRangeEditText.rootView.clearFocus()
                    }
                    false
                }
            }
        }

        _viewModel.barStrokeWidth.observe(viewLifecycleOwner) {
            _barEditText.setText(it.toString())
        }

        _viewModel.thumbStrokeWidth.observe(viewLifecycleOwner) {
            _thumbEditText.setText(it.toString())
        }

        _viewModel.selectedRangeStokeWidth.observe(viewLifecycleOwner) {
            _selectedRangeEditText.setText(it.toString())
        }

        _barIncrementBtn = view.findViewById<AppCompatButton?>(R.id.btn_add_bar_stroke_width).apply {
            setOnClickListener(this@StrokeFragment)
        }
        _barDecrementBtn = view.findViewById<AppCompatButton>(R.id.btn_minus_bar_stroke_width).apply {
            setOnClickListener(this@StrokeFragment)
        }

        _thumbIncrementBtn = view.findViewById<AppCompatButton>(R.id.btn_add_thumb_stroke_width).apply {
            setOnClickListener(this@StrokeFragment)
        }
        _thumbDecrementBtn = view.findViewById<AppCompatButton>(R.id.btn_minus_thumb_stroke_width).apply {
            setOnClickListener(this@StrokeFragment)
        }

        _selectedRangeIncrementBtn = view.findViewById<AppCompatButton>(R.id.btn_add_selected_range).apply {
            setOnClickListener(this@StrokeFragment)
        }
        _selectedRangeDecrementBtn = view.findViewById<AppCompatButton>(R.id.btn_minus_selected_range).apply {
            setOnClickListener(this@StrokeFragment)
        }
    }


    override fun onClick(v: View?) {
        v?.let {
            when(it) {
                _barIncrementBtn -> {
                    _viewModel.increaseBarStrokeWidth()
                }
                _barDecrementBtn -> {
                    _viewModel.decreaseBarStrokeWidth()
                }
                _thumbIncrementBtn -> {
                    _viewModel.increaseThumbStrokeWidth()
                }
                _thumbDecrementBtn -> {
                    _viewModel.decreaseThumbStrokeWidth()
                }
                _selectedRangeIncrementBtn -> {
                    _viewModel.increaseSelectedRangeStrokeWidth()
                }
                _selectedRangeDecrementBtn -> {
                    _viewModel.decreaseSelectedRangeStrokeWidth()
                }
            }
        }
    }
}