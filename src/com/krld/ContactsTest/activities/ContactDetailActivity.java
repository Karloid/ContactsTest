package com.krld.ContactsTest.activities;

import android.app.Fragment;
import android.os.Bundle;
import com.krld.ContactsTest.fragments.ContactDetailFragment;

public class ContactDetailActivity extends SingleFragmentActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected Fragment createFragment() {
        return ContactDetailFragment.newInstance(getIntent().getData());
    }
}