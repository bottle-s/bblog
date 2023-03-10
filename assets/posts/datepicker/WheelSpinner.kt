package kr.danal.app.damoum.presentation.ui.common

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.OverScroller
import androidx.core.content.ContextCompat
import androidx.core.graphics.toRectF
import kr.danal.app.damoum.R
import kr.danal.domain.util.log
import java.util.*

interface OnValueChangeListener {
    fun onValueChange(picker: WheelPicker, oldVal: String, newVal: String)
}

interface OnScrollListener {
    companion object {
        const val SCROLL_STATE_IDLE = 0
        const val SCROLL_STATE_TOUCH_SCROLL = 1
        const val SCROLL_STATE_FLING = 2
    }
    fun onScrollStateChange(view: WheelPicker, scrollState: Int)
}

class WheelPicker @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defaultItemCount: Int = 5
) : View(context, attrs, defStyleAttr) {

    private val TOP_AND_BOTTOM_FADING_EDGE_STRENGTH = 0.9f
    private val SNAP_SCROLL_DURATION = 300
    private val SELECTOR_MAX_FLING_VELOCITY_ADJUSTMENT = 4
    private val DEFAULT_TEXT_SIZE = 80

    private var mSelectorItemCount: Int
    private var mSelectorVisibleItemCount: Int
    private var mMinIndex: Int
    private var mMaxIndex: Int
    private var mMaxValidIndex: Int? = null
    private var mMinValidIndex: Int? = null

    private var mWheelMiddleItemIndex: Int
    private var mWheelVisibleItemMiddleIndex: Int
    private var mSelectorItemIndices: ArrayList<Int>
    private var mSelectorItemValidStatus: ArrayList<Boolean>
    private var mCurSelectedItemIndex = 0
    private var mWrapSelectorWheelPreferred: Boolean

    private var mTextPaint: Paint = Paint()
    private var mSelectedTextColor: Int
    private var mUnSelectedTextColor: Int
    private var mTextSize: Int
    private var mTextAlign: String

    private var mOverScroller: OverScroller? = null
    private var mVelocityTracker: VelocityTracker? = null
    private val mTouchSlop: Int
    private val mMaximumVelocity: Int
    private val mMinimumVelocity: Int
    private var mLastY: Float = 0f
    private var mIsDragging: Boolean = false
    private var mCurrentFirstItemOffset: Int = 0
    private var mInitialFirstItemOffset = Int.MIN_VALUE
    private var mTextGapHeight: Int = 0
    private var mItemHeight: Int = 0
    private var mTextHeight: Int = 0
    private var mPreviousScrollerY: Int = 0
    private var mOnValueChangeListener: OnValueChangeListener? = null
    private var mOnScrollListener: OnScrollListener? = null
    private var mAdapter: WheelAdapter? = null
    private var mFadingEdgeEnabled = true
    private var mSelectedTextScale = 1f
    private var mTypefaceIndex: Int = 0
    private var mScrollState = OnScrollListener.SCROLL_STATE_IDLE

    private var mSuffix:String = ""

    init {
        val attributesArray = context.obtainStyledAttributes(attrs, R.styleable.WheelPicker, defStyleAttr, 0)
        mSelectorItemCount = defaultItemCount
        mWheelMiddleItemIndex = (mSelectorItemCount - 1) / 2
        mSelectorVisibleItemCount = mSelectorItemCount
        mWheelVisibleItemMiddleIndex = (mSelectorVisibleItemCount - 1) / 2
        mSelectorItemIndices = ArrayList(mSelectorItemCount)
        mSelectorItemValidStatus = ArrayList(mSelectorItemCount)

        mMinIndex = attributesArray.getInt(R.styleable.WheelPicker_min, Integer.MIN_VALUE)
        mMaxIndex = attributesArray.getInt(R.styleable.WheelPicker_max, Integer.MAX_VALUE)
        if (attributesArray.hasValue(R.styleable.WheelPicker_maxValidIndex))
            mMaxValidIndex = attributesArray.getInt(R.styleable.WheelPicker_maxValidIndex, 0)
        if (attributesArray.hasValue(R.styleable.WheelPicker_minValidIndex))
            mMinValidIndex = attributesArray.getInt(R.styleable.WheelPicker_minValidIndex, 0)
        mWrapSelectorWheelPreferred = attributesArray.getBoolean(R.styleable.WheelPicker_wrapSelectorWheel, false)
        mSelectedTextScale = attributesArray.getFloat(R.styleable.WheelPicker_selectedTextScale, 1f)

        mOverScroller = OverScroller(context, DecelerateInterpolator(2.5f))
        val configuration = ViewConfiguration.get(context)
        mTouchSlop = configuration.scaledTouchSlop
        mMaximumVelocity = configuration.scaledMaximumFlingVelocity / SELECTOR_MAX_FLING_VELOCITY_ADJUSTMENT
        mMinimumVelocity = configuration.scaledMinimumFlingVelocity

        mSelectedTextColor = attributesArray.getColor(
            R.styleable.WheelPicker_selectedTextColor
            , ContextCompat.getColor(context, R.color.b1)
        )
        mUnSelectedTextColor = attributesArray.getColor(
            R.styleable.WheelPicker_textColor
            , ContextCompat.getColor(context, R.color.b13)
        )
        mTextSize = attributesArray.getDimensionPixelSize(R.styleable.WheelPicker_textSize, DEFAULT_TEXT_SIZE)
        val textAlignInt = attributesArray.getInt(R.styleable.WheelPicker_align, 1)
        mTextAlign = when (textAlignInt) {
            0 -> "LEFT"
            1 -> "CENTER"
            2 -> "RIGHT"
            else -> "CENTER"
        }
        mFadingEdgeEnabled = attributesArray.getBoolean(R.styleable.WheelPicker_fadingEdgeEnabled, true)
        mTypefaceIndex = attributesArray.getInt(R.styleable.WheelPicker_typeface, 0);

        mTextPaint.run {
            isAntiAlias = true
            isAntiAlias = true
            textSize = mTextSize.toFloat()

            textAlign = Paint.Align.valueOf(mTextAlign)
            style = Paint.Style.FILL_AND_STROKE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        attributesArray.recycle()

        initializeSelectorWheelIndices()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        if (changed) {
            // need to do all this when we know our size
            initializeSelectorWheel()
            initializeFadingEdges()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Try greedily to fit the max width and height.
        var lp: ViewGroup.LayoutParams? = layoutParams
        if (lp == null)
            lp = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        var width = calculateSize(suggestedMinimumWidth, lp.width, widthMeasureSpec)
        var height = calculateSize(suggestedMinimumHeight, lp.height, heightMeasureSpec)

        width += paddingLeft + paddingRight
        height += paddingTop + paddingBottom

        setMeasuredDimension(width, height)
    }

    override fun getSuggestedMinimumWidth(): Int {
        var suggested = super.getSuggestedMinimumHeight()
        if (mSelectorVisibleItemCount > 0) {
            suggested = Math.max(suggested, computeMaximumWidth())
        }
        return suggested
    }

    override fun getSuggestedMinimumHeight(): Int {
        var suggested = super.getSuggestedMinimumWidth()
        if (mSelectorVisibleItemCount > 0) {
            val fontMetricsInt = mTextPaint.fontMetricsInt
            val height = fontMetricsInt.descent - fontMetricsInt.ascent
            suggested = Math.max(suggested, height * mSelectorVisibleItemCount)
        }
        return suggested
    }

    private fun computeMaximumWidth(): Int {
        mTextPaint.textSize = mTextSize * 1.0f
        if (mAdapter != null) {
            return if (!mAdapter!!.getTextWithMaximumLength().isEmpty()) {
                val suggestedWith = mTextPaint.measureText(mAdapter!!.getTextWithMaximumLength()).toInt()
                mTextPaint.textSize = mTextSize * 1.0f
                suggestedWith
            } else {
                val suggestedWith = mTextPaint.measureText("0000").toInt()
                mTextPaint.textSize = mTextSize * 1.0f
                suggestedWith
            }
        }
        val widthForMinIndex = mTextPaint.measureText(mMinIndex.toString()).toInt()
        val widthForMaxIndex = mTextPaint.measureText(mMaxIndex.toString()).toInt()
        mTextPaint.textSize = mTextSize * 1.0f
        return if (widthForMinIndex > widthForMaxIndex)
            widthForMinIndex
        else
            widthForMaxIndex
    }

    private fun calculateSize(suggestedSize: Int, paramSize: Int, measureSpec: Int): Int {
        var result = 0
        val size = MeasureSpec.getSize(measureSpec)
        val mode = MeasureSpec.getMode(measureSpec)

        when (MeasureSpec.getMode(mode)) {
            MeasureSpec.AT_MOST ->

                if (paramSize == ViewGroup.LayoutParams.WRAP_CONTENT)
                    result = Math.min(suggestedSize, size)
                else if (paramSize == ViewGroup.LayoutParams.MATCH_PARENT)
                    result = size
                else {
                    result = Math.min(paramSize, size)
                }
            MeasureSpec.EXACTLY -> result = size
            MeasureSpec.UNSPECIFIED ->

                result = if (paramSize == ViewGroup.LayoutParams.WRAP_CONTENT || paramSize == ViewGroup.LayoutParams
                        .MATCH_PARENT
                )
                    suggestedSize
                else {
                    paramSize
                }
        }

        return result
    }

    private fun initializeSelectorWheel() {
        mItemHeight = getItemHeight()
        mTextHeight = computeTextHeight()
        mTextGapHeight = getGapHeight()

        val visibleMiddleItemPos = mItemHeight * mWheelVisibleItemMiddleIndex + mItemHeight / 2
        mInitialFirstItemOffset = visibleMiddleItemPos - mItemHeight * mWheelMiddleItemIndex
        mCurrentFirstItemOffset = mInitialFirstItemOffset
    }

    private fun initializeFadingEdges() {
        isVerticalFadingEdgeEnabled = mFadingEdgeEnabled
        if (mFadingEdgeEnabled)
            setFadingEdgeLength((bottom - top - mTextSize) / 2)
    }

    private fun initializeSelectorWheelIndices() {
        mSelectorItemIndices.clear()
        mSelectorItemValidStatus.clear()

        mCurSelectedItemIndex = if (mMinValidIndex == null || mMinValidIndex!! < mMinIndex) {
            if (mMinIndex <= 0) {
                0
            } else {
                mMinIndex
            }
        } else {
            if (mMinValidIndex!! <= 0) {
                0
            } else {
                mMinValidIndex!!
            }
        }

        for (i in 0 until mSelectorItemCount) {
            var selectorIndex = mCurSelectedItemIndex + (i - mWheelMiddleItemIndex)
            if (mWrapSelectorWheelPreferred) {
                selectorIndex = getWrappedSelectorIndex(selectorIndex)
            }
            mSelectorItemIndices.add(selectorIndex)
            mSelectorItemValidStatus.add(isValidPosition(selectorIndex))
        }
    }

    override fun getBottomFadingEdgeStrength(): Float {
        return TOP_AND_BOTTOM_FADING_EDGE_STRENGTH
    }

    override fun getTopFadingEdgeStrength(): Float {
        return TOP_AND_BOTTOM_FADING_EDGE_STRENGTH
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawVertical(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        onTouchEventVertical(event)
        return true
    }

    private fun onTouchEventVertical(event: MotionEvent) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }

        mVelocityTracker?.addMovement(event)

        val action: Int = event.actionMasked
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                if (!mOverScroller!!.isFinished)
                    mOverScroller!!.forceFinished(true)

                mLastY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                var deltaY = event.y - mLastY
                if (!mIsDragging && Math.abs(deltaY) > mTouchSlop) {
                    parent?.requestDisallowInterceptTouchEvent(true)

                    if (deltaY > 0) {
                        deltaY -= mTouchSlop
                    } else {
                        deltaY += mTouchSlop
                    }
                    onScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                    mIsDragging = true
                }

                if (mIsDragging) {
                    scrollBy(0, deltaY.toInt())
                    invalidate()
                    mLastY = event.y
                }
            }
            MotionEvent.ACTION_UP -> {
                if (mIsDragging) {
                    mIsDragging = false

                    mVelocityTracker?.computeCurrentVelocity(1000, mMaximumVelocity.toFloat())
                    val velocity = mVelocityTracker?.yVelocity?.toInt()

                    if (Math.abs(velocity!!) > mMinimumVelocity) {
                        mPreviousScrollerY = 0
                        mOverScroller?.fling(
                            scrollX, scrollY, 0, velocity, 0, 0, Integer.MIN_VALUE,
                            Integer.MAX_VALUE, 0, getItemHeight()
                        )
                        invalidateOnAnimation()
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_FLING)
                    }
                    recyclerVelocityTracker()
                } else {
                    //click event
                    val y = event.y.toInt()
                    handlerClickVertical(y)
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                if (mIsDragging) {
                    mIsDragging = false
                }
                recyclerVelocityTracker()
            }
        }
    }

    private fun handlerClickVertical(y: Int) {
        val selectorIndexOffset = y / mItemHeight - mWheelVisibleItemMiddleIndex
        changeValueBySteps(selectorIndexOffset)
    }

    override fun scrollBy(x: Int, y: Int) {
        if (y == 0)
            return

        val gap = mTextGapHeight

        if (!mWrapSelectorWheelPreferred && y > 0
            && (mSelectorItemIndices[mWheelMiddleItemIndex] <= mMinIndex
                    || (mMinValidIndex != null && mSelectorItemIndices[mWheelMiddleItemIndex] <= mMinValidIndex!!))
        ) {
            if (mCurrentFirstItemOffset + y - mInitialFirstItemOffset < gap / 2)
                mCurrentFirstItemOffset += y
            else {
                mCurrentFirstItemOffset = mInitialFirstItemOffset + (gap / 2)
                if (!mOverScroller!!.isFinished && !mIsDragging) {
                    mOverScroller!!.abortAnimation()
                }
            }
            return
        }

        if (!mWrapSelectorWheelPreferred && y < 0
            && (mSelectorItemIndices[mWheelMiddleItemIndex] >= mMaxIndex
                    || (mMaxValidIndex != null && mSelectorItemIndices[mWheelMiddleItemIndex] >= mMaxValidIndex!!))
        ) {
            if (mCurrentFirstItemOffset + y - mInitialFirstItemOffset > -(gap / 2))
                mCurrentFirstItemOffset += y
            else {
                mCurrentFirstItemOffset = mInitialFirstItemOffset - (gap / 2)
                if (!mOverScroller!!.isFinished && !mIsDragging) {
                    mOverScroller!!.abortAnimation()
                }
            }
            return
        }

        mCurrentFirstItemOffset += y

        while (mCurrentFirstItemOffset - mInitialFirstItemOffset < -gap) {
            mCurrentFirstItemOffset += mItemHeight
            increaseSelectorsIndex()
            if (!mWrapSelectorWheelPreferred
                && (mSelectorItemIndices[mWheelMiddleItemIndex] >= mMaxIndex
                        || (mMaxValidIndex != null && mSelectorItemIndices[mWheelMiddleItemIndex] >= mMaxValidIndex!!))
            ) {
                mCurrentFirstItemOffset = mInitialFirstItemOffset
            }
        }

        while (mCurrentFirstItemOffset - mInitialFirstItemOffset > gap) {
            mCurrentFirstItemOffset -= mItemHeight
            decreaseSelectorsIndex()
            if (!mWrapSelectorWheelPreferred
                && (mSelectorItemIndices[mWheelMiddleItemIndex] <= mMinIndex
                        || (mMinValidIndex != null && mSelectorItemIndices[mWheelMiddleItemIndex] <= mMinValidIndex!!))
            ) {
                mCurrentFirstItemOffset = mInitialFirstItemOffset
            }
        }
        onSelectionChanged(mSelectorItemIndices[mWheelMiddleItemIndex], true)
    }

    override fun computeScroll() {
        super.computeScroll()
        if (mOverScroller!!.computeScrollOffset()) {
            val x = mOverScroller!!.currX
            val y = mOverScroller!!.currY


            if (mPreviousScrollerY == 0) {
                mPreviousScrollerY = mOverScroller!!.startY
            }
            scrollBy(x, y - mPreviousScrollerY)
            mPreviousScrollerY = y
            invalidate()
        } else {
            if (!mIsDragging)
            //align item
                adjustItemVertical()
        }
    }

    private fun adjustItemVertical() {
        mPreviousScrollerY = 0
        var deltaY = mInitialFirstItemOffset - mCurrentFirstItemOffset

        if (Math.abs(deltaY) > mItemHeight / 2) {
            deltaY += if (deltaY > 0)
                -mItemHeight
            else
                mItemHeight
        }

        if (deltaY != 0) {
            mOverScroller!!.startScroll(scrollX, scrollY, 0, deltaY, 800)
            invalidateOnAnimation()
        }

        onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE)
    }

    private fun recyclerVelocityTracker() {
        mVelocityTracker?.recycle()
        mVelocityTracker = null
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
    }

    private fun onScrollStateChange(scrollState: Int) {
        if (mScrollState == scrollState) {
            return
        }
        mScrollState = scrollState
        mOnScrollListener?.onScrollStateChange(this, scrollState)
    }

    private fun getItemHeight(): Int {
        return height / (mSelectorItemCount)
    }

    private fun getGapHeight(): Int {
        return getItemHeight() - computeTextHeight()
    }

    private fun computeTextHeight(): Int {
        val metricsInt = mTextPaint.fontMetricsInt
        return Math.abs(metricsInt.bottom + metricsInt.top)
    }

    private fun invalidateOnAnimation() {
        if (Build.VERSION.SDK_INT >= 16)
            postInvalidateOnAnimation()
        else
            invalidate()
    }

    private fun drawVertical(canvas: Canvas) {
        if (mSelectorItemIndices.size == 0)
            return
        val itemHeight = getItemHeight()

        val x = when (mTextPaint.textAlign) {
            Paint.Align.LEFT -> paddingLeft.toFloat()
            Paint.Align.CENTER -> ((right - left) / 2).toFloat()
            Paint.Align.RIGHT -> (right - left).toFloat() - paddingRight.toFloat()
            else -> ((right - left) / 2).toFloat()
        }

        var y = mCurrentFirstItemOffset.toFloat()

        var i = 0

        while (i < mSelectorItemIndices.size) {
            var scale = 1f

            val offsetToMiddle = Math.abs(y - (mInitialFirstItemOffset + mWheelMiddleItemIndex * itemHeight).toFloat())

            var isSelected = false
            if (mSelectorItemValidStatus[i]) {
                if (offsetToMiddle < mItemHeight / 2) {
                    isSelected = true
                    mTextPaint.color = mSelectedTextColor
                } else {
                    mTextPaint.color = mUnSelectedTextColor
                }
            } else {
                mTextPaint.color = ContextCompat.getColor(context, R.color.material_grey_300)
            }

            canvas.save()
            canvas.scale(scale, scale, x, y)
            val item = if(getValue(mSelectorItemIndices[i]).isNotEmpty()) getValue(mSelectorItemIndices[i]) + mSuffix else ""
            if(isSelected) {
                val r = Rect().apply {
                    left = 0
                    right = canvas.width
                    top = (y - itemHeight / 2).toInt()
                    bottom = (y + itemHeight / 2).toInt()
                }
                canvas.drawRoundRect(
                    r.toRectF(), 100f, 100f, Paint().apply {
                        style = Paint.Style.FILL
                        color = Color.parseColor("#f7f7f7")
                        isAntiAlias = true
                    }
                )
            }
            val bounds = Rect().apply {
                mTextPaint.getTextBounds(item, 0, item.length, this)
            }
            canvas.drawText(item, x, y + bounds.height()/2, mTextPaint)

            canvas.restore()

            y += itemHeight
            i++
        }
    }

    private fun getPosition(value: String): Int = when {
        mAdapter != null -> {
            validatePosition(mAdapter!!.getPosition(value))
        }
        else -> try {
            val position = value.toInt()
            validatePosition(position)
        } catch (e: NumberFormatException) {
            0
        }
    }

    private fun increaseSelectorsIndex() {
        for (i in 0 until (mSelectorItemIndices.size - 1)) {
            mSelectorItemIndices[i] = mSelectorItemIndices[i + 1]
            mSelectorItemValidStatus[i] = mSelectorItemValidStatus[i + 1]
        }
        var nextScrollSelectorIndex = mSelectorItemIndices[mSelectorItemIndices.size - 2] + 1
        if (mWrapSelectorWheelPreferred && nextScrollSelectorIndex > mMaxIndex) {
            nextScrollSelectorIndex = mMinIndex
        }
        mSelectorItemIndices[mSelectorItemIndices.size - 1] = nextScrollSelectorIndex
        mSelectorItemValidStatus[mSelectorItemIndices.size - 1] = isValidPosition(nextScrollSelectorIndex)
    }

    private fun decreaseSelectorsIndex() {
        for (i in mSelectorItemIndices.size - 1 downTo 1) {
            mSelectorItemIndices[i] = mSelectorItemIndices[i - 1]
            mSelectorItemValidStatus[i] = mSelectorItemValidStatus[i - 1]
        }
        var nextScrollSelectorIndex = mSelectorItemIndices[1] - 1
        if (mWrapSelectorWheelPreferred && nextScrollSelectorIndex < mMinIndex) {
            nextScrollSelectorIndex = mMaxIndex
        }
        mSelectorItemIndices[0] = nextScrollSelectorIndex
        mSelectorItemValidStatus[0] = isValidPosition(nextScrollSelectorIndex)
    }

    private fun changeValueBySteps(steps: Int) {
        mPreviousScrollerY = 0
        mOverScroller!!.startScroll(0, 0, 0, -mItemHeight * steps, SNAP_SCROLL_DURATION)
        invalidate()
    }

    private fun onSelectionChanged(current: Int, notifyChange: Boolean) {
        val previous = mCurSelectedItemIndex
        mCurSelectedItemIndex = current
        if (notifyChange && previous != current) {
            notifyChange(previous, current)
        }
    }

    private fun getWrappedSelectorIndex(selectorIndex: Int): Int {
        if (selectorIndex > mMaxIndex) {
            return mMinIndex + (selectorIndex - mMaxIndex) % (mMaxIndex - mMinIndex + 1) - 1
        } else if (selectorIndex < mMinIndex) {
            return mMaxIndex - (mMinIndex - selectorIndex) % (mMaxIndex - mMinIndex + 1) + 1
        }
        return selectorIndex
    }

    private fun notifyChange(previous: Int, current: Int) {
        mOnValueChangeListener?.onValueChange(this, getValue(previous).replace(mSuffix, ""), getValue(current).replace(mSuffix, ""))
    }

    private fun validatePosition(position: Int): Int {
        return if (!mWrapSelectorWheelPreferred) {
            when {
                mMaxValidIndex == null && position > mMaxIndex -> mMaxIndex
                mMaxValidIndex != null && position > mMaxValidIndex!! -> mMaxValidIndex!!
                mMinValidIndex == null && position < mMinIndex -> mMinIndex
                mMinValidIndex != null && position < mMinValidIndex!! -> mMinValidIndex!!
                else -> position
            }
        } else {
            getWrappedSelectorIndex(position)
        }
    }

    fun scrollTo(position: Int) {
        if (mCurSelectedItemIndex == position)
            return

        mCurSelectedItemIndex = position
        mSelectorItemIndices.clear()
        for (i in 0 until mSelectorItemCount) {
            var selectorIndex = mCurSelectedItemIndex + (i - mWheelMiddleItemIndex)
            if (mWrapSelectorWheelPreferred) {
                selectorIndex = getWrappedSelectorIndex(selectorIndex)
            }
            mSelectorItemIndices.add(selectorIndex)
        }

        invalidate()
    }

    fun setOnValueChangedListener(onValueChangeListener: OnValueChangeListener) {
        mOnValueChangeListener = onValueChangeListener
    }

    fun setOnScrollListener(onScrollListener: OnScrollListener) {
        mOnScrollListener = onScrollListener
    }

    fun smoothScrollTo(position: Int) {
        val realPosition = validatePosition(position)
        changeValueBySteps(realPosition - mCurSelectedItemIndex)
    }

    fun smoothScrollToValue(value: String) {
        smoothScrollTo(getPosition(value))
    }

    fun scrollToValue(value: String) {
        scrollTo(getPosition(value))
    }

    fun setUnselectedTextColor(resourceId: Int) {
        mUnSelectedTextColor = resourceId
    }

    fun setAdapter(adapter: WheelAdapter?, indexRangeBasedOnAdapterSize: Boolean = true) {
        mAdapter = adapter
        if (mAdapter == null) {
            initializeSelectorWheelIndices()
            invalidate()
            return
        }

        if (adapter!!.getSize() != -1 && indexRangeBasedOnAdapterSize) {
            mMaxIndex = adapter.getSize() - 1
            mMinIndex = 0
        }

        mMaxValidIndex = adapter.getMaxValidIndex()
        mMinValidIndex = adapter.getMinValidIndex()

        initializeSelectorWheelIndices()
        invalidate()

        mAdapter?.picker = this
    }

    fun setTypeface(typeface: Typeface) {
        mTextPaint.typeface = typeface
    }

    fun setWrapSelectorWheel(wrap: Boolean) {
        mWrapSelectorWheelPreferred = wrap
        invalidate()
    }

    fun getWrapSelectorWheel(): Boolean {
        return mWrapSelectorWheelPreferred
    }

    fun setWheelItemCount(count: Int) {
        mSelectorItemCount = count
        mWheelMiddleItemIndex = (mSelectorItemCount - 1) / 2
        mSelectorVisibleItemCount = mSelectorItemCount
        mWheelVisibleItemMiddleIndex = (mSelectorVisibleItemCount - 1) / 2
        mSelectorItemIndices = ArrayList(mSelectorItemCount)
        mSelectorItemValidStatus = ArrayList(mSelectorItemCount)
        reset()
        invalidate()
    }

    fun setSelectedTextColor(colorId: Int) {
        mSelectedTextColor = ContextCompat.getColor(context, colorId)
        invalidate()
    }

    fun getValue(position: Int): String = when {
        mAdapter != null -> mAdapter!!.getValue(position)
        else -> if (!mWrapSelectorWheelPreferred) {
            when {
                position > mMaxIndex -> ""
                position < mMinIndex -> ""
                else -> position.toString()
            }
        } else {
            getWrappedSelectorIndex(position).toString()
        }
    }

    fun setValue(value: String) {
        scrollToValue(value)
    }

    fun setMaxValue(max: Int) {
        mMaxIndex = max
    }

    fun getMaxValue(): String {
        return if (mAdapter != null) {
            mAdapter!!.getValue(mMaxIndex)
        } else {
            mMaxIndex.toString()
        }
    }

    fun setMinValue(min: Int) {
        mMinIndex = min
    }

    fun setMinValidValue(minValid: Int?) {
        mMinValidIndex = minValid
    }

    fun setMaxValidValue(maxValid: Int?) {
        mMaxValidIndex = maxValid
    }

    fun getMinValue(): String {
        return if (mAdapter != null) {
            mAdapter!!.getValue(mMinIndex)
        } else {
            mMinIndex.toString()
        }
    }

    fun getAdapter(): WheelAdapter? {
        return mAdapter
    }

    fun reset() {
        initializeSelectorWheelIndices()
        initializeSelectorWheel()
        invalidate()
    }

    fun getCurrentItem(): String {
        return getValue(mCurSelectedItemIndex)
    }

    fun isValidPosition(position: Int): Boolean {
        return when {
            mMinValidIndex != null && position < mMinValidIndex!! -> false
            mMaxValidIndex != null && position > mMaxValidIndex!! -> false
            else -> true
        }
    }

    fun setSuffix(suffix: String) {
        mSuffix = suffix
    }

    fun getSuffix() = mSuffix
}



internal fun Int.clamp(min: Int, max: Int): Int {
    if (this < min) return min
    return if (this > max) max else this
}


abstract class WheelAdapter {
    abstract fun getValue(position: Int): String
    abstract fun getPosition(vale: String): Int
    abstract fun getTextWithMaximumLength(): String
    open fun getSize(): Int = -1
    open fun getMinValidIndex() : Int? {
        return null
    }
    open fun getMaxValidIndex() : Int? {
        return null
    }
    var picker: WheelPicker? = null
    fun notifyDataSetChanged() {
        picker?.setAdapter(this)
        picker?.requestLayout()
    }
}