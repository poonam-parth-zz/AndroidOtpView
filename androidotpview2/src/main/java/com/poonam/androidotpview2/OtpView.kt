package com.poonam.androidotpview2

import android.content.Context
import android.graphics.*
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.ViewCompat
import com.poonam.androidotpview.helpers.fetchColor


class OtpView : AppCompatEditText {

    private var mMaxLength = 4
    private var mClickListener: View.OnClickListener? = null
    private var mLineStroke = 3f
    private var mLineStrokeSelected = 4f
    private var mLinesPaint: Paint? = null
    private var isErrorShown: Boolean = false
    private var blink: Blink? = null
    private var mCharSize: Float = 0f
    private var drawCursor: Boolean = false
    private var mLineSpacing = 12f
    private var lineEndColor = Color.RED
    private var lineStartColor = Color.WHITE
    private var dashColor = Color.GRAY
    private var lineCountOtp = 4
    private var spaceLength = 0f
    private var dashLength = 0f
    private var showDash = true

    companion object {

        const val XML_NAMESPACE_ANDROID = "http://schemas.android.com/apk/res/android"
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)


    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

        init(attrs, defStyleAttr)
    }

    private fun convertToPx(v: Float): Float {

        return context.resources.displayMetrics.density * v
    }

    private fun init(attrs: AttributeSet, defStyleAttr: Int) {

        initPaintVar()
        setAttributes(attrs, defStyleAttr)
        initVariousParam(attrs)
        disableCopyPaste()

    }

    private fun setAttributes(attrs: AttributeSet, defStyleAttr: Int) {
            val theme = context.theme
            val typedArray = theme?.obtainStyledAttributes(attrs, R.styleable.OtpView, defStyleAttr, 0)
            lineStartColor = typedArray!!.getInt(R.styleable.OtpView_lineStartColor, Color.BLACK)
            lineEndColor = typedArray.getInt(R.styleable.OtpView_lineEndColor, Color.RED)
            dashColor = typedArray.getInt(R.styleable.OtpView_dashColor, Color.GRAY)
            lineCountOtp = typedArray.getInt(R.styleable.OtpView_lineCount, 4)
            dashLength = typedArray.getFloat(R.styleable.OtpView_dashLength, 0f)
            spaceLength = typedArray.getFloat(R.styleable.OtpView_spaceLength, 0f)
            showDash = typedArray.getBoolean(R.styleable.OtpView_showDash, true)
            typedArray.recycle()

    }


    override fun setOnClickListener(l: View.OnClickListener?) {

        mClickListener = l
    }

    override fun setOnTouchListener(l: OnTouchListener?) {
        super.setOnTouchListener(l)
        clearError()
    }

    override fun setCustomSelectionActionModeCallback(actionModeCallback: ActionMode.Callback) {

        throw RuntimeException("setCustomSelectionActionModeCallback() not supported.")
    }

    override fun onDraw(canvas: Canvas) {

        drawLines(canvas)
    }

    private fun updateColorForLines(i: Int, next: Int, x1: Float, y1: Float, x2: Float, y2: Float) {

        if (isErrorShown) {

            mLinesPaint!!.strokeWidth = mLineStrokeSelected
            mLinesPaint!!.color = context.fetchColor(R.color.colorAccent)
            mLinesPaint!!.shader = null

        } else

        // if pos is selected or less then that
            if (next < 0 || i == 0) {
                mLinesPaint!!.strokeWidth = mLineStrokeSelected
                mLinesPaint!!.color = context.fetchColor(R.color.white)
                mLinesPaint!!.shader =
                    LinearGradient(x1, y1, x2, y2, lineStartColor, lineEndColor, Shader.TileMode.MIRROR)

            } else {
                mLinesPaint!!.strokeWidth = mLineStroke
                mLinesPaint!!.shader = null
                mLinesPaint!!.color = context.fetchColor(R.color.grey)
            }
    }

    fun setError() {

        isErrorShown = true
        invalidate()
    }

    fun clearError() {

        isErrorShown = false
        invalidate()
    }

    private fun shouldBlink(): Boolean {

        return isFocused
    }

    private fun makeBlink() {

        if (shouldBlink()) {
            if (blink == null) {
                blink = Blink()
            }
            removeCallbacks(blink)
            drawCursor = false
            postDelayed(blink, 500)
        } else {
            if (blink != null) {
                removeCallbacks(blink)
            }
        }
    }


    private fun invalidateCursor(showCursor: Boolean) {

        if (drawCursor != showCursor) {
            drawCursor = showCursor
            invalidate()
        }
    }

    private fun drawCursor(canvas: Canvas, x1: Float, y1: Float) {

        if (drawCursor) {
            val fm = paint.fontMetrics
            val height = fm.descent - fm.ascent + fm.leading
            paint.color = context.fetchColor(R.color.black)
            paint.strokeWidth = 4f
            //canvas.drawLine(cx, cy, cx, cy - mCharSize / 2, paint)
            canvas.drawLine(x1, y1, x1, y1 - height, paint)
        }
    }

    override fun onTextChanged(text: CharSequence, start: Int, lengthBefore: Int, lengthAfter: Int) {

        if (start != text.length) {
            moveSelectionToEnd()
        }
        makeBlink()

    }


    override fun onSelectionChanged(selStart: Int, selEnd: Int) {

        super.onSelectionChanged(selStart, selEnd)
        if (text != null && selEnd != text!!.length) {
            moveSelectionToEnd()
        }
    }

    private fun moveSelectionToEnd() {

        if (text != null) {
            setSelection(text!!.length)
        }
    }


    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {

        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        if (focused) {
            moveSelectionToEnd()
            makeBlink()
        }
    }


    private fun drawDash(canvas: Canvas, x: Float, y: Float, width: Float) {
        if (showDash) {
            mLinesPaint!!.strokeWidth = 5f
            mLinesPaint!!.color = dashColor
            mLinesPaint!!.shader = null
            val fm = paint.fontMetrics
            val height = fm.descent - fm.ascent + fm.leading
            if (dashLength == 0f)
                canvas.drawLine(x, y - height / 2, x + width / 2, y - height / 2, mLinesPaint)
            else
                canvas.drawLine(x, y - height / 2, x + dashLength, y - height / 2, mLinesPaint)
        }
    }


    private fun disableCopyPaste() {

        //Disable copy paste
        super.setCustomSelectionActionModeCallback(object : ActionMode.Callback {
            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode) {}

            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                return false
            }
        })

        super.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if( s.toString().length == lineCountOtp  ){
                    (context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager)
                        .hideSoftInputFromWindow(windowToken, 0)
                }
            }
        })

        // When tapped, move cursor to end
        super.setOnClickListener { v ->
            setSelection(text!!.length)
            if (mClickListener != null) {
                mClickListener!!.onClick(v)
            }
        }

        super.setCursorVisible(false)
        setTextIsSelectable(false)

    }


    private fun initPaintVar() {

        mLineStroke = convertToPx(mLineStroke)
        mLineStrokeSelected = convertToPx(mLineStrokeSelected)
        mLinesPaint = Paint(paint)
        mLinesPaint!!.strokeWidth = mLineStroke
        setHintTextColor(context.fetchColor(R.color.black))
    }

    private fun initVariousParam(attrs: AttributeSet) {

        setBackgroundResource(0)
        mLineSpacing = convertToPx(mLineSpacing)
        mMaxLength = attrs.getAttributeIntValue(XML_NAMESPACE_ANDROID, "maxLength", 4)
    }

    private fun drawLines(canvas: Canvas) {

        val availableWidth = width

        if (spaceLength <= 0) {
            mCharSize = availableWidth / (lineCountOtp.toFloat() * 2 - 1)
        } else {
            mCharSize = (availableWidth.minus(spaceLength * lineCountOtp.minus(1))) / lineCountOtp.toFloat()
        }

        var startX = paddingLeft
        val bottom = height

        //Text Width
        val text = text
        val textLength = text!!.length
        val textWidths = FloatArray(textLength)
        paint.getTextWidths(getText(), 0, textLength, textWidths)
        val fm = paint.fontMetrics
        val heightTxt = fm.descent - fm.ascent + fm.leading

        var i = 0
        while (i < lineCountOtp) {
            canvas.save()
            if (i == textLength) {
                drawCursor(canvas, startX.toFloat() + mCharSize / 2, bottom.toFloat() - mLineSpacing)
            }

            updateColorForLines(
                i,
                i.minus(textLength),
                startX.toFloat(),
                bottom.toFloat(),
                startX.toFloat() + mCharSize,
                bottom.toFloat()
            )
            canvas.drawLine(startX.toFloat(), bottom.toFloat(), startX + mCharSize, bottom.toFloat(), mLinesPaint!!)

            if (getText()!!.length > i) {
                val middle = startX + mCharSize / 2
                canvas.drawText(text, i, i + 1, middle - textWidths[0] / 2, bottom - mLineSpacing - heightTxt / 4, paint)
            }

            if (i + 1 <= lineCountOtp - 1) {
                if (spaceLength > 0)
                    drawDash(canvas, startX + mCharSize + spaceLength / 2, bottom - mLineSpacing, mCharSize * 3 / 10)
                else
                    drawDash(canvas, startX + mCharSize + mCharSize / 2, bottom - mLineSpacing, mCharSize * 3 / 10)
                startX += if (spaceLength <= 0f) {
                    (mCharSize * 2).toInt()
                } else {
                    (mCharSize + spaceLength).toInt()
                }
            }
            i++
            canvas.restore()
        }
    }


    private inner class Blink : Runnable {

        private var cancelled: Boolean = false

        override fun run() {
            if (cancelled) {
                return
            }
            removeCallbacks(this)
            if (shouldBlink()) {
                invalidateCursor(!drawCursor)
                postDelayed(this, 500)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)
        val width: Int
        val height: Int
        val fm = paint.fontMetrics

        val boxHeight =  fm.descent - fm.ascent + fm.leading + mLineSpacing
        if (widthMode == View.MeasureSpec.EXACTLY) {
            width = widthSize
        } else {
            var boxesWidth = 0
            if(spaceLength > 0)
                boxesWidth = ((lineCountOtp - 1) * spaceLength + lineCountOtp * 80).toInt()
            else
                boxesWidth = ((2*lineCountOtp - 1) * 80).toInt()
            width = boxesWidth + ViewCompat.getPaddingEnd(this) + ViewCompat.getPaddingStart(this)
        }
        height = if (heightMode == View.MeasureSpec.EXACTLY)
            heightSize
        else
            (boxHeight + paddingTop + paddingBottom).toInt()
        setMeasuredDimension(width, height)
    }


}
