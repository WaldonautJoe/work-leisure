package jneickhoff.workleisure;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;

public class HorizontalMeter extends View {

	private int color;
	private int overColor;
	private float value;
	private float valueNorm;
	private float valueOver;
	private float maxValue;
	
	private final static int textSize = 18;
	private Rect meterNorm;
	private Rect meterOver;
	private Point textPos;
	
	private Paint textPaint;
	private Paint meterNormPaint;
	private Paint meterOverPaint;
	
	public HorizontalMeter(Context context) {
		super(context);
		
		this.color = context.getResources().getColor(R.color.blue);
		this.value = 1;
		this.valueNorm = 1;
		this.valueOver = 0;
		this.maxValue = 1;
		
		init();
	}
	
	public HorizontalMeter(Context context, float value, float maxValue, int color, int overColor) {
		super(context);
		
		this.color = color;
		this.overColor = overColor;
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
		
		init();
	}
	
	private void init() {
		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setColor(getContext().getResources().getColor(R.color.light_grey));
		textPaint.setTextSize(textSize);
		
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
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		int contentWidth = w - getPaddingLeft() - getPaddingRight();
		int contentHeight = h - getPaddingTop() - getPaddingBottom();
		
		int textSpace = (int) textPaint.measureText("88.8");
		int spaceBetween = 5;
		int meterWidthNorm = (int) ((valueNorm / maxValue) * (contentWidth - textSpace - spaceBetween));
		int meterWidthOver = (int) ((valueOver / maxValue) * (contentWidth - textSpace - spaceBetween));
		
		textPos = new Point(getPaddingLeft() + textSpace - (int) textPaint.measureText(String.format("%.1f",value)), 
							getPaddingTop() + (contentHeight + textSize)/2 - 3);
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
		canvas.drawRect(meterNorm, meterNormPaint);
		canvas.drawRect(meterOver, meterOverPaint);
	}
}
