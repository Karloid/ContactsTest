package com.krld.ContactsTest;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity {
    private View loginButton;
    private EditText userNameEdit;
    private EditText passwordEdit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        hideActionBar();
        initViews();
    }

    private void initViews() {
        userNameEdit = (EditText) findViewById(R.id.username_edit);
        userNameEdit.addTextChangedListener(new TextChangedListener());
        passwordEdit = (EditText) findViewById(R.id.password_edit);
        passwordEdit.addTextChangedListener(new TextChangedListener());
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

    private void updateLoginButtonState() {
        loginButton.setEnabled(!userNameEdit.getText().toString().isEmpty() && !passwordEdit.getText().toString().isEmpty());
    }

    private class TextChangedListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            updateLoginButtonState();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }


}
