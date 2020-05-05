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
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static androidx.navigation.Navigation.findNavController;
import static com.lu.driver.CommonTwo.chatWebSocketClient;


public class DriveFragment extends Fragment {
    private View view;
    private static final int REQ_STAR = 2;
    private static final int REQ_CHECK_SETTINGS = 1;
    private Activity activity;
    private TextView tvName,tvPhone,tvModel,tvPlate,tvStart,tvEnd;
    private Button btSelect,btCancel,btFinish,btToCustomer,btToEnd;
    private static final String TAG = "TAG_DriveFragment";
    private int driver_id = 1;
    private int order_id = 0;
    private NavController navController;
    private String o,d;
    Double longitude,latitude;
    private LocalBroadcastManager broadcastManager;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationClient;
    private Location lastLocation;
    private Driver driver;
    private CircleImageView ivCustomer;
    String customer_id;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        broadcastManager = LocalBroadcastManager.getInstance(activity);
        CommonTwo.connectServer(activity, CommonTwo.loadUserName(activity));
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(3000) //3秒 單位：ms
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_drive, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        driver = new Driver(driver_id);
        registerChatReceiver();
        navController = findNavController(view);
        checkLocationSettings();
        Customer customer = null;
        Driver driver = null;
        tvName = view.findViewById(R.id.tvNameinfo);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvModel = view.findViewById(R.id.tvModel);
        tvPlate = view.findViewById(R.id.tvPlate);
        tvStart = view.findViewById(R.id.tvStart);
        tvEnd = view.findViewById(R.id.tvEnd);
        btSelect = view.findViewById(R.id.btSelect);
        btCancel = view.findViewById(R.id.btCancel);
        btFinish = view.findViewById(R.id.btFinish);
        btToCustomer = view.findViewById(R.id.btToCustomer);
        btToEnd = view.findViewById(R.id.btToEnd);
        ivCustomer = view.findViewById(R.id.ivCustomer);
        customer_id = CommonTwo.loadCustomer(activity).replaceAll("customer","");
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "CustomerServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "findById");
            jsonObject.addProperty("customer_id", customer_id);
            try {
                String jsonIn = new CommonTask(url, jsonObject.toString()).execute().get();
                customer = new Gson().fromJson(jsonIn, Customer.class);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        if(customer != null) {
            String Name,Phone,Model,Plate;
            Name = "姓名："+customer.getCustomer_name();
            Phone = "電話："+customer.getCustomer_phone();
            Model = "車款："+customer.getCustomer_car_model()+" ("+customer.getCustomer_car_color()+")";
            Plate = "車號："+customer.getCustomer_number_plate();
            tvName.setText(Name);
            tvPhone.setText(Phone);
            tvModel.setText(Model);
            tvPlate.setText(Plate);
        }
        showPhoto();
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "OrderServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getOrderId");
            jsonObject.addProperty("driver_id", String.valueOf(driver_id));
            try {
                String jsonIn = new CommonTask(url, jsonObject.toString()).execute().get();
                order_id = Integer.parseInt(jsonIn);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        Order order = null;
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "OrderServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "findById");
            jsonObject.addProperty("order_id", String.valueOf(order_id));
            try {
                String jsonIn = new CommonTask(url, jsonObject.toString()).execute().get();
                order = new Gson().fromJson(jsonIn, Order.class);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }

        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "DriverServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getLocation");
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

        if(driver != null){
            longitude = driver.getDriver_longitude();
            latitude = driver.getDriver_latitude();
        }

        if(order != null) {
            String Start,End;
            Start ="出發地："+order.getOrder_start();
            End = "目的地："+order.getOrder_end();
            tvStart.setText(Start);
            tvEnd.setText(End);
            o = order.getOrder_start();
            d = order.getOrder_end();
        }
        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(activity)
                        /* 設定標題 */
                        //.setTitle(R.string.textTitle)
                        /* 設定圖示 */
                        //.setIcon(R.drawable.alert)
                        /* 設定訊息文字 */
                        .setMessage(R.string.textCancel)
                        /* 設定positive與negative按鈕上面的文字與點擊事件監聽器 */
                        .setPositiveButton(R.string.textYes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                /* 結束此Activity頁面 */
                                String sender = CommonTwo.loadUserName(activity);
                                String friend = CommonTwo.loadCustomer(activity);
                                String message = "cancel";
                                ChatMessage chatMessage = new ChatMessage("chat", sender, friend, message);
                                String chatMessageJson = new Gson().toJson(chatMessage);
                                chatWebSocketClient.send(chatMessageJson);
                                Log.d(TAG, "output: " + chatMessageJson);
                                activity.onBackPressed();
                            }
                        })
                        .setNegativeButton(R.string.textNo, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                /* 關閉對話視窗 */
                                dialog.cancel();
                            }
                        })
                        .show();
            }
        });
        btSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btSelect.setVisibility(View.INVISIBLE);
                btToCustomer.setVisibility(View.INVISIBLE);
                btFinish.setVisibility(View.VISIBLE);
                btToEnd.setVisibility(View.VISIBLE);
                String sender = CommonTwo.loadUserName(activity);
                String friend = CommonTwo.loadCustomer(activity);
                String message = "start";
                ChatMessage chatMessage = new ChatMessage("chat", sender, friend, message);
                String chatMessageJson = new Gson().toJson(chatMessage);
                chatWebSocketClient.send(chatMessageJson);
                Log.d(TAG, "output: " + chatMessageJson);
            }
        });

        final String origin = o;
        final String destination = d;
        btToCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Address addressReverse = reverseGeocode(latitude, longitude);
                StringBuilder sb = new StringBuilder();
                if (addressReverse != null) {
                    for (int i = 0; i <= addressReverse.getMaxAddressLineIndex(); i++) {
                        sb.append(addressReverse.getAddressLine(i)).append("\n");
                    }
                }
                Address addressOrigin = getAddress(sb.toString());
                Address addressDestination = getAddress(origin);
                boolean notFound = false;
                // 檢查是否可將起點、目的地轉成address
                if (addressOrigin == null) {
                    Toast.makeText(activity, R.string.textOriginNotFound, Toast.LENGTH_SHORT).show();
                    notFound = true;
                }
                if (addressDestination == null) {
                    Toast.makeText(activity, R.string.textDestinationNotFound, Toast.LENGTH_SHORT).show();
                    notFound = true;
                }
                if (notFound) {
                    return;
                }

                double fromLat = addressOrigin.getLatitude();
                double fromLng = addressOrigin.getLongitude();
                double toLat = addressDestination.getLatitude();
                double toLng = addressDestination.getLongitude();

                direct(fromLat, fromLng, toLat, toLng);
            }
        });

        btToEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Address addressOrigin = getAddress(origin);
                Address addressDestination = getAddress(destination);
                boolean notFound = false;
                // 檢查是否可將起點、目的地轉成address
                if (addressOrigin == null) {
                    Toast.makeText(activity, R.string.textOriginNotFound, Toast.LENGTH_SHORT).show();
                    notFound = true;
                }
                if (addressDestination == null) {
                    Toast.makeText(activity, R.string.textDestinationNotFound, Toast.LENGTH_SHORT).show();
                    notFound = true;
                }
                if (notFound) {
                    return;
                }

                double fromLat = addressOrigin.getLatitude();
                double fromLng = addressOrigin.getLongitude();
                double toLat = addressDestination.getLatitude();
                double toLng = addressDestination.getLongitude();

                direct(fromLat, fromLng, toLat, toLng);
            }
        });
        btFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sender = CommonTwo.loadUserName(activity);
                String friend = CommonTwo.loadCustomer(activity);
                String message = "finish";
                ChatMessage chatMessage = new ChatMessage("chat", sender, friend, message);
                String chatMessageJson = new Gson().toJson(chatMessage);
                chatWebSocketClient.send(chatMessageJson);
                Log.d(TAG, "output: " + chatMessageJson);
                star();
                //activity.onBackPressed();
            }
        });
    }

    private void direct(double fromLat, double fromLng, double toLat,
                        double toLng) {
        String uriStr = String.format(Locale.US,
                "https://www.google.com/maps/dir/?api=1" +
                        "&origin=%f,%f&destination=%f,%f&travelmode=driving",
                fromLat, fromLng, toLat, toLng);
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uriStr));
        intent.setClassName("com.google.android.apps.maps",
                "com.google.android.maps.MapsActivity");
        startActivity(intent);
    }

    private Address getAddress(String locationName) {
        Geocoder geocoder = new Geocoder(activity);
        List<Address> addressList = null;

        try {
            addressList = geocoder.getFromLocationName(locationName, 1);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
        if (addressList == null || addressList.isEmpty()) {
            return null;
        } else {
            return addressList.get(0);
        }
    }

    private Address reverseGeocode(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(activity);
        List<Address> addressList = null;
        try {
            addressList = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }

        if (addressList == null || addressList.isEmpty()) {
            return null;
        } else {
            return addressList.get(0);
        }
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
            if(m.equals("cancel")){
                new AlertDialog.Builder(activity)
                        /* 設定標題 */
                        //.setTitle(R.string.textTitle)
                        /* 設定圖示 */
                        //.setIcon(R.drawable.alert)
                        /* 設定訊息文字 */
                        .setMessage(R.string.textCancelCheck)
                        /* 設定positive與negative按鈕上面的文字與點擊事件監聽器 */
                        .setPositiveButton(R.string.textYes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                /* 結束此Activity頁面 */
                                activity.onBackPressed();
                            }
                        })
                        .setNegativeButton(R.string.textNo, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                /* 關閉對話視窗 */
                                activity.onBackPressed();
                            }
                        })
                        .show();
            }
            Log.d(TAG, message);
        }
    };

    private void star() {
        Intent loginIntent = new Intent(activity, StarDialogActivity.class);
        startActivityForResult(loginIntent, REQ_STAR);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        view = getView();
        if(resultCode == RESULT_OK) {
            if (requestCode == REQ_STAR) {
                SharedPreferences pref = activity.getSharedPreferences(Common.PREF_FILE,
                        MODE_PRIVATE);
                float n = pref.getFloat("n", 0);
                Order order = new Order(order_id,n);
                if (Common.networkConnected(activity)) {
                    String url = Common.URL_SERVER + "OrderServlet";
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", "customer_scoreUpdate");
                    jsonObject.addProperty("order", new Gson().toJson(order));
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
                CommonTwo.showToast(activity,String.valueOf(n));
                activity.onBackPressed();
            }
        }
        else if(resultCode == RESULT_CANCELED){
            if (requestCode == REQ_STAR) {
                SharedPreferences pref = activity.getSharedPreferences(Common.PREF_FILE,
                        MODE_PRIVATE);
                float m = pref.getFloat("m", 0);
                Order order = new Order(order_id,m);
                if (Common.networkConnected(activity)) {
                    String url = Common.URL_SERVER + "OrderServlet";
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", "customer_scoreUpdate");
                    jsonObject.addProperty("order", new Gson().toJson(order));
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
                CommonTwo.showToast(activity,String.valueOf(m));
                activity.onBackPressed();
            }
        }
    }

    // 更新位置訊息
    private void updateLastLocationInfo(Location lastLocation) {
        if (lastLocation == null) {
            Toast.makeText(activity, R.string.textLocationNotFound, Toast.LENGTH_SHORT).show();
            return;
        }

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
    public void onPause() {
        super.onPause();
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            fusedLocationClient = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        showLastLocation();
    }

    private void showPhoto(){
        int imageSize = getResources().getDisplayMetrics().widthPixels / 3;
        Bitmap bitmap = null;
        try {
            String url = Common.URL_SERVER + "CustomerServlet";
            bitmap = new CustomerImageTask(url, Integer.parseInt(customer_id), imageSize).execute().get();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        if (bitmap != null) {
            ivCustomer.setImageBitmap(bitmap);
        } else {
            ivCustomer.setImageResource(R.drawable.no_image);
        }
    }
}
