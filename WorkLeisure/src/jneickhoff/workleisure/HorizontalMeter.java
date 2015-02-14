package jneickhoff.workleisure;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class HorizontalMeter extends View {

	private int color;
	private int overColor;
	private int backgroundColor;
	private int textColor;
	private float value;
	private float valueNorm;
	private float valueOver;
	private float maxValue;
	private boolean isMaxDisplayed;
	private boolean isBackgroundPainted;
	
	private final static int textSize = 18;
	private Rect meterBackground;
	private Rect meterNorm;
	private Rect meterOver;
	private Point textPos;
	private Point textPos2;
	
	private Paint textPaint;
	private Paint meterBackgroundPaint;
	private Paint meterNormPaint;
	private Paint meterOverPaint;
	
	private int DEFAULT_COLOR = R.color.blue;
	private int DEFAULT_OVER_COLOR = R.color.blue_light2;
	private int DEFAULT_BACKGROUND_COLOR = R.color.light_grey;
	private int DEFAULT_TEXT_COLOR = R.color.light_grey;
	private float DEFAULT_VALUE = 1;
	private boolean DEFAULT_IS_MAX_DISPLAYED = false;
	private boolean DEFAULT_IS_BACKGROUND_PAINTED = false;
	
	public HorizontalMeter(Context context) {
		super(context);
		
		this.color = context.getResources().getColor(DEFAULT_COLOR);
		this.overColor = context.getResources().getColor(DEFAULT_OVER_COLOR);
		this.backgroundColor = context.getResources().getColor(DEFAULT_BACKGROUND_COLOR);
		this.textColor = context.getResources().getColor(DEFAULT_TEXT_COLOR);
		this.value = DEFAULT_VALUE;
		this.valueNorm = DEFAULT_VALUE;
		this.valueOver = 0;
		this.maxValue = DEFAULT_VALUE;
		this.isMaxDisplayed = DEFAULT_IS_MAX_DISPLAYED;
		this.isBackgroundPainted = DEFAULT_IS_BACKGROUND_PAINTED;
		
		init();
	}
	
	public HorizontalMeter(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		TypedArray a = context.obtainStyledAttributes(
				attrs, 
				R.styleable.HorizontalMeter);
		
		try {
			this.color = a.getColor(R.styleable.HorizontalMeter_color, DEFAULT_COLOR);
			this.overColor = a.getColor(R.styleable.HorizontalMeter_overColor, DEFAULT_OVER_COLOR);
			this.textColor = a.getColor(R.styleable.HorizontalMeter_textColor, DEFAULT_TEXT_COLOR);
			this.backgroundColor = a.getColor(R.styleable.HorizontalMeter_backgroundColor, DEFAULT_BACKGROUND_COLOR);
			this.isBackgroundPainted = a.getBoolean(R.styleable.HorizontalMeter_isBackgroundPainted, DEFAULT_IS_BACKGROUND_PAINTED);
			this.value = a.getFloat(R.styleable.HorizontalMeter_value, DEFAULT_VALUE);
			this.maxValue = a.getFloat(R.styleable.HorizontalMeter_maxValue, DEFAULT_VALUE);
			this.isMaxDisplayed = a.getBoolean(R.styleable.HorizontalMeter_isMaxDisplayed, DEFAULT_IS_MAX_DISPLAYED);
		}
		finally {
			a.recycle();
		}
		
		if(value < maxValue) {
			this.valueNorm = value;
			this.valueOver = 0;
		}
		else {
			this.valueNorm = maxValue;
			this.valueOver = value - maxValue;
			if(valueOver > maxValue)
				valueOver = maxValue;
		}
		
		init();
	}
	
	public HorizontalMeter(Context context, float value, float maxValue, boolean isMaxDisplayed, 
			int color, int overColor, int textColor, int backgroundColor, boolean isBackgroundPainted) {
		super(context);
		
		this.color = color;
		this.overColor = overColor;
		this.backgroundColor = backgroundColor;
		this.textColor = textColor;
		this.value = value;
		this.maxValue = maxValue;
		this.isMaxDisplayed = isMaxDisplayed;
		this.isBackgroundPainted = isBackgroundPainted;
		
		if(value < maxValue) {
			this.valueNorm = value;
			this.valueOver = 0;
		}
		else {
			this.valueNorm = maxValue;
			this.valueOver = value - maxValue;
			if(valueOver > maxValue)
				valueOver = maxValue;
		}
		
		init();
	}
	
	private void init() {
		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setColor(textColor);
		textPaint.setTextSize(textSize);
		
		meterBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		meterBackgroundPaint.setColor(backgroundColor);
		meterBackgroundPaint.setStyle(Paint.Style.FILL);
		
		meterNormPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		meterNormPaint.setColor(color);
		meterNormPaint.setStyle(Paint.Style.FILL);
		
		meterOverPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		meterOverPaint.setColor(overColor);
		meterOverPaint.setStyle(Paint.Style.FILL);
	}

	@Override
	public int getSuggestedMinimumWidth() {
		return 200;
	}
	
	@Override
	public int getSuggestedMinimumHeight() {
		return 20;
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
	
	public void setColors(int color, int overColor) {
		meterNormPaint.setColor(color);
		meterOverPaint.setColor(overColor);
	}
	
	public void setValue(float value) {
		setValue(value, maxValue);
	}
	
	public void setValue(float value, float maxValue) {
		this.value = value;
		this.maxValue = maxValue;
		
		if(value < maxValue) {
			this.valueNorm = value;
			this.valueOver = 0;
		}
		else {
			this.valueNorm = maxValue;
			this.valueOver = value - maxValue;
			if(valueOver > maxValue)
				valueOver = maxValue;
		}
		
		onDataChanged();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		onDataChanged();
	}
	
	protected void onDataChanged(){
		int contentWidth = getWidth() - getPaddingLeft() - getPaddingRight();
		int contentHeight = getHeight() - getPaddingTop() - getPaddingBottom();
		
		int textSpace = (int) textPaint.measureText("88.8");
		int spaceBetween = 5;
		int meterWidthBackground, meterWidthNorm, meterWidthOver;
		if(isMaxDisplayed) {
			meterWidthBackground = (int) ((maxValue - valueNorm) / maxValue * (contentWidth - textSpace * 2 - spaceBetween * 2));
			meterWidthNorm = (int) ((valueNorm / maxValue) * (contentWidth - textSpace * 2 - spaceBetween * 2));
			meterWidthOver = (int) ((valueOver / maxValue) * (contentWidth - textSpace * 2 - spaceBetween * 2));
			
			textPos2 = new Point(getPaddingLeft() + textSpace * 2 + spaceBetween * 2 + meterWidthNorm + meterWidthBackground - (int) textPaint.measureText(String.format("%.1f", maxValue)),
								 getPaddingTop() + (contentHeight + textSize)/2 - 3);
		}
		else {
			meterWidthBackground = (int) ((maxValue - valueNorm) / maxValue * (contentWidth - textSpace - spaceBetween));
			meterWidthNorm = (int) ((valueNorm / maxValue) * (contentWidth - textSpace - spaceBetween));
			meterWidthOver = (int) ((valueOver / maxValue) * (contentWidth - textSpace - spaceBetween));
		}		
		
		textPos = new Point(getPaddingLeft() + textSpace - (int) textPaint.measureText(String.format("%.1f",value)), 
							getPaddingTop() + (contentHeight + textSize)/2 - 3);
		meterBackground = new Rect(textSpace + spaceBetween + meterWidthNorm,
								   getPaddingTop(),
								   textSpace + spaceBetween + meterWidthNorm + meterWidthBackground,
								   getPaddingTop() + contentHeight);
		meterNorm = new Rect(textSpace + spaceBetween,
						 	 getPaddingTop(), 
						 	 textSpace + spaceBetween + meterWidthNorm,
						 	 getPaddingTop() + contentHeight);
		meterOver = new Rect(textSpace + spaceBetween,
						 	 getPaddingTop(), 
						 	 textSpace + spaceBetween + meterWidthOver,
						 	 getPaddingTop() + contentHeight);
		
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		canvas.drawText(String.format("%.1f",value), textPos.x, textPos.y, textPaint);
		if(isMaxDisplayed)
			canvas.drawText(String.format("%.1f", maxValue), textPos2.x, textPos2.y, textPaint);
		if(isBackgroundPainted)
			canvas.drawRect(meterBackground, meterBackgroundPaint);
		canvas.drawRect(meterNorm, meterNormPaint);
		canvas.drawRect(meterOver, meterOverPaint);
	}
}
