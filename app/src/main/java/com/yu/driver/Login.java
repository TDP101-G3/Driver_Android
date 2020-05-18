package com.yu.driver;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lu.driver.Common;
import com.lu.driver.CommonTask;
import com.lu.driver.R;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class Login extends Fragment {
    private Activity activity;
    private EditText etEmail, etPassword;
    private TextView tvForgetPassword, tvSignUp;
    private Button btLogin;
    private CommonTask commonTask, getUserInfoTask;
    private final static String TAG = "Login";
    private TextInputLayout tilEmail, tilPassword;
    private static int driver_id = 0;
    private static String driver_name = null;
    private SharedPreferences preferences;
    private final static String PREFERENCES_NAME = "preferences";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity.setTitle(R.string.textLogin);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    //  判斷是否有偏好設定
    @Override
    public void onStart() {
        super.onStart();
        preferences = activity.getSharedPreferences(Common.PREF_FILE, MODE_PRIVATE);
        driver_id = preferences.getInt("driver_id", driver_id);
        driver_name = preferences.getString("driver_name", driver_name);
        if (driver_id != 0){
            Common.showToast(activity, "歡迎回來"+driver_name);
            Navigation.findNavController(getView()).navigate(R.id.action_login_to_mainFragment);

        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btLogin = view.findViewById(R.id.btLogin);
        tilEmail = view.findViewById(R.id.tilEmail);
        tilPassword = view.findViewById(R.id.tilPassword);
        tvForgetPassword = view.findViewById(R.id.tvForgetPassword);
        tvSignUp = view.findViewById(R.id.tvSignUp);

        preferences = activity.getSharedPreferences(Common.PREF_FILE, MODE_PRIVATE);   //取得偏好設定檔


        tvForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_login_to_forgetPassword);

            }
        });

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_login_to_signUp);

            }
        });


        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object email = etEmail.getText();
                Object password = etPassword.getText();
                String Email = email.toString().trim();
                String Password = password.toString().trim();
                if (networkConnected()) {
                    String url = Common.URL_SERVER + "DriverServlet";//連伺服器
                    JsonObject jsonObject = new JsonObject();   //建一個物件
                    jsonObject.addProperty("action", "loginCheck");
                    jsonObject.addProperty("email", Email);//幫key加屬性，送到伺服器
                    jsonObject.addProperty("password", Password);
                    commonTask = new CommonTask(url, jsonObject.toString());//給伺服器位置，為傳送資料做準備
                    int loginResult = 0;

                    try {
                        String Login = commonTask.execute().get();//取得資料，啟動，呼叫execute，doinbackground會執行，呼叫get等開啟的新執行緒doinbackground，抓完資料回傳回來
//                        JsonObject jObject = new Gson().fromJson(Login, JsonObject.class);// Json 字串，直接轉成該類別的物件
                        loginResult = Integer.valueOf(Login);
                    } catch (Exception e) {
                        Log.e(TAG, "getString" + e.toString());
                    }

                    if (loginResult == 0 || Email.isEmpty()) {
                        tilEmail.setError("帳號輸入錯誤或帳號不存在");
                    } else if (loginResult == 2 || Password.isEmpty()) {
                        tilPassword.setError("密碼輸入錯誤");
                    } else if (loginResult == 1) {
//                      JsonObject jsonFindId = new JsonObject();   //建一個物件
//                      jsonFindId.addProperty("action", "findId");
//                      jsonFindId.addProperty("email", Email);//幫key加屬性，送到伺服器
                        JsonObject jsonGetUserInfo = new JsonObject();   //建一個物件
                        jsonGetUserInfo.addProperty("action", "getUserInfo");
                        jsonGetUserInfo.addProperty("email", Email);//幫key加屬性，送到伺服器
                        String jsonOut = jsonGetUserInfo.toString();
                        getUserInfoTask = new CommonTask(url, jsonOut);

                        try {
//                            CommonTask findIDTask = new CommonTask(url, jsonFindId.toString());
//                            String inputDriverId = findIDTask.execute().get();
//                            driver_id = Integer.valueOf(inputDriverId);
                            String jsonIn = getUserInfoTask.execute().get();
                            JsonObject jsonResult = new Gson().fromJson(jsonIn, JsonObject.class);
                            driver_id = jsonResult.get("driver_id").getAsInt();
                            driver_name = jsonResult.get("driver_name").getAsString();
//                            driver_id = Integer.valueOf(inputDriverId);
//                            driver_name = String.valueOf(inputDriverName);


                            savePreferences();
                        } catch (Exception e) {
                            Log.e(TAG, "getString" + e.toString());
                        }
                        Common.showToast(activity, "登入成功");
                        Navigation.findNavController(v).navigate(R.id.action_login_to_mainFragment);
                    } else {
                        Common.showToast(activity, "系統錯誤");

                    }
                }
            }
        });

    }


    private void savePreferences() {
        preferences.edit()   //開啟編輯狀態，編輯的東西暫存到記憶體裡
                .putInt("driver_id", driver_id)
                .putString("driver_name", driver_name)
                .apply();   //真的把記憶體的東西存到檔案裡
        Common.showToast(activity, "歡迎回來"+driver_name);
    }

//    private void loadPreferences() {
//        preferences = activity.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);   //取得偏好設定檔，偏好設定檔可以讀多個
//        loadPreferences();
//        int driver_id = preferences.getInt("driver_id", driver_id);
//    }

    //檢察網路狀態
    private boolean networkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return networkInfo != null && networkInfo.isConnected();
    }
}
