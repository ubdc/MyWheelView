package com.example.mywheelview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Scroller;
import android.widget.TextView;

public class WheelView <T> extends FrameLayout {
	private static final int ANIM_TIME = 400;
	private static final int DEFAULT_SELECTED_TEXT_COLOR = 0xff33B5E5;
	private static final int DEFAULT_MARGIN_ITEM_COUNT = 1;
	
	private ListView listView;
	private List<T> items = new ArrayList<T>();
	private ItemAdpater adapter;
	private T selectedItem;
	private int itemHeight;
	private int marginItemCount = 3;
	private int selectedTextColor;
	private int unSelectedTextColor;
	private int markerLineColor;
	private float markerLineHeight = 1;
	private float textSize;
	private Paint markerLinePaint;
	private Scroller scroller;
	private Handler h = new Handler();
	private Roll r = new Roll();
	private boolean isIdle = true;
	
	private class Roll implements Runnable {
		boolean finish;
		int position;
		int nextPosition;
		
		@Override
		public void run() {
			if (finish) return;
			if (scroller.computeScrollOffset()) {
				listView.setSelectionFromTop(position, scroller.getCurrY());
				h.postDelayed(this, 16);
			} else {
				listView.setSelectionFromTop(position, scroller.getCurrY());
				if (nextPosition < items.size()) {
					selectedItem = items.get(nextPosition);
				}
				if (onWheelStopListener != null) {
					onWheelStopListener.onWheelStop(nextPosition);
				}
				isIdle = true;
			}
		}
	}
	
	public WheelView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	public WheelView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WheelView(Context context) {
		this(context, null);
	}

	private void init(Context context, AttributeSet attrs, int defStyle) {
		scroller = new Scroller(context, new OvershootInterpolator());
		
		listView = new ListView(context);
		listView.setCacheColorHint(Color.TRANSPARENT);
		listView.setDivider(null);
		listView.setVerticalScrollBarEnabled(false);
		listView.setOnScrollListener(scrollListener);
		listView.setOnItemClickListener(itemClickListener);
		listView.setSelector(android.R.color.transparent);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
			listView.setOverScrollMode(AbsListView.OVER_SCROLL_NEVER);
		}
		listView.setVerticalFadingEdgeEnabled(false);
		adapter = new ItemAdpater(context);
		listView.setAdapter(adapter);
		addView(listView, -1, -1);
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WheelView, defStyle, 0);
		selectedTextColor = a.getColor(R.styleable.WheelView_selectedColor, DEFAULT_SELECTED_TEXT_COLOR);
		unSelectedTextColor = a.getColor(R.styleable.WheelView_unSelectedColor, DEFAULT_SELECTED_TEXT_COLOR);
		markerLineColor = a.getColor(R.styleable.WheelView_markerLineColor, DEFAULT_SELECTED_TEXT_COLOR);
		marginItemCount = a.getInteger(R.styleable.WheelView_marginItemCount, DEFAULT_MARGIN_ITEM_COUNT);
		markerLineHeight = a.getDimension(R.styleable.WheelView_markerLineHeight, markerLineHeight);
		textSize = a.getDimension(R.styleable.WheelView_android_textSize, -1);
		a.recycle();
		
		markerLinePaint = new Paint();
		markerLinePaint.setAntiAlias(true);
		markerLinePaint.setStrokeWidth(markerLineHeight);
		markerLinePaint.setColor(markerLineColor);
		
		TextView measureTxt = new TextView(context);
		measureTxt.setText("H");
		if (textSize > 0) {
			measureTxt.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
		}
		measureTxt.measure(
				MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 1, MeasureSpec.AT_MOST), 
				MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 1, MeasureSpec.AT_MOST));
		itemHeight = (int) (measureTxt.getMeasuredHeight() + dp2px(10) * 2);
	}
	
	private float dp2px(int dp) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getContext().getResources().getDisplayMetrics());
	}
	
	private AbsListView.OnScrollListener scrollListener = new AbsListView.OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
				smoothAlignMarkerLine();
			} else {
				removeRunnable();
				isIdle = false;
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			int childCount = listView.getChildCount();
			for (int i = 0; i < childCount; i++) {
				FrameLayout v = (FrameLayout) listView.getChildAt(i);
				TextView txt = (TextView) v.getChildAt(0);
				int centerLine = (getHeight() - getPaddingTop() - getPaddingBottom()) / 2;
				if (v.getTop() > centerLine
						|| v.getBottom() < centerLine) {
					txt.setTextColor(unSelectedTextColor);
				} else {
					txt.setTextColor(selectedTextColor);
				}
			}
		}
	};
	
	private void removeRunnable() {
		r.finish = true;
		selectedItem = null;
		h.removeCallbacks(r);
	}
	
	private OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
//			listView.smoothScrollToPosition(position - marginItemCount);
		}
	};
	
	private void smoothAlignMarkerLine() {
		if (listView.getChildCount() > 0) {
			View firstVisibleItem = listView.getChildAt(0);
			int top = firstVisibleItem.getTop();
			scroller.abortAnimation();
			r.finish = false;
			r.position = listView.getFirstVisiblePosition();
			if (-top > itemHeight / 2) {
				scroller.startScroll(0, top, 0, -top - itemHeight, ANIM_TIME);
				r.nextPosition = r.position + 1;
			} else {
				scroller.startScroll(0, top, 0, -top, ANIM_TIME);
				r.nextPosition = r.position;
			}
			h.post(r);
		}
	}
	
	public synchronized void setList(List<T> list) {
		removeRunnable();
		isIdle = true;
		items.clear();
		if (list != null && list.size() > 0) {
			items.addAll(list);
		}
		adapter.notifyDataSetChanged();
		listView.setSelection(0);
		if (items.size() > 0) {
			selectedItem = items.get(0);
		} else {
			selectedItem = null;
		}
	}
	
	public synchronized void setList(T[] arr) {
		removeRunnable();
		isIdle = true;
		items.clear();
		if (arr != null && arr.length > 0) {
			items.addAll(Arrays.asList(arr));
		}
		adapter.notifyDataSetChanged();
		listView.setSelection(0);
		if (items.size() > 0) {
			selectedItem = items.get(0);
		} else {
			selectedItem = null;
		}
	}
	
	public synchronized List<T> getList() {
		List<T> list = new ArrayList<T>();
		list.addAll(items);
		return list;
	}
	
	public void setSelection(T t) {
		setSelection(items.indexOf(t));
	}
	
	public void setSelection(int position) {
		listView.setSelection(position);
		if (position < items.size())
			selectedItem = items.get(position);
	}
	
	public int getMarginItemCount() {
		return marginItemCount;
	}
	
	public int getSelectedPosition() {
		return r.nextPosition;
	}
	
	public T getSelectedItem() {
//		int firstVisiblePosition = listView.getFirstVisiblePosition();
//		View v = listView.getChildAt(0);
//		if (v != null) {
//			if (v.getBottom() < itemHeight / 2) {
//				firstVisiblePosition++;
//			}
//		}
//		if (firstVisiblePosition >= 0 && firstVisiblePosition < items.size()) {
//			return items.get(firstVisiblePosition);
//		}
//		return null;
		
		return selectedItem;
	}
	
	public boolean isIdle() {
		return isIdle;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int height = itemHeight * (marginItemCount * 2 + 1);
		
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			getChildAt(i).measure(
					MeasureSpec.makeMeasureSpec(width - getPaddingLeft() - getPaddingRight(), widthMode), 
					MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
		}
		
		setMeasuredDimension(width, height + getPaddingTop() + getPaddingBottom());
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		
		//draw marker line
		float startX = getPaddingLeft();
		float startY = getPaddingTop() + marginItemCount * itemHeight;
		float stopX = getWidth() - getPaddingRight();
		float stopY = startY;
		canvas.drawLine(startX, startY, stopX, stopY, markerLinePaint);
		canvas.drawLine(startX, startY + itemHeight, stopX, stopY + itemHeight, markerLinePaint);
	}
	
	private class ItemAdpater extends BaseAdapter {
		private Context context;
		
		public ItemAdpater(Context context) {
			this.context = context;
		}

		@Override
		public int getCount() {
			return items.size() + marginItemCount * 2;
		}

		@Override
		public T getItem(int position) {
			if (position < marginItemCount) {
				return null;
			} else if (position < items.size() + marginItemCount) {
				return items.get(position - marginItemCount);
			} else {
				return null;
			}
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			FrameLayout container = (FrameLayout) convertView;
			if (container == null) {
				container = new FrameLayout(context);
				container.setLayoutParams(new AbsListView.LayoutParams(-1, itemHeight));
				TextView txt = new TextView(context);
				txt.setSingleLine();
				txt.setEllipsize(TruncateAt.END);
				txt.setPadding((int) dp2px(8), 0, (int) dp2px(8), 0);
				if (position == marginItemCount) {
					txt.setTextColor(unSelectedTextColor);
				} else {
					txt.setTextColor(selectedTextColor);
				}
				if (textSize > 0) {
					txt.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
				}
				FrameLayout.LayoutParams ll = new FrameLayout.LayoutParams(-2, -2);
				ll.gravity = Gravity.CENTER;
				container.addView(txt, ll);
			}
			TextView txt = (TextView) container.getChildAt(0);
			T item = getItem(position);
			if (item != null) {
				txt.setText("" + getItem(position));
			} else {
				txt.setText("");
			}
			return container;
		}
	}
	
	OnWheelStopListener onWheelStopListener;
	
	public interface OnWheelStopListener {
		void onWheelStop(int position);
	}
	
	public void setOnWheelStopListener(OnWheelStopListener listener) {
		this.onWheelStopListener = listener;
	}
}
