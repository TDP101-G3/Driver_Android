package com.lu.driver;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import static android.content.Context.MODE_PRIVATE;
import static androidx.navigation.Navigation.findNavController;
import static com.lu.driver.CommonTwo.chatWebSocketClient;


public class MainFragment extends Fragment {
    private static final String TAG = "TAG_MainFragment";
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private static final int REQ_CHECK_SETTINGS = 1;
    private static final int PER_ACCESS_LOCATION = 0;
    private ToggleButton tbtStatus;
    private Activity activity;
    private MapView mapView;
    private GoogleMap map;
    private Driver driver;
    private int driver_status = 0;
    private int driver_id;
    private LocalBroadcastManager broadcastManager;
    private NavController navController;
    private String user;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        SharedPreferences pref = activity.getSharedPreferences(Common.PREF_FILE,
                MODE_PRIVATE);
        driver_id = pref.getInt("driver_id", 0);
        user = "driver"+driver_id;
        CommonTwo.saveUserName(activity,user);
        broadcastManager = LocalBroadcastManager.getInstance(activity);
        CommonTwo.connectServer(activity, CommonTwo.loadUserName(activity));
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000) //1秒 單位：ms
                .setSmallestDisplacement(5); //5公尺 單位：m
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                lastLocation = locationResult.getLastLocation();
                updateLastLocationInfo(lastLocation);
            }
        };

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 初始化LocalBroadcastManager並註冊BroadcastReceiver
        // Inflate the layout for this fragment
        activity.setTitle(R.string.textMain);
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        registerChatReceiver();
        navController = findNavController(view);
        checkLocationSettings();
        mapView = view.findViewById(R.id.mapView);
        driver = new Driver(driver_id);
        // 在Fragment生命週期方法內呼叫對應的MapView方法
        mapView.onCreate(savedInstanceState);
        mapView.onStart();
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                showMyLocation();
            }
        });
        //registerChatReceiver();
        tbtStatus = view.findViewById(R.id.tbtStatus);
        tbtStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if (fusedLocationClient == null) {
                        showLastLocation();
                    }
                    driver_status = 1;
                    if (Common.networkConnected(activity)) {
                        String url = Common.URL_SERVER + "DriverServlet";
                        driver.setStatus(driver_status);
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("action", "driverUpdate");
                        jsonObject.addProperty("driver", new Gson().toJson(driver));
                        int count = 0;
                        try {
                            String result = new CommonTask(url, jsonObject.toString()).execute().get();
                            count = Integer.parseInt(result);
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                        if (count == 0) {
                            Common.showToast(activity, R.string.textUpdateFail);
                        } else {
                            Common.showToast(activity, R.string.textOnline);
                        }
                    } else {
                        Common.showToast(activity, R.string.textNoNetwork);
                    }
                }
                else{
                    driver_status = 0;
                    if (Common.networkConnected(activity)) {
                        String url = Common.URL_SERVER + "DriverServlet";
                        driver.setStatus(driver_status);
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("action", "driverUpdate");
                        jsonObject.addProperty("driver", new Gson().toJson(driver));
                        int count = 0;
                        try {
                            String result = new CommonTask(url, jsonObject.toString()).execute().get();
                            count = Integer.parseInt(result);
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                        if (count == 0) {
                            Common.showToast(activity, R.string.textUpdateFail);
                        } else {
                            Common.showToast(activity, R.string.textOffLine);
                        }
                    } else {
                        Common.showToast(activity, R.string.textNoNetwork);
                    }
                    if (fusedLocationClient != null) {
                        fusedLocationClient.removeLocationUpdates(locationCallback);
                    }
                }
            }
        });

    }

    /**
     * 註冊廣播接收器攔截聊天資訊
     * 因為是在Fragment註冊，所以Fragment頁面未開時不會攔截廣播
     */
    private void registerChatReceiver() {
        IntentFilter chatFilter = new IntentFilter("chat");
        broadcastManager.registerReceiver(chatReceiver, chatFilter);
    }

    // 接收到聊天訊息會在TextView呈現
    private BroadcastReceiver chatReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            final ChatMessage chatMessage = new Gson().fromJson(message, ChatMessage.class);
            String m = chatMessage.getMessage();
            // 接收到聊天訊息，若發送者與目前聊天對象相同，就換頁
            if (m.equals("call")) {
                CommonTwo.showToast(context,"call");
                //navController.navigate(R.id.driveFragment);
                new AlertDialog.Builder(activity)
                        /* 設定標題 */
                        //.setTitle(R.string.textTitle)
                        /* 設定圖示 */
                        //.setIcon(R.drawable.alert)
                        /* 設定訊息文字 */
                        .setMessage(R.string.textMessage)
                        /* 設定positive與negative按鈕上面的文字與點擊事件監聽器 */
                        .setPositiveButton(R.string.textYes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                /* 結束此Activity頁面 */
                                driver_status = 2;
                                if (Common.networkConnected(activity)) {
                                    String url = Common.URL_SERVER + "DriverServlet";
                                    driver.setStatus(driver_status);
                                    JsonObject jsonObject = new JsonObject();
                                    jsonObject.addProperty("action", "driverUpdate");
                                    jsonObject.addProperty("driver", new Gson().toJson(driver));
                                    int count = 0;
                                    try {
                                        String result = new CommonTask(url, jsonObject.toString()).execute().get();
                                        count = Integer.parseInt(result);
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
                                String sender = CommonTwo.loadUserName(activity);
                                String friend = chatMessage.getSender();
                                CommonTwo.saveCustomer(activity,friend);
                                String message = "yes";
                                ChatMessage chatMessage = new ChatMessage("chat", sender, friend, message);
                                String chatMessageJson = new Gson().toJson(chatMessage);
                                chatWebSocketClient.send(chatMessageJson);
                                Log.d(TAG, "output: " + chatMessageJson);
                                navController.navigate(R.id.driveFragment);
                            }
                        })
                        .setNegativeButton(R.string.textNo, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                /* 關閉對話視窗 */
                                String sender = CommonTwo.loadUserName(activity);
                                String friend = chatMessage.getSender();
                                String message = "no";
                                ChatMessage chatMessage = new ChatMessage("chat", sender, friend, message);
                                String chatMessageJson = new Gson().toJson(chatMessage);
                                chatWebSocketClient.send(chatMessageJson);
                                Log.d(TAG, "output: " + chatMessageJson);
                                dialog.cancel();
                            }
                        })
                        .show();
            }
            Log.d(TAG, message);
        }
    };

    private void refreshCenter(double lat,double lon){
        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(lat,lon));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(18);
        map.moveCamera(center);
        map.animateCamera(zoom);
    }

    // 檢查裝置是否開啟Location設定
    private void checkLocationSettings() {
        // 必須將LocationRequest設定加入檢查
        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder()
                        .addLocationRequest(locationRequest);
        Task<LocationSettingsResponse> task =
                LocationServices.getSettingsClient(activity)
                        .checkLocationSettings(builder.build());
        task.addOnSuccessListener(activity, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                if (ActivityCompat.checkSelfPermission(activity,
                        Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
                    // 取得並顯示最新位置
                    showLastLocation();

                }
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    Log.e(TAG, e.getMessage());
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        // 跳出Location設定的對話視窗
                        resolvable.startResolutionForResult(activity, REQ_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }
        });
    }

    // 更新位置訊息
    private void updateLastLocationInfo(Location lastLocation) {

        if (lastLocation == null) {
            Toast.makeText(activity, R.string.textLocationNotFound, Toast.LENGTH_SHORT).show();
            return;
        }
        refreshCenter(lastLocation.getLatitude(),lastLocation.getLongitude());

        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "DriverServlet";
            driver.setLocation(lastLocation.getLatitude(),lastLocation.getLongitude());
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "locationUpdate");
            jsonObject.addProperty("driver", new Gson().toJson(driver));
            int count = 0;
            try {
                String result = new CommonTask(url, jsonObject.toString()).execute().get();
                count = Integer.parseInt(result);
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
    }

    @Override
    public void onStart() {
        super.onStart();
        askAccessLocationPermission();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(driver_status == 2){
            driver_status = 1;
            if (Common.networkConnected(activity)) {
                String url = Common.URL_SERVER + "DriverServlet";
                driver.setStatus(driver_status);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", "driverUpdate");
                jsonObject.addProperty("driver", new Gson().toJson(driver));
                int count = 0;
                try {
                    String result = new CommonTask(url, jsonObject.toString()).execute().get();
                    count = Integer.parseInt(result);
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
        }
        showLastLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            fusedLocationClient = null;
        }
    }

    private void askAccessLocationPermission() {
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION
        };

        int result = ActivityCompat.checkSelfPermission(activity, permissions[0]);
        if (result == PackageManager.PERMISSION_DENIED) {
            requestPermissions(permissions, PER_ACCESS_LOCATION);
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        showMyLocation();

        if (requestCode == PER_ACCESS_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(activity, "Permission to access location should be granted", Toast.LENGTH_SHORT).show();
        }
    }

    private void showMyLocation() {
        if (ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        }
    }

    private void showLastLocation() {
        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
            fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        lastLocation = task.getResult();
                        updateLastLocationInfo(lastLocation);

                    }
                }
            });

            // 持續取得最新位置。looper設為null代表以現行執行緒呼叫callback方法，而非使用其他執行緒
            fusedLocationClient.requestLocationUpdates(
                    locationRequest, locationCallback, null);
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        // Fragment頁面切換時解除註冊，但不需要關閉WebSocket，
        // 否則回到前頁好友列表，會因為斷線而無法顯示好友
        broadcastManager.unregisterReceiver(chatReceiver);
        if(driver_status == 1){
            driver_status = 0;
            if (Common.networkConnected(activity)) {
                String url = Common.URL_SERVER + "DriverServlet";
                driver.setStatus(driver_status);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", "driverUpdate");
                jsonObject.addProperty("driver", new Gson().toJson(driver));
                int count = 0;
                try {
                    String result = new CommonTask(url, jsonObject.toString()).execute().get();
                    count = Integer.parseInt(result);
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
        }
    }
}