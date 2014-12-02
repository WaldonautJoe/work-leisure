package jneickhoff.workleisure;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class MirrorBarGraph extends View {

	//dimensions and coordinates
	private Line zeroLine;
	private float contentWidth;
	private float contentHeight;
	private int paddingLeft;
	private int paddingTop;
	private int paddingRight;
	private int paddingBottom;
	
	private List<Data> dataColumns;
	
	private float maxValue;
	private float DEFAULT_MAX_VALUE = 1.0f;
	private int lineColor;
	private int labelColor;
	private boolean showLabels;
	private float labelTextSize;
	private float minTextSize;
	private float labelTextWidth;
	private Label[] yLabels;
	private int topDataColor;
	private int bottomDataColor;
	private Paint sideLabelPaint;
	private Paint bottomLabelPaint;
	private Paint dataTopPaint;
	private Paint dataBottomPaint;
	private Paint linePaint;
	
	//default attribute values
	public final int DEFAULT_LINE_COLOR = Color.GRAY;
	public final int DEFAULT_LABEL_COLOR = Color.BLACK;
	public final boolean DEFAULT_SHOW_LABELS = true;
	public final float DEFAULT_LABEL_TEXT_SIZE = 14.0f;
	public final float DEFAULT_MIN_TEXT_SIZE = 9.0f;
	public final int DEFAULT_TOP_DATA_COLOR = Color.BLUE;
	public final int DEFAULT_BOTTOM_DATA_COLOR = Color.RED;
	
	public MirrorBarGraph(Context context) {
		super(context);
				
		lineColor = DEFAULT_LINE_COLOR;
		labelColor = DEFAULT_LABEL_COLOR;
		showLabels = DEFAULT_SHOW_LABELS;
		labelTextSize = DEFAULT_LABEL_TEXT_SIZE;
		minTextSize = DEFAULT_MIN_TEXT_SIZE;
		topDataColor = DEFAULT_TOP_DATA_COLOR;
		bottomDataColor = DEFAULT_BOTTOM_DATA_COLOR;
		
		init();
	}
	
	public MirrorBarGraph(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		TypedArray a = context.obtainStyledAttributes(
				attrs, 
				R.styleable.MirrorBarGraph);
		
		try {
			lineColor = a.getColor(R.styleable.MirrorBarGraph_lineColor, DEFAULT_LINE_COLOR);
			labelColor = a.getColor(R.styleable.MirrorBarGraph_labelColor, DEFAULT_LABEL_COLOR);
			showLabels = a.getBoolean(R.styleable.MirrorBarGraph_showLabels, DEFAULT_SHOW_LABELS);
			labelTextSize = a.getDimension(R.styleable.MirrorBarGraph_labelTextSize, DEFAULT_LABEL_TEXT_SIZE);
			minTextSize = a.getDimension(R.styleable.MirrorBarGraph_minTextSize, DEFAULT_MIN_TEXT_SIZE);
			topDataColor = a.getColor(R.styleable.MirrorBarGraph_topDataColor, DEFAULT_TOP_DATA_COLOR);
			bottomDataColor = a.getColor(R.styleable.MirrorBarGraph_bottomDataColor, DEFAULT_BOTTOM_DATA_COLOR);
		} 
		finally {
			a.recycle();
		}
		
		init();
	}
	
	private void init() {
		dataColumns = new ArrayList<Data>();
		maxValue = DEFAULT_MAX_VALUE;
		yLabels = new Label[5];
		for(int i = 0; i < yLabels.length; i++)
			yLabels[i] = new Label("0.0");
		if(labelTextSize < minTextSize)
			labelTextSize = minTextSize;
		
		sideLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		sideLabelPaint.setColor(this.labelColor);
		sideLabelPaint.setTextSize(labelTextSize);
		
		bottomLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		bottomLabelPaint.setColor(this.labelColor);
		bottomLabelPaint.setTextSize(labelTextSize);
		
		dataTopPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		dataTopPaint.setColor(this.topDataColor);
		dataTopPaint.setStyle(Paint.Style.FILL);
		
		dataBottomPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		dataBottomPaint.setColor(this.bottomDataColor);
		dataBottomPaint.setStyle(Paint.Style.FILL);
		
		linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		linePaint.setColor(this.lineColor);
	}
	
	public void fillTestValues() {
		addColumnPair("12/23", 1, 3);
		addColumnPair("12/24", 2, 2);
		addColumnPair("12/25", 3, 1);
		addColumnPair("12/26", 1, 3);
		addColumnPair("12/27", 1, 4);
		addColumnPair("12/28", 2, 1);
		addColumnPair("12/29", 4, 0.5f);
	}
	
	public void addColumnPair(String label, float topValue, float bottomValue) {
		float biggerValue = Math.max(topValue, bottomValue);
		if(biggerValue > maxValue){
			maxValue = biggerValue;
			onMaxChanged();
		}
		dataColumns.add(new Data(label, topValue, bottomValue));
		
		onDataChanged();
	}
	
	public void clearColumnPairs() {
		dataColumns.clear();
		maxValue = DEFAULT_MAX_VALUE;
		onMaxChanged();
		onDataChanged();
	}
	
	@Override
    protected int getSuggestedMinimumWidth() {
        return 200;
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        return 200;
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
	
	/**
	 * Update the x axis labels according to the maximum
	 */
	private void onMaxChanged() {
		if(showLabels) {
			String strMaxValue = String.format("%.1f", maxValue);
			yLabels[4].text = strMaxValue;
			yLabels[0].text = strMaxValue;
			String strMidValue = String.format("%.1f", maxValue/2.0f);
			yLabels[3].text = strMidValue;
			yLabels[1].text = strMidValue;
			labelTextWidth = sideLabelPaint.measureText(yLabels[2].text);
			labelTextWidth = Math.max(labelTextWidth, sideLabelPaint.measureText(strMaxValue));
			labelTextWidth = Math.max(labelTextWidth, sideLabelPaint.measureText(strMaxValue));
			
			int labelPaddingLeft = getPaddingLeft();
			yLabels[0].x = labelPaddingLeft;
			yLabels[0].y = contentHeight + paddingTop + labelTextSize/2;
			yLabels[1].x = labelPaddingLeft;
			yLabels[1].y = contentHeight * 0.75f + paddingTop + labelTextSize/2;
			yLabels[2].x = labelPaddingLeft;
			yLabels[2].y = contentHeight * 0.5f + paddingTop + labelTextSize/2;
			yLabels[3].x = labelPaddingLeft;
			yLabels[3].y = contentHeight * 0.25f + paddingTop + labelTextSize/2;
			yLabels[4].x = labelPaddingLeft;
			yLabels[4].y = paddingTop + labelTextSize/2;
		}
	}
	
	private void onDataChanged() {
		
		float colWidth = contentWidth / (1.0f + 2.0f * dataColumns.size());
		float bottomTextSize = labelTextSize;
		float bestLabelWidth;
		float maxLabelWidth;
		if(colWidth > 0) {
			do {
				bottomLabelPaint.setTextSize(bottomTextSize);
				bestLabelWidth = colWidth * 2 - bottomLabelPaint.measureText(" ");
				maxLabelWidth = 0;
				for(Data dataPair : dataColumns) {
					float labelWidth = bottomLabelPaint.measureText(dataPair.label.text);
					if(labelWidth > maxLabelWidth) {
						maxLabelWidth = labelWidth;
						if(maxLabelWidth > bestLabelWidth) {
							bottomTextSize--;
							break;
						}
					}
				}
			} while(bottomTextSize > minTextSize && maxLabelWidth > bestLabelWidth);
		}
		
		//store dimensions and coordinates for columns and labels
		float colLeftSide = 0.0f;
		for(Data dataPair : dataColumns) {
			colLeftSide += colWidth;
			
			float topColSize = dataPair.topCol.value / maxValue * contentHeight / 2.0f;
			dataPair.topCol.rect = new Rect((int) colLeftSide, 
								(int) (contentHeight / 2.0f - topColSize), 
								(int) (colLeftSide + colWidth), 
								(int) (contentHeight / 2.0f));
			dataPair.topCol.rect.offset(paddingLeft, paddingTop);
			
			float bottomColSize = dataPair.bottomCol.value / maxValue * contentHeight / 2.0f;
			dataPair.bottomCol.rect = new Rect((int) colLeftSide, 
								(int) (contentHeight / 2.0f), 
								(int) (colLeftSide + colWidth), 
								(int) (contentHeight / 2.0f + bottomColSize));
			dataPair.bottomCol.rect.offset(paddingLeft, paddingTop);
			
			dataPair.label.x = paddingLeft + colLeftSide + colWidth/2 
					- bottomLabelPaint.measureText(dataPair.label.text)/2;
			dataPair.label.y = paddingTop + contentHeight + labelTextSize + 2;
			
			colLeftSide += colWidth;
		}
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		paddingLeft = getPaddingLeft();
		paddingTop = getPaddingTop();
		paddingRight = getPaddingRight();
		paddingBottom = getPaddingBottom();
		
		if(showLabels) {
			paddingLeft += this.labelTextWidth + 2;
			paddingTop += this.labelTextSize/2;
			paddingBottom += this.labelTextSize/2 + labelTextSize + 2;
		}
		
		contentWidth = w - paddingLeft - paddingRight;
		contentHeight = h - paddingTop - paddingBottom;
		
		onMaxChanged();
		
		zeroLine = new Line(0 + paddingLeft, 
							contentHeight / 2.0f + paddingTop, 
							contentWidth + paddingLeft, 
							contentHeight / 2.0f + paddingTop);
				
		onDataChanged();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		canvas.drawLine(zeroLine.startX, zeroLine.startY, zeroLine.endX, zeroLine.endY, linePaint);
		
		Log.w("DrawEvent", "Maximum: " + maxValue);
		
		for(Label label : yLabels) {
			canvas.drawText(label.text, label.x, label.y, sideLabelPaint);
		}
		
		if(dataColumns.size() > 0) {
			for(Data dataPair : dataColumns) {
				Log.w("DrawEvent", "Column drawn: " + dataPair);
				canvas.drawRect(dataPair.topCol.rect, dataTopPaint);
				canvas.drawRect(dataPair.bottomCol.rect, dataBottomPaint);
				canvas.drawText(dataPair.label.text, dataPair.label.x, dataPair.label.y, bottomLabelPaint);
			}
		}
	}

	private class Column {
		public float value;
		public Rect rect;
		
		public Column(float value){
			this.value = value;
		}
		
		@Override
		public String toString(){
			return String.valueOf(value);
		}
	}
	
	private class Label {
		public String text;
		public float x;
		public float y;
		
		public Label(String text) {
			this.text = text;
			this.x = 0;
			this.y = 0;
		}
	}
	
	private class Data {
		public Label label;
		public Column topCol;
		public Column bottomCol;
		
		public Data(String label, float topValue, float bottomValue) {
			this.label = new Label(label);
			this.topCol = new Column(topValue);
			this.bottomCol = new Column(bottomValue);
		}
		
		@Override
		public String toString(){
			return label.text + ": " + topCol + ", " + bottomCol;
		}
	}
	
	private class Line {
		public float startX;
		public float startY;
		public float endX;
		public float endY;
		
		Line(float startX, float startY, float endX, float endY) {
			this.startX = startX;
			this.startY = startY;
			this.endX = endX;
			this.endY = endY;
		}
	}
	
}
