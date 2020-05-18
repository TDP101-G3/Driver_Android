package com.user;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lu.driver.Common;
import com.lu.driver.CommonTask;
import com.lu.driver.Driver;
import com.lu.driver.ImageTask;
import com.lu.driver.R;

public class userEditFragment extends Fragment {
    private String TAG = "TAG_userEditFragment";
    private FragmentActivity activity;
    private EditText etName, etPhone, etEmail;
    private Driver driver;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity.setTitle(R.string.textUserEdit);
        return inflater.inflate(R.layout.fragment_user_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etName = view.findViewById(R.id.etName);
        etPhone = view.findViewById(R.id.etPhone);
        etEmail = view.findViewById(R.id.etEmail);

        final NavController navController;
        navController = Navigation.findNavController(view);
        Bundle bundle = getArguments();
        if (bundle == null || bundle.getSerializable("driver") == null) {
            Common.showToast(activity, R.string.textNoUserFound);
            navController.popBackStack();
            return;
        }
        driver = (Driver) bundle.getSerializable("driver");
        showDriver();

        Button btDone = view.findViewById(R.id.btDone);
        btDone.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString();
                if (name.length() <= 0) {
                    Common.showToast(activity, R.string.textNameIsInvalid);
                    return;
                }
                String phone = etPhone.getText().toString();
                if (phone.length() <= 0) {
                    Common.showToast(activity, R.string.textPhoneIsInvalid);
                    return;
                }
                String email = etEmail.getText().toString();
                if (email.length() <= 0) {
                    Common.showToast(activity, R.string.textEmailIsInvalid);
                    return;
                }
                driver = driver.updateDriver(name, phone, email);

                if (Common.networkConnected(activity)) {
                    String url = Common.URL_SERVER + "DriverServlet";
                    JsonObject jsonObject = new JsonObject();

                    jsonObject.addProperty("action", "updateUserData");
                    jsonObject.addProperty("driver", new Gson().toJson(driver));
                    int count = 0;
                    try {
                        String result = new CommonTask(url, jsonObject.toString()).execute().get();
                        count = Integer.valueOf(result);
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                    if (count == 0) {
                        Common.showToast(activity, R.string.textUpdateFail);
                    } else {
                        Common.showToast(activity, R.string.textUpdateSuccess);
                    }
                } else {
                    Common.showToast(activity, R.string.textNoNetwork);
                }
                /* 回前一個Fragment */
                navController.popBackStack();
            }
        });
    }

    private void showDriver() {
        etName.setText(driver.getDriver_name());
        etPhone.setText(driver.getDriver_phone());
        etEmail.setText(driver.getDriver_email());
    }
}
