package com.krld.ContactsTest;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/**
 * Created by Andrey on 9/28/2014.
 */
public class ContactListActivity extends SingleFragmentActivity implements ContactListFragment.Callbacks {
    @Override
    protected Fragment createFragment() {
        return new ContactListFragment();
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
            if (oldDetail != null) {
                ft.remove(oldDetail);
            }
            ft.add(R.id.detailFragmentContainer, newDetail);
            ft.commit();
        }

    }
}