package org.buffer.android.counterview

import android.content.Context
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatTextView
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.EditText

class CounterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatTextView(context, attrs, defStyle) {

    private val textWatcher: TextWatcher
    var textView: EditText? = null
    lateinit var counterMode: CounterMode
    var charactersRemainingUntilCounterDisplay: Int? = null
        set(value) {
            field = value
            updateCounterValue(textViewContentLength())
        }

    var counterMaxLength: Int = 0
        set(value) {
            field = value
            updateCounterValue(textViewContentLength())
        }

    private var counterTextColor = -1
    private var counterErrorTextColor = -1

    fun attachToEditText(editText: EditText) {
        if (this.textView != null) {
            throw IllegalStateException("There is already an EditText attached to this view!")
        }
        this.textView = editText
        this.textView?.addTextChangedListener(textWatcher)
    }

    fun detachFromEditText() {
        this.textView?.removeTextChangedListener(textWatcher)
        this.textView = null
    }

    fun updateEnabledState(contentLength: Int) {
        val charactersUntilDisplay = charactersRemainingUntilCounterDisplay ?: 0
        setCounterEnabled(contentLength >= counterMaxLength - charactersUntilDisplay)
    }

    fun updateCounterValue(contentLength: Int) {
        visibility = charactersRemainingUntilCounterDisplay?.let {
            if ((counterMaxLength - contentLength) <= it) View.VISIBLE else View.GONE
        } ?: run {
            View.VISIBLE
        }
        text = when (counterMode) {
            CounterMode.DESCENDING -> {
                val remainingCounterMaxLength = counterMaxLength - contentLength
                remainingCounterMaxLength.toString()
            }
            CounterMode.ASCENDING -> {
                contentLength.toString()
            }
            CounterMode.STANDARD -> {
                 context.getString(R.string.character_count_divider,
                         contentLength, counterMaxLength)
            }
        }
        contentDescription = context.getString(R.string.character_count_description,
                contentLength, counterMaxLength)
        updateTextColor(contentLength)
    }

    fun setCounterTextColor(@ColorInt color: Int) {
        counterTextColor = color
        updateTextColor(textViewContentLength())
    }

    fun setCounterErrorTextColor(@ColorInt color: Int) {
        counterErrorTextColor = color
        updateTextColor(textViewContentLength())
    }

    private fun updateTextColor(contentLength: Int) {
        if (contentLength > counterMaxLength) {
            setTextColor(counterErrorTextColor)
        } else {
            setTextColor(counterTextColor)
        }
    }

    private fun textViewContentLength() = textView?.length() ?: 0

    private fun handleAttributes(context: Context, attrs: AttributeSet?) {
        attrs?.let {
            val styleAttributes = context.obtainStyledAttributes(it, R.styleable.CounterView, 0, 0)
            try {
                counterTextColor = styleAttributes
                        .getColor(R.styleable.CounterView_counterCountColor,
                                ContextCompat.getColor(getContext(), R.color.red))
                counterErrorTextColor = styleAttributes
                        .getColor(R.styleable.CounterView_counterErrorTextColor,
                                ContextCompat.getColor(getContext(), R.color.red))
                val counterMode = styleAttributes
                        .getString(R.styleable.CounterView_counterMode)
                if (counterMode != null) {
                    this.counterMode = CounterMode.fromId(counterMode)
                } else {
                    this.counterMode = CounterMode.STANDARD
                }
            } finally {
                styleAttributes.recycle()
            }
        } ?: run {
            counterErrorTextColor = ContextCompat.getColor(getContext(), R.color.red)
            counterTextColor = ContextCompat.getColor(getContext(), R.color.off_black)
            counterMode = CounterMode.STANDARD
        }
    }

    private fun setCounterEnabled(enabled: Boolean) {
        visibility = if (enabled) View.VISIBLE else View.GONE
    }

    init {
        handleAttributes(context, attrs)
        this.textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                updateCounterValue(s.length)
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        }
    }
}