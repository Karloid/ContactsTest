package com.krld.ContactsTest.fragments;

import android.animation.ValueAnimator;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.krld.ContactsTest.R;
import com.krld.ContactsTest.test.SlidingTabLayout;
import com.krld.ContactsTest.test.SlidingTabStrip;

import java.util.ArrayList;
import java.util.List;

public class TabFragment extends Fragment {
	private ViewPager mViewPager;
	private SlidingTabLayout mSlidingTab;
	private List<ColorTab> mTabs;
	private SamplePagerAdapter mAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.tab_view, container, false);
		return v;
	}


	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		initDefaultsTabs();
		mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
		mAdapter = new SamplePagerAdapter();
		mViewPager.setAdapter(mAdapter);

		mSlidingTab = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
		mSlidingTab.setViewPager(mViewPager);
		mSlidingTab.setCustomTabColorizer(new RandomTabColorizer());
	}

	private void initDefaultsTabs() {
		mTabs = new ArrayList<ColorTab>();
		int tabCount = 100;
		for (int i = 0; i < tabCount; i++) {
			ColorTab colorTab = new ColorTab(i);
			mTabs.add(colorTab);
		}
	}

	private class SamplePagerAdapter extends PagerAdapter {
		@Override
		public int getCount() {
			return mTabs.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object o) {
			return o == view;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return "Title " + position;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View view = getActivity().getLayoutInflater().inflate(R.layout.pager_item, container, false);
			container.addView(view);

			ColorTab colorTab = mTabs.get(position);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.textView = (TextView) view.findViewById(android.R.id.text1);
			viewHolder.background = (LinearLayout) view.findViewById(android.R.id.background);
			viewHolder.swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(android.R.id.content);
			colorTab.setViewHolder(viewHolder);
			colorTab.fillViewContent();
			return view;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		private class ViewHolder {
			public TextView textView;
			public LinearLayout background;
			public SwipeRefreshLayout swipeRefreshLayout;
		}
	}

	private class RandomTabColorizer implements SlidingTabLayout.TabColorizer {
		@Override
		public int getIndicatorColor(int position) {

			return mTabs.get(position).getColor();
		}

		@Override
		public int getDividerColor(int position) {
			return mTabs.get(position).getDividerColor();
		}
	}

	private class ColorTab {
		private int color;
		private int dividerColor;
		private String text;
		private SamplePagerAdapter.ViewHolder viewHolder;
		private int oldColor;

		public ColorTab(int i) {
			text = "Tab " + i;
			refreshColors();
		}

		public void setColor(int color) {
			this.color = color;
		}

		public int getColor() {
			return color;
		}

		public void setDividerColor(int dividerColor) {
			this.dividerColor = dividerColor;
		}

		public int getDividerColor() {
			return dividerColor;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public void refreshColors() {
			setOldColor(getColor());
			setColor(Color.rgb((int) (Math.random() * 255f), (int) (Math.random() * 255f), (int) (Math.random() * 255f)));
			setDividerColor(Color.rgb((int) (Math.random() * 255f), (int) (Math.random() * 255f), (int) (Math.random() * 255f)));
		}

		public void setViewHolder(SamplePagerAdapter.ViewHolder viewHolder) {
			this.viewHolder = viewHolder;
		}

		public SamplePagerAdapter.ViewHolder getViewHolder() {
			return viewHolder;
		}

		public void fillViewContent() {
			viewHolder.textView.setText(getText());
			viewHolder.background.setBackgroundColor(getColor());
			viewHolder.swipeRefreshLayout.setOnRefreshListener(new OnRefreshTabListener(viewHolder.swipeRefreshLayout));
		}

		public void updateBackgroundColor() {
			final float[] from = new float[3],
					to =   new float[3];

			Color.colorToHSV(getOldColor(), from);
			Color.colorToHSV(getColor(), to);

			ValueAnimator anim = ValueAnimator.ofFloat(0, 1);   // animate from 0 to 1
			anim.setDuration(300);                              // for 300 ms

			final float[] hsv  = new float[3];                  // transition color
			anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
				@Override public void onAnimationUpdate(ValueAnimator animation) {
					// Transition along each axis of HSV (hue, saturation, value)
					hsv[0] = from[0] + (to[0] - from[0])*animation.getAnimatedFraction();
					hsv[1] = from[1] + (to[1] - from[1])*animation.getAnimatedFraction();
					hsv[2] = from[2] + (to[2] - from[2])*animation.getAnimatedFraction();

					viewHolder.background.setBackgroundColor(Color.HSVToColor(hsv));
				}
			});

			anim.start();                                        // start animation
		}

		public void setOldColor(int oldColor) {
			this.oldColor = oldColor;
		}

		public int getOldColor() {
			return oldColor;
		}
	}

	private class OnRefreshTabListener implements SwipeRefreshLayout.OnRefreshListener {
		private final SwipeRefreshLayout swipeRefreshLayout;

		public OnRefreshTabListener(SwipeRefreshLayout swipeRefreshLayout) {
			this.swipeRefreshLayout = swipeRefreshLayout;
		}

		@Override
		public void onRefresh() {
			int currentItem = mViewPager.getCurrentItem();

			ColorTab colorTab = mTabs.get(currentItem);
			colorTab.refreshColors();
			colorTab.updateBackgroundColor();
			//swipeRefreshLayout.requestLayout();
			SlidingTabStrip tabStrip = mSlidingTab.getTabStrip();
			tabStrip.invalidate();
			swipeRefreshLayout.setRefreshing(false);
		}
	}
}
