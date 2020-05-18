package com.user;

import android.app.Activity;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lu.driver.Common;
import com.lu.driver.CommonTask;
import com.lu.driver.Driver;
import com.lu.driver.ImageTask;
import com.lu.driver.R;


public class userFragment extends Fragment {
    private String TAG = "TAG_userFragment";
    private Activity activity;
    private TextView tvUserName, tvPhone, tvEmail;
    private ImageView ivUserPhoto;
    private CommonTask carFindByIdTask;
    private ImageTask driverImageTask;
    private Driver driver;
    private int imageSize;
    private int driver_id = 1;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity.setTitle(R.string.textData);
        return inflater.inflate(R.layout.fragment_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvUserName = view.findViewById(R.id.tvUserName);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvEmail = view.findViewById(R.id.tvEmail);
        ivUserPhoto = view.findViewById(R.id.ivUserPhoto);
        driver = findById(driver_id);
        setDriver(driver);
        setDriverPhoto(driver_id);

        Button btUser = view.findViewById(R.id.btUser);
        btUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("driver", driver);
                Navigation.findNavController(view).navigate(R.id.action_userFragment_to_userEditFragment, bundle);
            }
        });

        ivUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                Navigation.findNavController(view).navigate(R.id.action_userFragment_to_userPhotoUpdateFragment, bundle);
            }
        });

        Button btCarAssurance = view.findViewById(R.id.btCarInsurance);
        btCarAssurance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.action_userFragment_to_driverFilesFragment);
            }
        });
    }

    private void setDriverPhoto(int driver_id) {
        String url = Common.URL_SERVER + "DriverServlet";
        imageSize = getResources().getDisplayMetrics().widthPixels / 4;
        driverImageTask = new ImageTask(url, driver_id, imageSize, ivUserPhoto);
        driverImageTask.execute();
    }

    private Driver findById(int driver_id) {
        Driver driver = null;
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "DriverServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "findUserById");
            jsonObject.addProperty("driver_id", driver_id);
            String jsonOut = jsonObject.toString();
            carFindByIdTask = new CommonTask(url, jsonOut);
            try{
                String jsonIn = carFindByIdTask.execute().get();
                driver = new Gson().fromJson(jsonIn, Driver.class);
            }catch (Exception e){
                Log.e (TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return driver;
    }
    private void setDriver(Driver driver){
        tvUserName.setText(String.valueOf(driver.getDriver_name()));
        tvPhone.setText(String.valueOf(driver.getDriver_phone()));
        tvEmail.setText(String.valueOf(driver.getDriver_email()));
    }
}
