package jneickhoff.workleisure;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;

public class HorizontalMeter extends View {

	private int color;
	private float value;
	private float maxValue;
	
	private final static int textSize = 18;
	private Rect meter;
	private Point textPos;
	
	private Paint textPaint;
	private Paint meterPaint;
	
	public HorizontalMeter(Context context) {
		super(context);
		
		this.color = context.getResources().getColor(R.color.blue);
		this.value = 1;
		this.maxValue = 1;
		
		init();
	}
	
	public HorizontalMeter(Context context, float value, float maxValue, int color) {
		super(context);
		
		if(value > maxValue)
			throw new IllegalArgumentException("value must be less than maxValue");
		//TODO draw symbol if value > maxValue
		
		this.color = color;
		this.value = value;
		this.maxValue = maxValue;
		
		init();
	}
	
	private void init() {
		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setColor(getContext().getResources().getColor(R.color.light_grey));
		textPaint.setTextSize(textSize);
		
		meterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		meterPaint.setColor(color);
		meterPaint.setStyle(Paint.Style.FILL);
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
		int meterWidth = (int) ((contentWidth - textSpace - spaceBetween) * (value / maxValue));
		
		textPos = new Point(getPaddingLeft() + textSpace - (int) textPaint.measureText(String.format("%.1f",value)), 
							getPaddingTop() + (contentHeight + textSize)/2 - 3);
		meter = new Rect(textSpace + spaceBetween,
						 getPaddingTop(), 
						 textSpace + spaceBetween + meterWidth,
						 getPaddingTop() + contentHeight);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		canvas.drawText(String.format("%.1f",value), textPos.x, textPos.y, textPaint);
		canvas.drawRect(meter, meterPaint);
	}
}
