package com.krld.ContactsTest.fragments;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.krld.ContactsTest.R;
import com.krld.ContactsTest.activities.ContactListActivity;

public class LoginFragment extends Fragment {
    private View loginButton;
    private EditText userNameEdit;
    private EditText passwordEdit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.login, container, false);
        initViews(v);
        return v;
    }


    private void initViews(View v) {
        userNameEdit = (EditText) v.findViewById(R.id.username_edit);
        userNameEdit.addTextChangedListener(new TextChangedListener());
        passwordEdit = (EditText) v.findViewById(R.id.password_edit);
        passwordEdit.addTextChangedListener(new TextChangedListener());
        loginButton = v.findViewById(R.id.loginButton);
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
        Intent intent = new Intent(getActivity(), ContactListActivity.class);
        startActivity(intent);
    }

    private void updateLoginButtonState() {
        loginButton.setEnabled(true || !userNameEdit.getText().toString().isEmpty() && !passwordEdit.getText().toString().isEmpty());
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
