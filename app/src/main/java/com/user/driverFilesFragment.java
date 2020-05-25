package com.user;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lu.driver.Common;
import com.lu.driver.CommonTask;
import com.lu.driver.Driver;
import com.lu.driver.R;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


public class driverFilesFragment extends Fragment {
    private String TAG = "TAG_driverFilesFragment";
    private FragmentActivity activity;
    private TextView tvIdStatus, tvLicenceStatus, tvInsuranceStatus, tvExpireDate;
    private CommonTask statusTask;
    private int driver_id;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        SharedPreferences pref = activity.getSharedPreferences(Common.PREF_FILE,
                MODE_PRIVATE);
        driver_id = pref.getInt("driver_id", 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity.setTitle(R.string.textData);
        return inflater.inflate(R.layout.fragment_driver_files, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvIdStatus = view.findViewById(R.id.tvIdStatus);
        tvLicenceStatus = view.findViewById(R.id.tvLicenceStatus);
        tvInsuranceStatus = view.findViewById(R.id.tvInsuranceStatus);
        tvExpireDate = view.findViewById(R.id.tvExpireDate);

        String[] status = getStatus();
        tvIdStatus.setText(status[0]);
        tvLicenceStatus.setText(status[1]);
        tvInsuranceStatus.setText(status[2]);
        tvExpireDate.setText(status[3]);

        Button btIdEdit = view.findViewById(R.id.btIdEdit);
        btIdEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.action_driverFilesFragment_to_idUpdateFragment);
            }
        });
        Button btLicenceEdit = view.findViewById(R.id.btLicenceEdit);
        btLicenceEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.action_driverFilesFragment_to_driverLicenceUpdateFragment);
            }
        });
        Button btInsuranceEdit = view.findViewById(R.id.btInsuranceEdit);
        btInsuranceEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.action_driverFilesFragment_to_driverInsuranceUpdateFragment);
            }
        });

    }

    private String[] getStatus() {
        String[] text = {"idStatusIsValid","licenceStatusIsValid","insuranceStatusIsValid", "expireDateIsValid"};
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "DriverServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getStatus");
            jsonObject.addProperty("driver_id", driver_id);
            String jsonOut = jsonObject.toString();
            statusTask = new CommonTask(url, jsonOut);
            try{
                String jsonIn = statusTask.execute().get();
                JsonArray statusJson = new Gson().fromJson(jsonIn, JsonArray.class);
                text[0] = statusInChinese(statusJson.get(0).getAsString());
                text[1] = statusInChinese(statusJson.get(1).getAsString());
                text[2] = statusInChinese(statusJson.get(2).getAsString());
                text[3] = statusJson.get(3).getAsString();
            }catch (Exception e){
                Log.e (TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return text;
    }

    private String statusInChinese(String name) {
        String text = "";
        if (name.isEmpty() || name.equals("unfinished")){
            text = "尚未認證";
        } else if(name.equals("success")) {
            text = "認證成功";
        } else if(name.equals("processing")) {
            text = "認證中";
        } else if(name.equals("expired")) {
            text = "認證過期";
        } else if(name.equals("failed")) {
            text = "認證失敗";
        }

        return text;
    }
}
