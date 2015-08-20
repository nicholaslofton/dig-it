package com.example.root.digit;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;

public class MyPagerAdapter extends FragmentPagerAdapter {

    int count;
    String[] login_signup = new String[2];

    public MyPagerAdapter(FragmentManager fm, int size) {
        super(fm);
        count = size;
        login_signup[0] = "Log In";
        login_signup[1] = "Sign Up";
    }

    @Override
    public Fragment getItem (int position) {
        Fragment fragment = new LoginFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("page", position);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getCount() { return count; }

    @Override
    public CharSequence getPageTitle (int position) {
        Locale l = Locale.getDefault();
        return login_signup[position].toUpperCase(l);
    }

    public static class LoginFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            // Inflate the layout resource that'll be returned
            View rootView = inflater.inflate(R.layout.activity_login, container, false);

            // Get the arguments that was supplied when
            // the fragment was instantiated in the
            // CustomPagerAdapter
            Bundle args = getArguments();

            if (args.getInt("page") == 0) {
                ((LoginActivity)getActivity()).mUserNameView = (AutoCompleteTextView) rootView.findViewById(R.id.username);

                ((LoginActivity)getActivity()).mPasswordView = (EditText) rootView.findViewById(R.id.password);

                ((LoginActivity)getActivity()).mLoginFormView = rootView.findViewById(R.id.login_form);
                ((LoginActivity)getActivity()).mProgressView = rootView.findViewById(R.id.login_progress);
                ((LoginActivity)getActivity()).mEmailLoginFormView = rootView.findViewById(R.id.email_login_form);

                // Set up the login form.
                ((LoginActivity)getActivity()).mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                        if (id == R.id.login || id == EditorInfo.IME_NULL) {
                            Activity activity = getActivity();
                            if(activity instanceof LoginActivity){
                                LoginActivity myactivity = (LoginActivity) activity;
                                myactivity.attemptLogin();
                            }
                            return true;
                        }
                        return false;
                    }
                });

                Button mEmailSignInButton = (Button) rootView.findViewById(R.id.email_sign_in_button);
                mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Activity activity = getActivity();
                        if(activity instanceof LoginActivity){
                            LoginActivity myactivity = (LoginActivity) activity;
                            myactivity.attemptLogin();
                        }
                    }
                });

            }
            else {
                rootView = inflater.inflate(R.layout.activity_sign_up, container, false);

                // Set up the login form.
                ((LoginActivity)getActivity()).mUserNameViewSignUp = (AutoCompleteTextView) rootView.findViewById(R.id.username_sign_up);
                ((LoginActivity)getActivity()).mEmailViewSignUp = (AutoCompleteTextView) rootView.findViewById(R.id.email_sign_up);
                ((LoginActivity)getActivity()).mPasswordViewSignUp = (EditText) rootView.findViewById(R.id.password_sign_up);

                ((LoginActivity)getActivity()).mLoginFormViewSignUp = rootView.findViewById(R.id.login_form_sign_up);
                ((LoginActivity)getActivity()).mProgressViewSignUp = rootView.findViewById(R.id.login_progress_sign_up);
                ((LoginActivity)getActivity()).mEmailLoginFormViewSignUp = rootView.findViewById(R.id.email_sign_up_form);

                Button mEmailSignUpButton = (Button) rootView.findViewById(R.id.email_sign_up_button);
                mEmailSignUpButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Activity activity = getActivity();
                        if(activity instanceof LoginActivity){
                            LoginActivity myactivity = (LoginActivity) activity;
                            myactivity.attemptSignUp();
                        }
                    }
                });
            }

            return rootView;
        }
    }
}
