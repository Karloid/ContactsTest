package com.krld.ContactsTest;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

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