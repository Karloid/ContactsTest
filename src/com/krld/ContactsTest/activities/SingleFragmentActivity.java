package com.krld.ContactsTest.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.krld.ContactsTest.R;

public abstract class SingleFragmentActivity extends ActionBarActivity {

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private String[] menuArray;
	private ActionBarDrawerToggle mDrawerToggle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutResourceId());


		Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
		setSupportActionBar(toolbar);
		setupNavigation(toolbar);


		FragmentManager fm = getFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
		if (fragment == null) {
			fragment = createFragment();
			fm.beginTransaction()
					.add(R.id.fragmentContainer, fragment)
					.commit();
		}

	}

	protected void setupNavigation(Toolbar toolbar) {
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (mDrawerLayout == null) return;
		menuArray = getResources().getStringArray(R.array.menu_array);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, menuArray));
		mDrawerList.setOnItemClickListener(new OnMenuItemClickListener());
		mDrawerToggle = new ActionBarDrawerToggle(this,
				mDrawerLayout, toolbar,
				R.string.drawer_open,
				R.string.drawer_close);
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mDrawerToggle.setDrawerIndicatorEnabled(thisMainActivity());
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}

	private boolean thisMainActivity() {
		return this instanceof ContactListActivity;
	}

	protected int getLayoutResourceId() {
		return R.layout.activity_onepane;
	}

	protected abstract Fragment createFragment();

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {  // ???
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
