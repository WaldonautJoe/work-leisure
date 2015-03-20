package jneickhoff.workleisure;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class NotchedHorizontalMeter extends View {

	private int progressColor;
	private int notchColor;
	private int backgroundColor;
	private long value;
	private long valueDisplay;
	private long minValue;
	private long maxValue;
	private List<Calendar> notchList;
	private boolean isBackgroundPainted;
	
	private Rect meterBackground;
	private Rect meterProgress;
	private List<Rect> notchPositions;
	
	private Paint meterProgressPaint;
	private Paint meterNotchPaint;
	private Paint meterBackgroundPaint;
	
	private final static int DEFAULT_PROGRESS_COLOR = R.color.blue_light2;
	private final static int DEFAULT_BACKGROUND_COLOR = R.color.light_grey;
	private final static int DEFAULT_NOTCH_COLOR = R.color.blue;
	private final static int DEFAULT_VALUE = 1;
	private final static int DEFAULT_MIN_VALUE = 0;
	private final static boolean DEFAULT_IS_BACKGROUND_PAINTED = false;
	
	public NotchedHorizontalMeter(Context context) {
		super(context);
		
		this.progressColor = context.getResources().getColor(DEFAULT_PROGRESS_COLOR);
		this.notchColor = context.getResources().getColor(DEFAULT_NOTCH_COLOR);
		this.backgroundColor = context.getResources().getColor(DEFAULT_BACKGROUND_COLOR);
		this.value = DEFAULT_VALUE;
		this.valueDisplay = DEFAULT_VALUE;
		this.minValue = DEFAULT_MIN_VALUE;
		this.maxValue = DEFAULT_VALUE;
		this.isBackgroundPainted = DEFAULT_IS_BACKGROUND_PAINTED;
		
		init();
	}
	
	public NotchedHorizontalMeter(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		TypedArray a = context.obtainStyledAttributes(
				attrs, 
				R.styleable.NotchedHorizontalMeter);
		
		try {
			this.progressColor = a.getColor(R.styleable.NotchedHorizontalMeter_progressColor, DEFAULT_PROGRESS_COLOR);
			this.notchColor = a.getColor(R.styleable.NotchedHorizontalMeter_notchColor, DEFAULT_NOTCH_COLOR);
			this.backgroundColor = a.getColor(R.styleable.NotchedHorizontalMeter_backgroundColor, DEFAULT_BACKGROUND_COLOR);
			this.isBackgroundPainted = a.getBoolean(R.styleable.HorizontalMeter_isBackgroundPainted, DEFAULT_IS_BACKGROUND_PAINTED);
			this.value = a.getInteger(R.styleable.NotchedHorizontalMeter_progressValueInt, DEFAULT_VALUE);
			this.minValue = a.getInteger(R.styleable.NotchedHorizontalMeter_minValueInt, DEFAULT_MIN_VALUE);
			this.maxValue = a.getInteger(R.styleable.NotchedHorizontalMeter_maxValueInt, DEFAULT_VALUE);
		}
		finally {
			a.recycle();
		}
		
		init();
	}
	
	public NotchedHorizontalMeter(Context context, long value, long minValue, long maxValue,  
			int color, int notchColor, int backgroundColor, boolean isBackgroundPainted) {
		super(context);
		
		this.progressColor = color;
		this.notchColor = color;
		this.backgroundColor = backgroundColor;
		this.value = value;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.isBackgroundPainted = isBackgroundPainted;
		
		init();
	}
	
	private void init() {
		if(value < minValue)
			this.valueDisplay = minValue;
		if(value < maxValue)
			this.valueDisplay = value;
		else
			this.valueDisplay = maxValue;
		
		meterProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		meterProgressPaint.setColor(progressColor);
		meterProgressPaint.setStyle(Paint.Style.FILL);
		
		meterNotchPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		meterNotchPaint.setColor(notchColor);
		meterNotchPaint.setStyle(Paint.Style.FILL);
		
		meterBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		meterBackgroundPaint.setColor(backgroundColor);
		meterBackgroundPaint.setStyle(Paint.Style.FILL);
		
		notchList = new ArrayList<Calendar>();
		notchPositions = new ArrayList<Rect>();
	}

	@Override
	public int getSuggestedMinimumWidth() {
		return 200;
	}
	
	@Override
	public int getSuggestedMinimumHeight() {
		return 10;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

	    int widthMode = MeasureSpec.getMode(widthMeasureSpec);
	    int widthSize = MeasureSpec.getSize(widthMeasureSpec);
	    int heightMode = MeasureSpec.getMode(heightMeasureSpec);
	    int heightSize = MeasureSpec.getSize(heightMeasureSpec);

	    int width;
	    int height;

	    if (widthMode == MeasureSpec.EXACTLY) {
	        width = widthSize;
	    } 
	    else if (widthMode == MeasureSpec.AT_MOST) {
	        width = Math.min(getSuggestedMinimumWidth(), widthSize);
	    } 
	    else {
	        width = getSuggestedMinimumWidth();
	    }

	    if (heightMode == MeasureSpec.EXACTLY) {
	        height = heightSize;
	    } 
	    else if (heightMode == MeasureSpec.AT_MOST) {
	        height = Math.min(getSuggestedMinimumHeight(), heightSize);
	    } 
	    else {
	        height = getSuggestedMinimumHeight();
	    }

	    setMeasuredDimension(width, height);
	}
	
	public void setColors(int progressColor, int notchColor) {
		meterProgressPaint.setColor(progressColor);
		meterNotchPaint.setColor(notchColor);
		invalidate();
	}
	
	public void setValue(long value) {
		setValue(value, minValue, maxValue);
	}
	
	public void setValue(long value, long minValue, long maxValue) {
		this.value = value;
		this.minValue = minValue;
		this.maxValue = maxValue;
		
		if(value < minValue)
			this.valueDisplay = minValue;
		if(value < maxValue) {
			this.valueDisplay = value;
		}
		else {
			this.valueDisplay = maxValue;
		}
		
		onDataChanged();
		invalidate();
	}
	
	public void setNotchValues(List<Calendar> notchList) {
		this.notchList.clear();
		this.notchList.addAll(notchList);
		
		onDataChanged();
		invalidate();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		onDataChanged();
	}
	
	protected void onDataChanged(){
		int contentWidth = getWidth() - getPaddingLeft() - getPaddingRight();
		int contentHeight = getHeight() - getPaddingTop() - getPaddingBottom();
		
		int meterWidthBackground, meterWidthProgress;
		
		meterWidthProgress = (int) (((float) (valueDisplay - minValue) / (maxValue - minValue)) * (contentWidth - 2));
		meterWidthBackground = (contentWidth - 2) - meterWidthProgress;
		
		meterProgress = new Rect(1,
			 					 0, 
			 					 1 + meterWidthProgress,
			 					 contentHeight);
		meterProgress.offset(getPaddingLeft(), getPaddingTop());
		meterBackground = new Rect(1 + meterWidthProgress,
								   0,
								   1 + meterWidthProgress + meterWidthBackground,
								   contentHeight);
		meterBackground.offset(getPaddingLeft(), getPaddingTop());
		
		notchPositions.clear();
		for(Calendar cal : notchList) {
			int notchPosition = (int) (((float) (cal.getTimeInMillis() - minValue) / (maxValue - minValue)) * (contentWidth - 2));
			Rect notchRect = new Rect(1 + notchPosition, 
									  0, 
									  4 + notchPosition, 
									  contentHeight);
			notchRect.offset(getPaddingLeft(), getPaddingTop());
			notchPositions.add(notchRect);
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if(isBackgroundPainted)
			canvas.drawRect(meterBackground, meterBackgroundPaint);
		canvas.drawRect(meterProgress, meterProgressPaint);
		for(Rect notch : notchPositions) {
			canvas.drawRect(notch, meterNotchPaint);
		}
	}
}
