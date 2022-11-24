package com.jimmy.twosliderexample

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

/**
 * @author jimmy
 * Created 2022/9/26 at 12:19 下午
 *
 * This fragment will contain UI for alternative the data of the slider
 * including
 * - start value
 * - end value
 * - initial from value
 * - initial to value
 *
 */
class DataFragment: Fragment(R.layout.fragment_data), OnClickListener {
    private val _viewModel: DataViewModel by lazy {
        ViewModelProvider(requireActivity()).get(DataViewModel::class.java)
    }

    private lateinit var _stepSizeEditText: AppCompatEditText
    private lateinit var _startValueEditText: AppCompatEditText
    private lateinit var _endValueEditText: AppCompatEditText

    private lateinit var _stepSizeIncrementBtn: AppCompatButton
    private lateinit var _stepSizeDecrementBtn: AppCompatButton
    private lateinit var _startValueIncrementBtn: AppCompatButton
    private lateinit var _startValueDecrementBtn: AppCompatButton
    private lateinit var _endValueIncrementBtn: AppCompatButton
    private lateinit var _endValueDecrementBtn: AppCompatButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.apply {
            _stepSizeEditText = findViewById<AppCompatEditText?>(R.id.edittext_step_size).apply {
                setOnFocusChangeListener { v, hasFocus ->
                    if (!hasFocus) {
                        val editText = v as AppCompatEditText
                        val input = editText.text?.toString()
                        if (input == null || input.isEmpty() || input.toInt() <= 0) {
                            editText.setText("1")
                        }
                        _viewModel.setNumberOfStep(editText.text!!.toString().toInt())
                    }
                }

                addTextChangedListener {
                    it?.toString()?.let { str ->
                        if (str.startsWith("0") && str.length > 1) {
                            _startValueEditText.setText(str.removePrefix("0"))
                        }
                    }
                }
            }
            _startValueEditText = findViewById<AppCompatEditText?>(R.id.edittext_start_value).apply {
                setOnFocusChangeListener { v, hasFocus ->
                    if (!hasFocus) {
                        val editText = v as AppCompatEditText
                        val input = editText.text?.toString()
                        val endValue = _endValueEditText.text!!.toString().toInt()
                        if (input == null || input.isEmpty() || input.toInt() == endValue) {
                            editText.setText("${endValue - 1}")
                            _viewModel.showErrorMsg("Number of Step cannot be less than 1")
                        }
                        _viewModel.setStartValue(editText.text!!.toString().toInt())
                    }
                }

                addTextChangedListener {
                    it?.toString()?.let { str ->
                        if (str.startsWith("0") && str.length > 1) {
                            _startValueEditText.setText(str.removePrefix("0"))
                        }
                    }
                }
            }
            _endValueEditText = findViewById<AppCompatEditText?>(R.id.edittext_end_value).apply {
                setOnEditorActionListener { v, actionId, event ->
                    if(actionId == EditorInfo.IME_ACTION_DONE) {
                        val imm = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(v.windowToken, 0)
                        _endValueEditText.clearFocus()
                    }
                    false
                }

                setOnFocusChangeListener { v, hasFocus ->
                    if (!hasFocus) {
                        val editText = v as AppCompatEditText
                        val input = editText.text?.toString()
                        val startValue = _startValueEditText.text!!.toString().toInt()
                        if (input == null || input.isEmpty() || input.toInt() == startValue) {
                            editText.setText("${startValue + 1}")
                        }
                        _viewModel.setEndValue(editText.text!!.toString().toInt())
                    }
                }

                addTextChangedListener {
                    it?.toString()?.let { str ->
                        if (str.startsWith("0") && str.length > 1) {
                            _endValueEditText.setText(str.removePrefix("0"))
                        }
                    }
                }
            }
            _stepSizeIncrementBtn = findViewById<AppCompatButton?>(R.id.btn_add_step_size).apply {
                setOnClickListener(this@DataFragment)
            }
            _stepSizeDecrementBtn = findViewById<AppCompatButton?>(R.id.btn_minus_step_size).apply {
                setOnClickListener(this@DataFragment)
            }
            _startValueIncrementBtn = findViewById<AppCompatButton?>(R.id.btn_add_start_value).apply {
                setOnClickListener(this@DataFragment)
            }
            _startValueDecrementBtn = findViewById<AppCompatButton?>(R.id.btn_minus_start_value).apply {
                setOnClickListener(this@DataFragment)
            }
            _endValueIncrementBtn = findViewById<AppCompatButton?>(R.id.btn_add_end_value).apply {
                setOnClickListener(this@DataFragment)
            }
            _endValueDecrementBtn = findViewById<AppCompatButton?>(R.id.btn_minus_end_value).apply {
                setOnClickListener(this@DataFragment)
            }
        }

        _viewModel.numberOfStep.observe(viewLifecycleOwner) {
            _stepSizeEditText.setText("$it")
        }

        _viewModel.startValue.observe(viewLifecycleOwner) {
            _startValueEditText.setText("$it")
        }

        _viewModel.endValue.observe(viewLifecycleOwner) {
            _endValueEditText.setText("$it")
        }

        _viewModel.errorMsg.observe(viewLifecycleOwner) {
            it?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                _viewModel.doneShowingError()
            }
        }
    }

    override fun onClick(v: View?) {
        v?.let {
            when(it) {
                _stepSizeIncrementBtn -> {
                    _viewModel.increaseNumberOfStep()
                }
                _stepSizeDecrementBtn -> {
                    _viewModel.decreaseNumberOfStep()
                }
                _startValueIncrementBtn -> {
                    _viewModel.increaseStartValue()
                }
                _startValueDecrementBtn -> {
                    _viewModel.decreaseStartValue()
                }
                _endValueIncrementBtn -> {
                    _viewModel.increaseEndValue()
                }
                _endValueDecrementBtn -> {
                    _viewModel.decreaseEndValue()
                }
            }
        }
    }
}