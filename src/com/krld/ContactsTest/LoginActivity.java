package com.krld.ContactsTest;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

/**
 * Created by Andrey on 9/28/2014.
 */
public class LoginActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new LoginFragment();
    }
}