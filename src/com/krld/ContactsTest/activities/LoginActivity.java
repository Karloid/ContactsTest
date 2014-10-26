package com.krld.ContactsTest.activities;

import android.app.Fragment;
import android.os.Bundle;
import com.krld.ContactsTest.fragments.LoginFragment;
import com.krld.ContactsTest.R;

/**
 * Created by Andrey on 9/28/2014.
 */
public class LoginActivity extends SingleFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_onepane_no_nav;
    }

    @Override
    protected Fragment createFragment() {
        return new LoginFragment();
    }
}