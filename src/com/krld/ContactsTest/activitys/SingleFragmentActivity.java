package com.krld.ContactsTest.activitys;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.krld.ContactsTest.R;

public abstract class SingleFragmentActivity extends Activity {

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private String[] menuArray;
	private ActionBarDrawerToggle mDrawerToggle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutResourceId());
		FragmentManager fm = getFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
		if (fragment == null) {
			fragment = createFragment();
			fm.beginTransaction()
					.add(R.id.fragmentContainer, fragment)
					.commit();
		}
		setupNavigation();
	}

	protected void setupNavigation() {
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (mDrawerLayout == null) return;
		menuArray = getResources().getStringArray(R.array.menu_array);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, menuArray));
		mDrawerList.setOnItemClickListener(new OnMenuItemClickListener());
		mDrawerToggle = new ActionBarDrawerToggle(this,
				mDrawerLayout,
				R.drawable.ic_drawer,
				R.string.drawer_open,
				R.string.drawer_close);
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
	}

	protected int getLayoutResourceId() {
		return R.layout.activity_onepane;
	}

	protected abstract Fragment createFragment();

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle your other action bar items...

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		if (mDrawerToggle != null)
			mDrawerToggle.syncState();
	}

	private class OnMenuItemClickListener implements android.widget.AdapterView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectMenuItem(position);
		}
	}

	private void selectMenuItem(int position) {
		mDrawerList.setItemChecked(position, true);
		Toast.makeText(this, menuArray[position], Toast.LENGTH_SHORT).show();
		if (position == 4) {
			startTabActivity();
		} else if (position == 5) {
			startContactListActivity();
		}
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	private void startContactListActivity() {
		Intent intent = new Intent(this, ContactListActivity.class);
		startActivity(intent);
	}

	private void startTabActivity() {
		Intent intent = new Intent(this, TabActivity.class);
		startActivity(intent);
	}
}
