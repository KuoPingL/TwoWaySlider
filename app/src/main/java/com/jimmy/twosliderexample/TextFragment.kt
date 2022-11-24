package com.jimmy.twosliderexample

import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Typeface
import android.graphics.fonts.FontStyle
import android.os.Bundle
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.FontRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

/**
 * @author jimmy
 * Created 2022/9/26 at 12:31 下午
 *
 * this fragment allows you to change your font, color and size ...
 *
 */
class TextFragment: Fragment(R.layout.fragment_text) {

    private lateinit var _textSizeEditText: AppCompatEditText
    private lateinit var _typefaceBtn: AppCompatButton

    private lateinit var _btnIncTextSize: AppCompatButton
    private lateinit var _btnDecTextSize: AppCompatButton

    private lateinit var _typeface: Typeface
    private val _typefaces by lazy {
        listOf(
            Typeface.DEFAULT,
            ResourcesCompat.getFont(requireContext(), R.font.bungee_spice)!!,
            ResourcesCompat.getFont(requireContext(), R.font.lobster_regular)!!,
            ResourcesCompat.getFont(requireContext(), R.font.nabla_regular)!!,
            ResourcesCompat.getFont(requireContext(), R.font.noto_sans_ethiopic)!!,
            ResourcesCompat.getFont(requireContext(), R.font.pacifico_regular)!!,
        )
    }
    private val _typefaceNames = listOf("System", "Bungee Spice", "Lobster", "Nabla", "Noto Sans Ethiopic", "Pacifico").toTypedArray()

    private var _titleView: TextView? = null

    private val _viewModel: TextViewModel by lazy {
        ViewModelProvider(requireActivity())[TextViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _textSizeEditText = view.findViewById<AppCompatEditText?>(R.id.edittext_text_size).apply {

//            addTextChangedListener {
//                if (_textSizeEditText.isFocused) {
//                    if (it != null && it.isNotEmpty()) {
//                        _viewModel.setTextSizeSp(it.toString().toInt())
//                    }
//                }
//            }

            setOnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    val editText = v as AppCompatEditText
                    val input = editText.text?.toString()

                    if (input.isNullOrEmpty()) {
                        editText.setText("12");
                    }

                    _viewModel.setTextSizeSp(editText.text!!.toString().toInt())
                }
            }

            addTextChangedListener {
                it?.toString()?.let { str ->
                    if (str.startsWith("0") && str.length > 1) {
                        _textSizeEditText.setText(str.removePrefix("0"))
                    }
                }
            }

            setOnEditorActionListener { v, actionId, event ->

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    v.hideKeyboard()
                    v.clearFocus()
                }

                false
            }
        }

        _btnIncTextSize = view.findViewById<AppCompatButton?>(R.id.btn_add_text_size).apply {
            setOnClickListener {
                _textSizeEditText.clearFocus()
                _viewModel.increaseTextSizeSp()
            }
        }

        _btnDecTextSize = view.findViewById<AppCompatButton?>(R.id.btn_minus_text_size).apply {
            setOnClickListener {
                _textSizeEditText.clearFocus()
                _viewModel.decreaseTextSizeSp()
            }
        }

        _typefaceBtn = view.findViewById<AppCompatButton?>(R.id.btn_font_picker).apply {
            setOnClickListener {
                AlertDialog.Builder(this@TextFragment.requireContext()).apply {

                    if (_titleView == null) {
                        _titleView = TextView(context).apply {
                            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
                            gravity = Gravity.CENTER_HORIZONTAL
                        }
                        _titleView?.apply {
                            text = "Sample"
                            textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f, Resources.getSystem().displayMetrics)
                        }
                    }

                    _titleView?.typeface = _typeface

                    setCustomTitle(_titleView!!)

                    var index = _typefaces.indexOf(_typeface)

                    if (index < 0) index = 0

                    setSingleChoiceItems(_typefaceNames, index) {d: DialogInterface, which: Int->
                        _titleView?.apply {
                            text = _typefaceNames[which]
                            typeface = _typefaces[which]
                        }
                    }

                    setPositiveButton("Ok") {d: DialogInterface, _: Int ->
                        _typeface = _titleView!!.typeface
                        _viewModel.setTypeface(_typeface)
                        d.dismiss()
                    }
                    setNegativeButton("Cancel") {d: DialogInterface, _: Int ->
                        d.dismiss()
                    }

                    setOnDismissListener {
                        _titleView?.let {
                            (it.parent as ViewGroup).removeView(it)
                        }
                    }
                }.show()
            }
        }

        _viewModel.textSizeSp.observe(viewLifecycleOwner) {
            _textSizeEditText.setText(it.toString())
        }

        _viewModel.textTypeface.observe(viewLifecycleOwner) {
            _typeface = it
            _typefaceBtn.text = _typefaceNames[_typefaces.indexOf(it)]
        }
    }
}