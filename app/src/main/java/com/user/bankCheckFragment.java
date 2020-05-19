package com.user;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lu.driver.Common;
import com.lu.driver.CommonTask;
import com.lu.driver.Driver;
import com.lu.driver.R;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class bankCheckFragment extends Fragment {
    private final static String TAG = "TAG_bankCheckFragment";
    private Activity activity;
    private Button btEditDone;
    private EditText etBankAccountName, etBankAccount, etBankCode;
    private Driver driver;
    private int driver_id;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity.setTitle(R.string.textSetBankAccount);
        return inflater.inflate(R.layout.fragment_bank_check, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etBankAccountName = view.findViewById(R.id.etBankAccountName);
        etBankAccount = view.findViewById(R.id.etBankAccount);
        etBankCode = view.findViewById(R.id.etBankCode);
        btEditDone = view.findViewById(R.id.btEditDone);

        SharedPreferences pref = activity.getSharedPreferences(Common.PREF_FILE,
                MODE_PRIVATE);
        driver_id = pref.getInt("driver_id", 0);

        final NavController navController;
        navController = Navigation.findNavController(view);
        Bundle bundle = getArguments();
        if (bundle == null ) {
            Common.showToast(activity, R.string.textNoUserFound);
            navController.popBackStack();
            return;
        }

        if (bundle != null){
            final String driver_bank_name = bundle.getString("BankAccountName");
            final String driver_bank_account = bundle.getString("BankAccount");
            final String driver_bank_code = bundle.getString("BankCode");

            etBankAccountName.setText(driver_bank_name);
            etBankAccount.setText(driver_bank_account);
            etBankCode.setText(driver_bank_code);

        btEditDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String driver_bank_name = etBankAccountName.getText().toString();
                if (driver_bank_name.length() <= 0) {
                    Common.showToast(activity, "不能為空");
                    return;
                }
                String driver_bank_account = etBankAccount.getText().toString();
                if (driver_bank_account.length() <= 0) {
                    Common.showToast(activity, "不能為空");
                    return;
                }
                String driver_bank_code = etBankCode.getText().toString();
                if (driver_bank_code.length() <= 0) {
                    Common.showToast(activity, "不能為空");
                    return;
                }


                if (Common.networkConnected(activity)) {
                    String url = Common.URL_SERVER + "DriverServlet";
                    JsonObject jsonObject = new JsonObject();
                    Driver driver = new Driver(driver_bank_name, driver_bank_account, driver_bank_code, driver_id);
                    jsonObject.addProperty("action", "updateUserBankData");
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
                Navigation.findNavController(v).navigate(R.id.action_bankCheckFragment_to_bankFragment);
            }
        });
    }


}
}
