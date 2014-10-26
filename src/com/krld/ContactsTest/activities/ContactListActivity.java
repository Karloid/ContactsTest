package com.krld.ContactsTest.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import com.krld.ContactsTest.fragments.ContactDetailFragment;
import com.krld.ContactsTest.fragments.ContactListFragment;
import com.krld.ContactsTest.R;

/**
 * Created by Andrey on 9/28/2014.
 */
public class ContactListActivity extends SingleFragmentActivity implements ContactListFragment.Callbacks {
    @Override
    protected Fragment createFragment() {
		Fragment f = ContactListFragment.newInstance();
		return f;
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_masterdetail;
    }

    @Override
    public void onContactSelected(Uri uri) {
        if (findViewById(R.id.detailFragmentContainer) == null) {
            Intent intent = new Intent(this, ContactDetailActivity.class);
            intent.setData(uri);
            startActivity(intent);
        } else {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            Fragment oldDetail = fm.findFragmentById(R.id.detailFragmentContainer);
            Fragment newDetail = ContactDetailFragment.newInstance(uri);
			//ft.setCustomAnimations(R.anim.slide_left, R.anim.slide_out_rigth);
			ft.setCustomAnimations(R.anim.fade_out, R.anim.fade_in);
			//ft.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
            if (oldDetail != null) {
                ft.remove(oldDetail);
            }
            ft.replace(R.id.detailFragmentContainer, newDetail);
            ft.commit();
        }

    }

	@Override
	public boolean isMultiPaneActivity() {
		return findViewById(R.id.detailFragmentContainer) != null;
	}
}