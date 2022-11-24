package com.jimmy.two_way_slider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;


/**
 * @author jimmy
 * Created 2022/9/12 at 12:12 上午
 */
public class TwoWaySlider extends View {

    private Listener _l;

    public void setOnSlideListener(@Nullable Listener l) {
        _l = l;
    }

    public static final int LINEAR = 0x00000000;
    public static final int OVAL = 0x00000001;

    @IntDef({LINEAR})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Style {
    }

    public static final int VERTICAL = 0x00000000;
    public static final int HORIZONTAL = 0x00000001;

    @IntDef({HORIZONTAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Orientation {
    }

    public static final int DEFAULT_NUMBER_OF_STEPS = -1;

    private static final boolean debug = false;

    private final StringBuilder zeros = new StringBuilder();

    // ========== Style
    private int _style = LINEAR;
    private int _orientation = HORIZONTAL;

    public void setStyle(@Style int style) {
        _style = style;
        postInvalidate();
    }

    /**
     * Setting the orientation only works if the slider is in Linear Style
     *
     * @param orientation : can either be Vertical or Horizontal
     */
    public void setOrientation(@Orientation int orientation) {
        _orientation = orientation;
        postInvalidate();
    }

    private boolean _shouldShowTick = true;
    private final Paint _tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public void showTick() {
        _shouldShowTick = true;
        postInvalidate();
    }
    public void hideTick() {
        _shouldShowTick = false;
        postInvalidate();
    }


    // ========== Thumbs
    private final Thumb _thumbFrom = new Thumb(new RectF(), new Paint(Paint.ANTI_ALIAS_FLAG));
    private final Thumb _thumbTo = new Thumb(new RectF(), new Paint(Paint.ANTI_ALIAS_FLAG));
    private float _thumbSize;
    private float _thumbFromCenter, _thumbToCenter;
    private int _thumbFromStep = 0; // current position
    private int _thumbToStep = 1;
    private Thumb _lastThumbMoved = _thumbFrom;

    public void setFromThumbColor(@ColorInt int color) {
        _thumbFrom.setColor(color);
        postInvalidate();
    }

    public void setToThumbColor(@ColorInt int color) {
        _thumbTo.setColor(color);
        postInvalidate();
    }

    public int getToThumbColor() {
        return _thumbTo.getColor();
    }

    public int getFromThumbColor() {
        return _thumbFrom.getColor();
    }

    /**
     * @param size in dp
     */
    public void setThumbSize(float size) {
        _thumbSize = size;
        postInvalidate();
    }

    /**
     *
     * @return Thumb Size in db
     */
    public int getThumbSize() {
        return (int)(_thumbSize / Resources.getSystem().getDisplayMetrics().density);//(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, _thumbSize, Resources.getSystem().getDisplayMetrics());
    }

    // ========== Selected Range
    private final Paint _selectedRangePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public void setSelectedRangeColor(int color) {
        _selectedRangePaint.setColor(color);
        postInvalidate();
    }

    public int getSelectedRangeColor() {
        return _selectedRangePaint.getColor();
    }

    public void setSelectedRangeStrokeWidth(float width) {
        if (width < 0) return;
        _selectedRangePaint.setStrokeWidth(width);
        postInvalidate();
    }

    public void setSelectedRangePaint(Paint paint) {
        _selectedRangePaint.set(paint);
        postInvalidate();
    }

    Paint getSelectedRangePaint() {
        return _selectedRangePaint;
    }

    public int getSelectedRangeStrokeWidth() {
        return (int)(_selectedRangePaint.getStrokeWidth() / Resources.getSystem().getDisplayMetrics().density); //(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, _selectedRangePaint.getStrokeWidth(), Resources.getSystem().getDisplayMetrics());
    }

    // ========== Value Label
    private final PointF[] _arrowPointers = new PointF[]{
            new PointF(), new PointF(), new PointF()
    };
    private final Path _valueLabelPath = new Path();
    private final Paint _labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean _shouldShowValueLabel = true;

    public void hideBubbleLabel() {
        _shouldShowValueLabel = false;
        requestLayout();
    }

    public void showBubbleLabel() {
        _shouldShowValueLabel = true;
        requestLayout();
    }

    // ========== Bar related
    private final Paint _barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF _barRect = new RectF();

    public void setBarColor(int color) {
        _barPaint.setColor(color);
        postInvalidate();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void setBarColor(Long color) {
        _barPaint.setColor(color);
    }

    public void setBarStrokeWidth(float strokeWidth) {
        if (strokeWidth == _barPaint.getStrokeWidth()) return;
        _barPaint.setStrokeWidth(strokeWidth);
        postInvalidate();
    }

    /**
     *
     * @return bar stroke width in dp
     */
    public int getBarStrokeWidth() {
        return  (int)(_barPaint.getStrokeWidth() / Resources.getSystem().getDisplayMetrics().density); //(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, _barPaint.getStrokeWidth(), Resources.getSystem().getDisplayMetrics());
    }

    public int getBarColor() {
        return _barPaint.getColor();
    }

    /**
     * Number of Decimal Place in the value the slider returns
     */
    private int _accuracy = 2;
    private boolean _showFullPrecision = false;
    private MathContext _roundMode = new MathContext(_accuracy, RoundingMode.HALF_DOWN);

    /**
     *
     * @param precision is the number of decimal points the slider can show.
     *                  The upper limit is 5, just to keep the ui reasonable.
     */
    public void setPrecision(int precision) {
        if (_accuracy == precision) {
            return;
        }

        if (precision > 6) {
            precision = MAX_PRECISION;
        }

        _accuracy = precision;
        _roundMode = new MathContext(_accuracy, RoundingMode.HALF_DOWN);
    }

    public void shouldShowFullPrecision(boolean shouldShowAllPrecision) {
        _showFullPrecision = shouldShowAllPrecision;
    }

    public float getFromValue() {
        return BigDecimal.valueOf((long)_startValue)
                .add(BigDecimal.valueOf((long)_endValue - (long)_startValue)
                        .multiply(BigDecimal.valueOf((double) _thumbFromStep)
                                .divide(BigDecimal.valueOf((double) getNumberOfSteps()), _roundMode)))
                .round(_roundMode)
                .floatValue();
    }

    public float getToValue() {
        return BigDecimal.valueOf((long)_startValue)
                .add(BigDecimal.valueOf((long)_endValue - (long)_startValue)
                        .multiply(BigDecimal.valueOf((double) _thumbToStep)
                                .divide(BigDecimal.valueOf((double) getNumberOfSteps()), _roundMode)))
                .round(_roundMode)
                .floatValue();
    }

    /**
     * Setting the value before setting the desired
     * total steps will resulted incorrect values being calculated
     * @param value the actual from value
     */
    public void setFromValue(float value) {
        value = validateValueBeforeSetting(value);
        float step = ((float)(value - _startValue)/(float)(_endValue - _startValue) * (float) getNumberOfSteps());

        if (step - (int)step > 0.5) {
            _thumbFromStep = (int) (step + 1);
        } else {
            _thumbFromStep = (int) step;
        }
        postInvalidate();
    }

    /**
     * Setting the value before setting the desired
     * total steps will resulted incorrect values being calculated
     * @param value the actual to value
     */
    public void setToValue(float value) {
        value = validateValueBeforeSetting(value);
        float step = ((float)(value - _startValue)/(float)(_endValue - _startValue) * (float) getNumberOfSteps());

        if (step - (int)step > 0.5) {
            _thumbToStep = (int) (step + 1);
        } else {
            _thumbToStep = (int) step;
        }
        postInvalidate();
    }

    private float validateValueBeforeSetting(float value) {
        if (_startValue < _endValue) {
            if (value < _startValue) {
                value = _startValue;
            } else if (value > _endValue) {
                value = _endValue;
            }

        } else if (_startValue > _endValue) {
            if (value > _startValue) {
                value = _startValue;
            } else if (value < _endValue) {
                value = _endValue;
            }
        }
        return value;
    }

    private int _numberOfSteps = DEFAULT_NUMBER_OF_STEPS;
    /**
     * @param value : To let slider auto calculate the number of steps, set it to {@link DEFAULT_NUMBER_OF_STEPS}.
     *              By default, the slider will return a 2 decimal place value.
     */
    public void setNumberOfSteps(int value) {
        if (value < MIN_NUMBER_OF_STEPS && value != DEFAULT_NUMBER_OF_STEPS) throw new RuntimeException(String.format("Number of step %d cannot be less than 1", value));
        _numberOfSteps = value;
        _thumbToStep = _numberOfSteps ;
        calculateAlignmentPrecision();
        postInvalidate();
    }

    public int getNumberOfSteps() {
        if (_numberOfSteps == DEFAULT_NUMBER_OF_STEPS)  {
            return Math.abs(_startValue - _endValue) * 100;
        } else {
            return _numberOfSteps;
        }
    }

    // ========== To and From Values
    private MathContext _alignmentPrecision = new MathContext(2, RoundingMode.HALF_DOWN);
    private int _startValue = 0;
    private int _endValue = 1;
    private final TextPaint _textPaint = new TextPaint();
    private final Rect _textRect = new Rect();
    private boolean _shouldShowBottomValue = true;

    public int getStartValue() {
        return _startValue;
    }

    public void setStartValue(int value) {
        _startValue = value;
        _thumbFromStep = 0;
        calculateAlignmentPrecision();
        postInvalidate();
    }

    public int getEndValue() {
        return _endValue;
    }

    public void setEndValue(int value) {
        _endValue = value;
        _thumbToStep = getNumberOfSteps();
        calculateAlignmentPrecision();
        postInvalidate();
    }

    public void setTextTypeface(Typeface tf) {
        _textPaint.setTypeface(tf);
        postInvalidate();
    }

    public Typeface getTextTypeface() {
        return _textPaint.getTypeface();
    }

    public void setTextPaint(TextPaint tp) {
        _textPaint.set(tp);
        postInvalidate();
    }

    public void setTextColor(@ColorInt int color) {
        _textPaint.setColor(color);
        postInvalidate();
    }

    /**
     *
     * @param size in sp
     */
    public void setTextSize(int size) {
        _textPaint.setTextSize(getPxFromSp(size));
        postInvalidate();
    }

    /**
     *
     * @return sp of textsize
     */
    public int getTextSize() {
        return (int)(_textPaint.getTextSize() / Resources.getSystem().getDisplayMetrics().density);
    }

    private void calculateAlignmentPrecision() {
        int steps = getNumberOfSteps();
        int newPrecision = 0;
        while(steps > 0) {
            newPrecision ++;
            steps /= 10;
        }
        if (_alignmentPrecision.getPrecision() != newPrecision) {
            _alignmentPrecision = new MathContext(newPrecision, RoundingMode.HALF_DOWN);
        }
    }

    public void hideBottomValue() {
        _shouldShowBottomValue = false;
        requestLayout();
    }

    public void showBottomValue() {
        _shouldShowBottomValue = true;
        requestLayout();
    }

    // need to add public to be accessible, otherwise, this is considered protected, since it is in a library
    public TwoWaySlider(@NonNull Context context) {
        this(context, null);
    }

    public TwoWaySlider(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public TwoWaySlider(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    void init(Context context, @Nullable AttributeSet attrs) {

        setBackgroundColor(Color.TRANSPARENT);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TwoWaySlider);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveAttributeDataForStyleable(context, R.styleable.TwoWaySlider, attrs, a, 0, 0);
        }

        try {

            _shouldShowBottomValue = a.getBoolean(R.styleable.TwoWaySlider_showBottomValue, true);
            _shouldShowValueLabel = a.getBoolean(R.styleable.TwoWaySlider_showValueLabel, true);
            _shouldShowTick = a.getBoolean(R.styleable.TwoWaySlider_showTick, true);

            setNumberOfSteps(a.getInt(R.styleable.TwoWaySlider_numberOfSteps, DEFAULT_NUMBER_OF_STEPS));

            setStartValue(a.getInt(R.styleable.TwoWaySlider_startValue, _startValue));
            setEndValue(a.getInt(R.styleable.TwoWaySlider_endValue, _endValue));

            setFromValue(a.getFloat(R.styleable.TwoWaySlider_thumbFromValue, (float) _startValue));
            setToValue(a.getFloat(R.styleable.TwoWaySlider_thumbToValue, (float) _endValue));

            setPrecision(a.getInt(R.styleable.TwoWaySlider_precision, _accuracy));

            if (a.hasValue(R.styleable.TwoWaySlider_thumbFromColor)) {
                ColorStateList cl = a.getColorStateList(R.styleable.TwoWaySlider_thumbFromColor);
                if (cl != null) {
                    _thumbFrom.setColor(cl.getDefaultColor());
                }
            } else {
                _thumbFrom.setColor(Color.WHITE);
            }

            if (a.hasValue(R.styleable.TwoWaySlider_thumbToColor)) {
                ColorStateList cl = a.getColorStateList(R.styleable.TwoWaySlider_thumbToColor);
                if (cl != null) {
                    _thumbTo.setColor(cl.getDefaultColor());
                }
            } else {
                _thumbTo.setColor(Color.BLACK);
            }

            _thumbSize = a.getDimension(R.styleable.TwoWaySlider_thumbSize, getPxFromDp(DEFAULT_THUMB_SIZE));

            if (a.hasValue(R.styleable.TwoWaySlider_barColor)) {
                ColorStateList cl = a.getColorStateList(R.styleable.TwoWaySlider_barColor);
                if (cl != null) {
                    _barPaint.setColor(cl.getDefaultColor());
                }
            } else {
                _barPaint.setColor(Color.LTGRAY);
            }

            float barPaintStrokeWidth = a.getDimension(R.styleable.TwoWaySlider_barStrokeWidth, getPxFromDp(DEFAULT_BAR_STROKE_WIDTH));
            _barPaint.setStrokeWidth(barPaintStrokeWidth);

            if (a.hasValue(R.styleable.TwoWaySlider_selectedRangeColor)) {
                ColorStateList cl = a.getColorStateList(R.styleable.TwoWaySlider_selectedRangeColor);
                if (cl != null) {
                    _selectedRangePaint.setColor(cl.getDefaultColor());
                }
            } else {
                _selectedRangePaint.setColor(Color.GRAY);
            }

            _selectedRangePaint.setStrokeWidth(a.getDimension(R.styleable.TwoWaySlider_selectedRangeStrokeWidth, getPxFromDp(DEFAULT_THUMB_SIZE)));

            if (a.hasValue(R.styleable.TwoWaySlider_tickColor)) {
                ColorStateList cl = a.getColorStateList(R.styleable.TwoWaySlider_tickColor);
                if (cl != null) {
                    _tickPaint.setColor(cl.getDefaultColor());
                }
            }

            String fontName = a.getString(R.styleable.TwoWaySlider_fontName);

            if (fontName != null && !fontName.isEmpty()) {
                Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/" + fontName);
                _textPaint.setTypeface(tf);
            } else if (a.hasValue(R.styleable.TwoWaySlider_typeface)) {
                    int fontId = a.getResourceId(R.styleable.TwoWaySlider_typeface, -1);
                    Typeface tf = ResourcesCompat.getFont(context, fontId);
                    _textPaint.setTypeface(tf);
            } else {
                _textPaint.setTypeface(Typeface.DEFAULT);
            }

            if (a.hasValue(R.styleable.TwoWaySlider_textColor)) {
                ColorStateList cl = a.getColorStateList(R.styleable.TwoWaySlider_textColor);
                if (cl != null) {
                    _textPaint.setColor(cl.getDefaultColor());
                }
            } else {
                _textPaint.setColor(Color.BLACK);
            }

            _textPaint.setTextSize(a.getDimension(R.styleable.TwoWaySlider_textSize, getPxFromSp(DEFAULT_TEXT_SIZE)));

            if (a.hasValue(R.styleable.TwoWaySlider_tickColor)) {
                ColorStateList cl = a.getColorStateList(R.styleable.TwoWaySlider_tickColor);
                if (cl != null) {
                    _tickPaint.setColor(cl.getDefaultColor());
                }
            } else {
                _tickPaint.setColor(Color.WHITE);
            }

        } finally {
            a.recycle();
        }

        _thumbFrom.setPaintStyle(Paint.Style.FILL);
        _thumbTo.setPaintStyle(Paint.Style.FILL);

        _barPaint.setStrokeCap(Paint.Cap.ROUND);
        _barPaint.setStyle(Paint.Style.STROKE);
        _barPaint.setStrokeWidth(getPxFromDp(DEFAULT_BAR_STROKE_WIDTH));

        _selectedRangePaint.setStyle(Paint.Style.STROKE);

        _tickPaint.setStyle(Paint.Style.FILL);

        _labelPaint.setStrokeJoin(Paint.Join.BEVEL);
        _labelPaint.setStyle(Paint.Style.STROKE);
        _labelPaint.setStrokeWidth(getPxFromDp(2));
        _labelPaint.setPathEffect(new CornerPathEffect(5));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int measuredWidth = getMeasuredWidth(); //MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int measuredHeight = getMeasuredHeight(); //MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int resultWidth = measuredWidth;
        int resultHeight = measuredHeight;

        // start value text bounds
        String startValueString = String.valueOf(_startValue);
        _textPaint.getTextBounds(startValueString, 0, startValueString.length(), _textRect);
        int startTextWidth = _textRect.width();
        int startTextHeight = _textRect.height();

        // end value text bounds
        String endValueString = String.valueOf(_endValue);
        _textPaint.getTextBounds(endValueString, 0, endValueString.length(), _textRect);
        int endTextWidth = _textRect.width() + 1;

        // longest value text bounds (default 2 decimal place + '.' + largest start value or end value
        zeros.delete(0, zeros.length());
        for (int i = 0; i < _accuracy; i++) {
            zeros.append("0");
        }
        @SuppressLint("DefaultLocale")
        String valueWithDecimalString = String.format("%d.%s", endTextWidth > startTextWidth ? _endValue : _startValue, zeros);
        _textPaint.getTextBounds(valueWithDecimalString, 0, valueWithDecimalString.length(), _textRect);
        float textMaxHeight = _textPaint.getFontMetrics().bottom - _textPaint.getFontMetrics().top;
        float textMaxWidth = _textRect.width();

        // Bubble Height and Width
        float bubbleHeight = textMaxHeight + getPxFromDp(BUBBLE_VERTICAL_PADDING) * 2;
        float bubbleWidth = textMaxWidth + getPxFromDp(BUBBLE_HORIZONTAL_PADDING) * 2;

        // Bubble to Bar Distance
        float bubbleToBarDistance = getPxFromDp(BUBBLE_TO_BAR_DISTANCE);

        float thumbSize = _thumbSize;

        if (_barPaint.getStrokeWidth() > thumbSize) {
            thumbSize = _barPaint.getStrokeWidth();
        }

        int minHeight, minWidth;
        switch (_style) {
            case LINEAR:
                switch (_orientation) {
                    case HORIZONTAL:
                        minHeight = _barPaint.getStrokeWidth() > thumbSize ?
                                (int) (_barPaint.getStrokeWidth() + getPaddingTop() + getPaddingBottom()) :
                                (int) (thumbSize + getPaddingTop() + getPaddingBottom());

                        if (_shouldShowValueLabel) {
                            minHeight += bubbleToBarDistance + bubbleHeight;
                        }

                        if (_shouldShowBottomValue) {
                            minHeight += textMaxHeight + getPxFromDp(BAR_TO_TEXT_MARGIN) ;
                        }

                        // The text at the bottom
                        minWidth = (int) (thumbSize * 2 + getPaddingLeft() + getPaddingRight());

                        resultWidth = Math.max(measuredWidth, minWidth);
                        resultHeight = Math.min(measuredHeight, minHeight);
                        setMinimumWidth(minWidth);
                        setMinimumHeight(minHeight);
                        break;
                    case VERTICAL:
                        minWidth = (int) (textMaxWidth + thumbSize + getPxFromDp(BAR_TO_TEXT_MARGIN) + bubbleToBarDistance + bubbleWidth + getPaddingLeft() + getPaddingRight());
                        float topPadding = startTextHeight > thumbSize ? startTextHeight / 2f : thumbSize / 2;
                        minHeight = (int) (topPadding * 2 + thumbSize + getPaddingTop() + getPaddingBottom());

                        resultWidth = Math.min(measuredWidth, minWidth);
                        resultHeight= Math.max(measuredHeight, minHeight);
                        setMinimumWidth(minWidth);
                        setMinimumHeight(minHeight);
                        break;
                }
                break;
            case OVAL:
                if (measuredHeight < measuredWidth) {
                    resultHeight = measuredWidth;
                } else {
                    resultWidth = measuredHeight;
                }
                break;
        }

        setMeasuredDimension(resultWidth, resultHeight);
    }

    float calculateThumbXPosition(int thumbPosition, float barWidth, float startPadding) {

        float barEndPadding = _thumbSize / 2f;
        if (_barPaint.getStrokeWidth() > _thumbSize) {
            barEndPadding = _barPaint.getStrokeWidth() / 2f;
        }

        // due to the canvas translation, thumbCenterX can ignore startPadding
        float thumbCenterX =  BigDecimal.valueOf((double) thumbPosition)
                .divide(BigDecimal.valueOf((double) getNumberOfSteps()), _alignmentPrecision)
                .multiply(BigDecimal.valueOf((double) barWidth))
                .floatValue() + barEndPadding + startPadding;

        if (thumbCenterX < barEndPadding + startPadding) {
            thumbCenterX = barEndPadding + startPadding;
        } else if (thumbCenterX > barEndPadding + barWidth + startPadding) {
            thumbCenterX =  barEndPadding + barWidth + startPadding;
        }

        return thumbCenterX;
    }

    int calculateThumbStepWithCenter(float thumbCenter) {

        double steps = (double) (thumbCenter - _barRect.left) / (_barRect.width()) * getNumberOfSteps();
        double diff = steps - (int) Math.floor(steps);
        if (diff >= 0.5) {
            steps++;
        }

        return (int) steps;
    }

    float getPositionForStep(int step, float barWidth, float startPadding) {

        float barEndPadding = _thumbSize / 2f;
        if (_barPaint.getStrokeWidth() > _thumbSize) {
            barEndPadding = _barPaint.getStrokeWidth() / 2f;
        }

        return BigDecimal.valueOf((double) step)
                .divide(BigDecimal.valueOf((double) getNumberOfSteps()), _alignmentPrecision)
                .multiply(BigDecimal.valueOf((double) barWidth))
                .floatValue() + barEndPadding + startPadding;
    }

    private final Paint tempPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw Background
        super.onDraw(canvas);

        if (debug) {
            tempPaint.setStyle(Paint.Style.STROKE);
            tempPaint.setStrokeWidth(1);
            float currentTop = ((getBottom() - getTop()) - getMeasuredHeight()) / 2f;
            canvas.drawRect(0, currentTop, getMeasuredWidth(), currentTop + getMeasuredHeight(), tempPaint);
        }

        float startPadding = (float) getPaddingLeft();
        float endPadding = (float) getPaddingRight();
        if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) {
            startPadding = (float) getPaddingRight();
            endPadding = (float) getPaddingLeft();
        }

        switch (_style) {
            case LINEAR:
                switch (_orientation) {
                    case HORIZONTAL:
                        float barEndPadding = _thumbSize /2;
                        if (_barPaint.getStrokeWidth() > _thumbSize) {
                            barEndPadding = _barPaint.getStrokeWidth() / 2;
                        }

                        float textHeight = 0;

                        // DRAW the BAR above the text with a distance of Bar To Text Margin + half (the diff between line width and thumb
                        float barBottom = (getBottom() - getTop() - getMeasuredHeight()) / 2f + (float) getMeasuredHeight();
                        float barCenterY = barBottom - getPaddingBottom() - _barPaint.getStrokeWidth() / 2f;

                        if(getBarStrokeWidth() < getThumbSize()) {
                            barCenterY = barBottom - getPaddingBottom() - getPxFromDp(getThumbSize()) / 2f;
                        }

                        if (getBarStrokeWidth() < getSelectedRangeStrokeWidth() && getThumbSize() < getSelectedRangeStrokeWidth()) {
                            barCenterY = barBottom - getPaddingBottom() - _selectedRangePaint.getStrokeWidth() / 2f;
                        }

                        if (_shouldShowBottomValue) {

                            // Draw the Text on both ends of the bar
                            if (debug) {
                                canvas.save();
                                canvas.translate(startPadding, (getBottom() - getTop() - getMeasuredHeight())/ 2f);
                                tempPaint.setColor(Color.rgb(255, 100, 100));
                                canvas.drawRect(0f, 0f, getMeasuredWidth() - startPadding - endPadding, getMeasuredHeight(), tempPaint);
                                canvas.restore();
                            }
                            // draw start and end value at the bottom corners with margin to ends of slider size / 2

                            _textPaint.getTextBounds(String.valueOf(_endValue), 0, String.valueOf(_endValue).length(), _textRect);
                            float endValueWidth = _textRect.width();

                            _textPaint.getTextBounds(String.valueOf(_startValue), 0, String.valueOf(_startValue).length(), _textRect);
                            float startValueWidth = _textRect.width();

                            textHeight = _textPaint.getFontMetrics().bottom - _textPaint.getFontMetrics().top;

                            barCenterY -= (textHeight + getPxFromDp(BAR_TO_TEXT_MARGIN));

                            float baseline = (float) (barBottom - getPxFromDp(BAR_TO_TEXT_MARGIN));

                            canvas.drawText(String.valueOf(_startValue),
                                    (barEndPadding < startValueWidth / 2 ?
                                            getPxFromDp(EXTRA_SPACE): barEndPadding - 0.5f * startValueWidth) + startPadding,
                                    baseline,
                                    _textPaint);

                            canvas.drawText(String.valueOf(_endValue),
                                    (float) getMeasuredWidth() - endPadding - (barEndPadding < endValueWidth ?
                                             endValueWidth + getPxFromDp(EXTRA_SPACE):
                                            barEndPadding * 2 - (barEndPadding * 2 - endValueWidth) / 2),
                                    baseline,
                                    _textPaint);
                        }

                        float barLength = getMeasuredWidth() - startPadding - endPadding - barEndPadding * 2;
                        _barRect.set(
                                barEndPadding + startPadding,
                                barCenterY,
                                barEndPadding + startPadding + barLength,
                                barCenterY
                                );
                        canvas.drawLine(
                                _barRect.left, _barRect.top,
                                _barRect.right, _barRect.bottom,
                                _barPaint);

                        // draw thumb
                        if (!_thumbFrom.isMoving()) {
                            _thumbFromCenter = calculateThumbXPosition(
                                    _thumbFromStep, barLength,
                                    startPadding);
                        }


                        float thumbOriginX = _thumbFromCenter - _thumbSize / 2;

                        _thumbFrom.setRectF(thumbOriginX, barCenterY - _thumbSize / 2, thumbOriginX + _thumbSize, barCenterY + _thumbSize / 2);

                        if (!_thumbTo.isMoving()) {
                            _thumbToCenter = calculateThumbXPosition(
                                    _thumbToStep, barLength,
                                    startPadding);
                        }
                        thumbOriginX = _thumbToCenter - _thumbSize / 2;
                        _thumbTo.setRectF(thumbOriginX, barCenterY - _thumbSize / 2, thumbOriginX + _thumbSize, barCenterY + _thumbSize / 2);

                        float x0 = _thumbFromCenter;
                        float x1 = _thumbToCenter;

                        if (x0 > x1) {
                            x0 = _thumbToCenter;
                            x1 = _thumbFromCenter;
                        }
                        canvas.drawLine(x0, barCenterY, x1, barCenterY, _selectedRangePaint);

                        if (_thumbFrom.isMoving() || _lastThumbMoved == _thumbFrom) {
                            canvas.drawOval(_thumbTo.rect, _thumbTo.paint);
                            canvas.drawOval(_thumbFrom.rect, _thumbFrom.paint);
                        } else {
                            canvas.drawOval(_thumbFrom.rect, _thumbFrom.paint);
                            canvas.drawOval(_thumbTo.rect, _thumbTo.paint);
                        }

                        if (_thumbTo.isMoving()) {
                            // draw the indicator
                            drawValueLabelForThumb(canvas, _thumbTo.rect, String.valueOf(getToValue()), _thumbTo.getColor(), startPadding, endPadding);
                        }

                        if (_thumbFrom.isMoving()) {
                            // draw the indicator
                            drawValueLabelForThumb(canvas, _thumbFrom.rect, String.valueOf(getFromValue()), _thumbFrom.getColor(), startPadding, endPadding);
                        }

                        if (_shouldShowTick && getNumberOfSteps() != DEFAULT_NUMBER_OF_STEPS && getNumberOfSteps() > 2 && barLength / getNumberOfSteps() > getPxFromDp(3)) {
                            for (int i = 0; i <= getNumberOfSteps(); i++) {
                                float x = getPositionForStep(i, barLength, startPadding);
                                float r = ((_barRect.bottom - _barRect.top) - getPxFromDp(2)) / 6f;

                                if (r * 2 < barLength / getNumberOfSteps()) {
                                    r = (barLength / getNumberOfSteps()) / 6f;
                                }

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    canvas.drawOval(x - r, barCenterY - r, x + r, barCenterY + r, _tickPaint);
                                } else {
                                    canvas.drawCircle(x, barCenterY, r, _tickPaint);
                                }
                            }
                        }


                        break;
                    case VERTICAL:
                        break;
                }
                break;
            case OVAL:
                break;
        }
    }

    void drawValueLabelForThumb(Canvas canvas, RectF thumbRect, String value, int color, float startPadding, float endPadding) {

        if (_showFullPrecision) {
            String[] strings = value.split("[.]");

            while (strings[1].length() < _accuracy) {
                strings[1] = strings[1].concat("0");
            }

            value = strings[0] + "." + strings[1];
        }

        _textPaint.getTextBounds(value, 0, value.length(), _textRect);
        _labelPaint.setColor(color);

        float thumbSize = _thumbSize;
        float labelToBarDistance = getPxFromDp(BUBBLE_TO_BAR_DISTANCE);
        _arrowPointers[0].set(thumbRect.centerX() - thumbSize /2f, thumbRect.top - labelToBarDistance);
        _arrowPointers[1].set(thumbRect.centerX(), thumbRect.top);
        _arrowPointers[2].set(thumbRect.centerX() + thumbSize /2f, thumbRect.top - labelToBarDistance);

        _valueLabelPath.reset();

        // calculate the proper width
        float valueLabelLeft = _textRect.width()/2f + getPxFromDp(BUBBLE_HORIZONTAL_PADDING) + startPadding > thumbRect.centerX() ?
                startPadding + _labelPaint.getStrokeWidth():
                thumbRect.centerX() - _textRect.width()/2f - getPxFromDp(BUBBLE_HORIZONTAL_PADDING);

        if (valueLabelLeft == _labelPaint.getStrokeWidth() + startPadding) {
            // far left
            _arrowPointers[0].set(valueLabelLeft, _arrowPointers[0].y);
        }

        float valueLabelBottom = thumbRect.top - labelToBarDistance;
        float valueLabelTop = valueLabelBottom - getPxFromDp(BUBBLE_VERTICAL_PADDING) * 2 - _textRect.height();

        /*
            Here is the structure of the value label
            (value label left) | -(BUBBLE_HORIZONTAL_PADDING) - (text) - (BUBBLE_HORIZONTAL_PADDING) -| (value Label Right)

            If it approach the far right, then :
            valueLabelRight = getMeasuredWidth - endPadding - paintLabel.strokeWidth
         */
        float valueLabelRight = valueLabelLeft + _textRect.width() + getPxFromDp(BUBBLE_HORIZONTAL_PADDING) * 2 > getMeasuredWidth() - endPadding ?
                getMeasuredWidth() - _labelPaint.getStrokeWidth() - endPadding:
                valueLabelLeft + _textRect.width() + getPxFromDp(BUBBLE_HORIZONTAL_PADDING) * 2;

        if (valueLabelRight == getMeasuredWidth() - _labelPaint.getStrokeWidth() - endPadding) {
            // this means the thumb is at the far right
            valueLabelLeft = valueLabelRight - (_textRect.width() + getPxFromDp(BUBBLE_HORIZONTAL_PADDING) * 2);
            _arrowPointers[2].set(valueLabelRight, _arrowPointers[2].y);
        }

        _valueLabelPath.moveTo(_arrowPointers[0].x, _arrowPointers[0].y);
        _valueLabelPath.lineTo(_arrowPointers[1].x, _arrowPointers[1].y);
        _valueLabelPath.lineTo(_arrowPointers[2].x, _arrowPointers[2].y);
        _valueLabelPath.lineTo(valueLabelRight, _arrowPointers[2].y);
        _valueLabelPath.lineTo(valueLabelRight, valueLabelTop);
        _valueLabelPath.lineTo(valueLabelLeft, valueLabelTop);
        _valueLabelPath.lineTo(valueLabelLeft, _arrowPointers[0].y);
        _valueLabelPath.close();

        canvas.drawPath(_valueLabelPath, _labelPaint);
        canvas.drawText(value, valueLabelLeft + getPxFromDp(BUBBLE_HORIZONTAL_PADDING), valueLabelBottom - getPxFromDp(BUBBLE_VERTICAL_PADDING), _textPaint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // check if touch is on _slider rects
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                if (_lastThumbMoved.rect.contains(x,y)) {
                    _lastThumbMoved.setState(Thumb.MOVING);
                    postInvalidate();
                    return true;
                } else {
                    if (_thumbTo.rect.contains(x, y)) {
                        _thumbTo.setState(Thumb.MOVING);
                        _lastThumbMoved = _thumbTo;
                        postInvalidate();
                        return true;
                    } else if (_thumbFrom.rect.contains(x, y)) {
                        _thumbFrom.setState(Thumb.MOVING);
                        _lastThumbMoved = _thumbFrom;
                        postInvalidate();
                        return true;
                    }
                }

            case MotionEvent.ACTION_MOVE:
                if (_thumbFrom.isMoving() || _thumbTo.isMoving()) {

                    if (_thumbFrom.isMoving()) {
                        _thumbFromCenter = x;
                        if (_thumbFromCenter < _barRect.left) {
                            _thumbFromCenter = _barRect.left;
                        } else if (_thumbFromCenter > _barRect.right) {
                            _thumbFromCenter = _barRect.right;
                        }
                        // save thumb location
                        _thumbFromStep = calculateThumbStepWithCenter(_thumbFromCenter);
                    }

                    if (_thumbTo.isMoving()) {
                        _thumbToCenter = x;
                        if (_thumbToCenter < _barRect.left) {
                            _thumbToCenter = _barRect.left;
                        } else if (_thumbToCenter > _barRect.right) {
                            _thumbToCenter = _barRect.right;
                        }
                        // save thumb location
                        _thumbToStep = calculateThumbStepWithCenter(_thumbToCenter);
                    }

                    if (_l != null) {
                        _l.onSliderMoved(getFromValue(), getToValue());
                    }

                    postInvalidate();
                    return true;
                }

                break;

            case MotionEvent.ACTION_UP:

                if (_thumbTo.state == Thumb.IDLE && _thumbFrom.state == Thumb.IDLE) {
                    performClick();
                }

                _thumbTo.setState(Thumb.IDLE);
                _thumbFrom.setState(Thumb.IDLE);
                postInvalidate();
                return true;

            case MotionEvent.ACTION_CANCEL:
                _thumbTo.setState(Thumb.IDLE);
                _thumbFrom.setState(Thumb.IDLE);
                postInvalidate();
                return true;
        }

        return super.onTouchEvent(event);
    }


    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle b = new Bundle();
        b.putParcelable("superState", super.onSaveInstanceState());
        b.putInt(THUMB_TO_STEP, _thumbToStep);
        b.putInt(THUMB_FROM_STEP, _thumbFromStep);

        return b;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {

        if (state instanceof Bundle) {
            Bundle b = (Bundle) state;
            _thumbToStep = b.getInt(THUMB_TO_STEP);
            _thumbFromStep = b.getInt(THUMB_FROM_STEP);
            state = b.getParcelable("superState");
        }

        super.onRestoreInstanceState(state);
    }

    static final String THUMB_TO_STEP = "Thumb To Step";
    static final String THUMB_FROM_STEP = "Thumb From Step";

    static final int BUBBLE_VERTICAL_PADDING = 10;
    static final int BUBBLE_HORIZONTAL_PADDING = 10;
    static final int BUBBLE_TO_BAR_DISTANCE = 10;

    public static final int DEFAULT_BAR_STROKE_WIDTH = 8;
    static final int BAR_TO_TEXT_MARGIN = 5;

    public static final int DEFAULT_THUMB_SIZE = 20;

    static final int DEFAULT_TEXT_SIZE = 16;
    static final int MAX_PRECISION = 5;
    static final int MIN_NUMBER_OF_STEPS = 1;

    static final int EXTRA_SPACE = 5;

    protected static float getPxFromDp(int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) dp, Resources.getSystem().getDisplayMetrics());
    }

    protected static float getPxFromSp(int sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, (float) sp, Resources.getSystem().getDisplayMetrics());
    }

    public interface Listener {
        void onSliderMoved(float from, float to);
    }

    public class Thumb {

        public final static int IDLE = 0x00000000;
        public final static int MOVING = 0x00000001;

        public int state = IDLE;
        public RectF rect;
        public Paint paint;

        public Thumb(RectF rectF, Paint paint) {
            this.rect = rectF;
            this.paint = paint;
        }

        public boolean isMoving() {
            return state == MOVING;
        }

        public void setState(int state) {
            if (state == IDLE || state == MOVING) {
                this.state = state;
            }
        }

        public void setColor(int color) {
            paint.setColor(color);
        }

        public int getColor() {
            return paint.getColor();
        }

        public void setPaintStyle(Paint.Style style) {
            paint.setStyle(style);
        }

        public Paint getPaint() {
            return paint;
        }

        public void setRectF(RectF rect) {
            this.rect.set(rect);
        }

        public void setRectF(float left, float top, float right, float bottom) {
            this.rect.set(left, top, right, bottom);
        }
    }
}
