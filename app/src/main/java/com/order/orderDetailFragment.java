package com.order;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lu.driver.Common;
import com.lu.driver.CommonTask;
import com.lu.driver.Customer;
import com.lu.driver.Order;
import com.lu.driver.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class orderDetailFragment extends Fragment {
    private static final String TAG = "orderDetailFragment";
    private Activity activity;
    private TextView tvOrderNumber, tvDetailStartAddress, tvDetailEndAddress, tvDetailOrderDate, tvDetailOrderMoney, tvDetailCustomer, tvDetailComment, tvDetailCustomerRate, tvHelp;
    private Order order;
    private CommonTask getDriverNameTask;
    private GoogleMap map;
    private MapView mvOrder;
    private LatLng start_latLng=null;
    private LatLng end_latLng, open_latLng;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity.setTitle(R.string.textOrder);
        return inflater.inflate(R.layout.fragment_order_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final NavController navController;
        navController = Navigation.findNavController(view);
        Bundle bundle = getArguments();
        if (bundle == null || bundle.getSerializable("order") == null) {
            Common.showToast(activity, R.string.textNoOrdersFound);
            navController.popBackStack();
            return;
        }
        order = (Order) bundle.getSerializable("order");
        //double test= order.getStart_latitude();
        //System.out.println("test order can find LatLng:" + test);

        String start_address = order.getOrder_start();
        String end_address = order.getOrder_end();
        tvOrderNumber = view.findViewById(R.id.tvOrderNumber);
        tvDetailStartAddress = view.findViewById(R.id.tvDetailStartAddress);
        tvDetailEndAddress = view.findViewById(R.id.tvDetailEndAddress);
        tvDetailOrderDate = view.findViewById(R.id.tvDetailOrderDate);
        tvDetailOrderMoney = view.findViewById(R.id.tvDetailOrderMoney);
        tvDetailCustomer = view.findViewById(R.id.tvDetailCustomer);
        tvDetailComment = view.findViewById(R.id.tvDetailComment);
        tvDetailCustomerRate = view.findViewById(R.id.tvDetailCustomerRate);
        tvHelp = view.findViewById(R.id.tvHelp);

        tvOrderNumber.setText(String.valueOf(order.getOrder_id()));
        tvDetailStartAddress.setText(start_address);
        tvDetailEndAddress.setText(end_address);
        tvDetailOrderDate.setText(order.getOrder_time());
        tvDetailOrderMoney.setText(String.valueOf(order.getOrder_money()));
        String customer_name = getCustomerName(order.getCustomer_id());
        String text = "您給" + customer_name + "評分了";
        tvDetailCustomer.setText(customer_name);
        tvDetailComment.setText(text);
        tvDetailCustomerRate.setText(String.valueOf(order.getCustomer_score()));

        mvOrder = view.findViewById(R.id.mvOrder);
        if (!Double.toString(order.getStart_latitude()).equals("0.0")) {
            start_latLng = new LatLng(order.getStart_latitude(), order.getStart_longitude());
            end_latLng = new LatLng(order.getEnd_latitude(), order.getEnd_longitude());
        } else{
            if (start_address.isEmpty() || end_address.isEmpty()){
                Common.showToast(activity,R.string.textLocationNotFound);
            } else {
                Address address_start = geocode(start_address);
                Address address_end = geocode(end_address);
                if (address_end==null || address_start==null){
                    Common.showToast(activity,R.string.textNoMap);
                    return;
                }
                start_latLng = new LatLng(address_start.getLatitude(), address_start.getLongitude());
                end_latLng = new LatLng(address_end.getLatitude(), address_end.getLongitude());
            }
        }
        //System.out.println("order LatLng Start/End: " + start_latLng + " / " + end_latLng);
        if (start_latLng==null) {
            Common.showToast(activity,R.string.textNoMap);
            return;
        } else {
            open_latLng = new LatLng((start_latLng.latitude + end_latLng.latitude) / 2, (start_latLng.longitude + end_latLng.longitude) / 2);

            mvOrder.onCreate(savedInstanceState);
            mvOrder.onStart();
            mvOrder.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    map = googleMap;
                    BitmapDescriptor icon_start = BitmapDescriptorFactory.fromResource(R.drawable.spotstart);
                    BitmapDescriptor icon_end = BitmapDescriptorFactory.fromResource(R.drawable.spotend);
                    map.addMarker(new MarkerOptions()
                            .position(start_latLng)
                            .icon(icon_start));
                    map.addMarker(new MarkerOptions()
                            .position(end_latLng)
                            .icon(icon_end));
                    moveMap(open_latLng);
                }
            });
        }
    }
    private String getCustomerName(int customer_id) {
        String customer_name = "";
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "/CustomerServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "findById");
            jsonObject.addProperty("customer_id", customer_id);
            String jsonOut = jsonObject.toString();
            getDriverNameTask = new CommonTask(url, jsonOut);
            try{
                String jsonIn = getDriverNameTask.execute().get();
                Customer customer = null;
                customer = new Gson().fromJson(jsonIn, Customer.class);
                customer_name = customer.getCustomer_name();
            }catch (Exception e){
                Log.e (TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return customer_name;
    }

    private void moveMap(LatLng latLng) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(12)
                .build();
        CameraUpdate cameraUpdate = CameraUpdateFactory
                .newCameraPosition(cameraPosition);
        map.animateCamera(cameraUpdate);
    }

//    private LatLng getLocationFromAddress(String Address) throws IOException {
//        Geocoder coder = new Geocoder(activity);
//        List<Address> address;
//        LatLng point = null;
//            address = coder.getFromLocationName(Address, 5);
//            if (address == null)
//                return null;
//            Address location = address.get(0);
//            point = new LatLng(location.getLatitude(),location.getLongitude());
//
//        return point;
//    }

    private Address geocode(String locationName) {
        //System.out.println("order geocode() address: "+locationName);
        Geocoder geocoder = new Geocoder(activity);
        List<Address> addressList = null;
        try {
            addressList = geocoder.getFromLocationName(locationName, 1);
            if(addressList == null){
                return null;
            }
            Address address = addressList.get(0);
            //System.out.println("order geocode() addressList1: "+ address);
            return address;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
