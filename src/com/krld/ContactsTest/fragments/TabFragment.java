package com.krld.ContactsTest.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.krld.ContactsTest.R;
import com.krld.ContactsTest.test.SlidingTabLayout;

public class TabFragment extends Fragment {
	private ViewPager mViewPager;
	private SlidingTabLayout mSlidingTab;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.tab_view, container, false);
		return v;
	}


	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
		mViewPager.setAdapter(new SamplePagerAdapter());

		mSlidingTab = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
		mSlidingTab.setViewPager(mViewPager);
	}

	private class SamplePagerAdapter extends PagerAdapter {
		@Override
		public int getCount() {
			return 10;
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
			TextView textView = (TextView)view.findViewById(android.R.id.text1);
			textView.setText("Tab " + position);
			return view;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}
	}
}
