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
public class bankFragment extends Fragment {
    private final static String TAG = "TAG_bankFragment";
    private Activity activity;
    private Button btEdit;
    private EditText etBankAccountName, etBankAccount, etBankCode;
    private int driver_id;
    private Driver driver;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        activity.setTitle(R.string.textSetBankAccount);
        return inflater.inflate(R.layout.fragment_bank, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etBankAccountName = view.findViewById(R.id.etBankAccountName);
        etBankAccount = view.findViewById(R.id.etBankAccount);
        etBankCode = view.findViewById(R.id.etBankCode);
        btEdit = view.findViewById(R.id.btEdit);






        SharedPreferences pref = activity.getSharedPreferences(Common.PREF_FILE,
                MODE_PRIVATE);
        driver_id = pref.getInt("driver_id", 0);


        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "DriverServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getBankInformation");
            jsonObject.addProperty("driver_id", driver_id);
            try {
                String jsonIn = new CommonTask(url, jsonObject.toString()).execute().get();
                driver = new Gson().fromJson(jsonIn, Driver.class);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);


        }

            etBankAccountName.setText(driver.getDriver_bank_name());
            etBankAccount.setText(driver.getDriver_bank_account());
            etBankCode.setText(driver.getDriver_bank_code());


        btEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                String BankAccountName = etBankAccountName.getText().toString().trim();
                String BankAccount = etBankAccount.getText().toString().trim();
                String BankCode = etBankCode.getText().toString().trim();

                bundle.putString("BankAccountName", BankAccountName);
                bundle.putString("BankAccount", BankAccount);
                bundle.putString("BankCode", BankCode);
                Navigation.findNavController(v).navigate(R.id.action_bankFragment_to_bankCheckFragment, bundle);

            }
        });

    }


}
