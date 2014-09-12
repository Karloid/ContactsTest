package com.krld.ContactsTest;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.example.ContactsTest.R;

public class LoginActivity extends Activity {
    private View loginButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        hideActionBar();
        initLoginButton();
    }

    private void initLoginButton() {
        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tryLogin();
            }
        });
    }

    private void tryLogin() {
        startContactListActivity();
    }

    private void startContactListActivity() {
        Intent intent = new Intent(this, ContactListActivity.class);
        startActivity(intent);
    }

    private void hideActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.hide();
    }
}
