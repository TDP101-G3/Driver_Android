package com.yu.driver;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.lu.driver.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class SignUpBankAccount extends Fragment {
    private final static String TAG = "TAG_SignUp";
    private Activity activity;
    private Button btContinue;
    private EditText etBankAccountName, etBankAccount, etBankCode;
    private byte[] image;
    private ImageView imageView;


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
        return inflater.inflate(R.layout.fragment_sign_up_bank_account, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etBankAccountName = view.findViewById(R.id.etBankAccountName);
        etBankAccount = view.findViewById(R.id.etBankAccount);
        etBankCode = view.findViewById(R.id.etBankCode);
        btContinue = view.findViewById(R.id.btContinue);

        Bundle sign1 = getArguments();
        if (sign1 != null) {
            final String driver_name = sign1.getString("name");
            final String driver_password = sign1.getString("password");
            final String driver_email = sign1.getString("email");
            final String driver_phone = sign1.getString("phoneNumber");
//            String text1 = "User name: " + driver_name + "; password: " + driver_password + "\n";
//            tvResult.append(text1);

            btContinue.setOnClickListener(new View.OnClickListener() {
                                              @Override
                                              public void onClick(View v) {
                                                  Bundle sign2 = new Bundle();
                                                  String driver_name_2 = driver_name.trim();
                                                  String driver_password_2 = driver_password.trim();
                                                  String driver_email_2 = driver_email.trim();
                                                  String driver_phone_2 = driver_phone.trim();

                                                  String driver_bank_name_2 = etBankAccountName.getText().toString().trim();
                                                  String driver_bank_account_2 = etBankAccount.getText().toString().trim();
                                                  String driver_bank_code_2 = etBankCode.getText().toString().trim();
                                                  if (driver_bank_name_2.length() <= 0) {
                                                      etBankAccountName.setError("帳戶名稱不能為空");
                                                  }
                                                  if (driver_bank_account_2.length() <= 0) {
                                                      etBankAccount.setError("銀行帳戶不能為空");
                                                  }
                                                  if (driver_bank_code_2.length() <= 0) {
                                                      etBankCode.setError("銀行代碼不能為空");
                                                  }
                                                  if (driver_bank_name_2.isEmpty() || driver_bank_account_2.isEmpty() || driver_bank_code_2.isEmpty()) {
                                                      return;
                                                  }


                                                  sign2.putString("name", driver_name_2);
                                                  sign2.putString("password", driver_password_2);
                                                  sign2.putString("email", driver_email_2);
                                                  sign2.putString("phoneNumber", driver_phone_2);
                                                  sign2.putString("bankAccountName", driver_bank_name_2);
                                                  sign2.putString("bankAccount", driver_bank_account_2);
                                                  sign2.putString("bankCode", driver_bank_code_2);

                                                  Navigation.findNavController(v).navigate(R.id.action_signUpBankAccount_to_signUp_2, sign2);
                                              }

                                          }

            );
        }
    }

}



