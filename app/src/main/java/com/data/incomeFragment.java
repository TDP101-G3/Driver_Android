package com.data;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lu.driver.Common;
import com.lu.driver.CommonTask;
import com.lu.driver.R;

import java.util.ArrayList;
import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;
import static com.data.WeekPickerDialog.weekNumber;
import static com.data.WeekPickerDialog.yearNumber;


/**
 * A simple {@link Fragment} subclass.
 */
public class incomeFragment extends Fragment {
    private TextView tvWeekPick,tvOrderCount,tvWeekIncome;
    private Activity activity;
    private int driver_id, week_order_number, week_order_money,monday_order_number,tuesday_order_number,wednesday_order_number,thursday_order_number,friday_order_number,saturday_order_number,sunday_order_number;
    private int driver_id_2, driver_id_3,driver_id_4,driver_id_5,driver_id_6,driver_id_7;
    private int yearNumber_2, yearNumber_3,yearNumber_4,yearNumber_5,yearNumber_6,yearNumber_7;
    private int weekNumber_2, weekNumber_3,weekNumber_4,weekNumber_5,weekNumber_6,weekNumber_7;
//    private CommonTask incomeTask;
    private String TAG = "TAG_incomeFragment";
//    private Order order;
    private BarChart barChart;
    private ArrayList<BarEntry> barEntryArrayList;
    private ArrayList<String> labelsNames;

    ArrayList<MonthDalesData> monthDalesDataList = new ArrayList<>();




    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        activity.setTitle(R.string.textIncome);
        return inflater.inflate(R.layout.fragment_income, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvWeekPick = view.findViewById(R.id.tvWeekPick);
        tvOrderCount = view.findViewById(R.id.tvOrderCount);
        tvWeekIncome = view.findViewById(R.id.tvWeekIncome);
        barChart = view.findViewById(R.id.Barchart);
        SharedPreferences pref = activity.getSharedPreferences(Common.PREF_FILE,
                MODE_PRIVATE);
        driver_id = pref.getInt("driver_id", 0);

        tvWeekPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new WeekPickerDialog(activity,Calendar.getInstance(), new WeekPickerDialog.OnWeekSelectListener() {
                    @Override
                    public void onWeekSelect(Week week) {
                        if(week == null){
                            tvWeekPick.setText("請選擇");
                        }else {
                            tvWeekPick.setText(week.getSelectWeekBeginAndEnd());
//                            Common.showToast(activity, "歡迎回來"+yearNumber+weekNumber);

                            if (Common.networkConnected(activity)) {
                                String url = Common.URL_SERVER + "OrderServlet";//連伺服器
                                JsonObject jsonObject = new JsonObject();   //建一個物件
                                jsonObject.addProperty("action", "incomeWeekAll");
                                jsonObject.addProperty("driver_id", driver_id);
                                jsonObject.addProperty("yearNumber", yearNumber);
                                jsonObject.addProperty("weekNumber", weekNumber);

                                try {
                                    Gson gson = new Gson();
                                    String jsonIn = new CommonTask(url, jsonObject.toString()).execute().get();
                                    JsonObject incomeWeekAllDetail = gson.fromJson(jsonIn, JsonObject.class);
                                        week_order_number = incomeWeekAllDetail.get("week_order_number").getAsInt();
                                        week_order_money = incomeWeekAllDetail.get("week_order_money").getAsInt();
                                } catch (Exception e) {
                                    Log.e(TAG, e.toString()); }
                            } else {
                                    Common.showToast(activity, R.string.textNoNetwork);
                                    }

                            tvOrderCount.setText(String.valueOf(week_order_number));
                            tvWeekIncome.setText("$"+String.valueOf(week_order_money));
                            getIncomeweekdaily();

                        }
                    }
                }).show();
            }




        });




    }

    private void getIncomeweekdaily(){
        driver_id_2 = driver_id;
        driver_id_3 = driver_id;
        driver_id_4 = driver_id;
        driver_id_5 = driver_id;
        driver_id_6 = driver_id;
        driver_id_7 = driver_id;

        yearNumber_2 = yearNumber;
        yearNumber_3 = yearNumber;
        yearNumber_4 = yearNumber;
        yearNumber_5 = yearNumber;
        yearNumber_6 = yearNumber;
        yearNumber_7 = yearNumber;

        weekNumber_2 = weekNumber;
        weekNumber_3 = weekNumber;
        weekNumber_4 = weekNumber;
        weekNumber_5 = weekNumber;
        weekNumber_6 = weekNumber;
        weekNumber_7 = weekNumber;

        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "OrderServlet";//連伺服器
            JsonObject jsonObject = new JsonObject();   //建一個物件
            jsonObject.addProperty("action", "getIncomeweekdaily");
            jsonObject.addProperty("driver_id", driver_id);
            jsonObject.addProperty("yearNumber", yearNumber);
            jsonObject.addProperty("weekNumber", weekNumber);

            jsonObject.addProperty("driver_id_2", driver_id_2);
            jsonObject.addProperty("driver_id_3", driver_id_3);
            jsonObject.addProperty("driver_id_4", driver_id_4);
            jsonObject.addProperty("driver_id_5", driver_id_5);
            jsonObject.addProperty("driver_id_6", driver_id_6);
            jsonObject.addProperty("driver_id_7", driver_id_7);

            jsonObject.addProperty("yearNumber_2", yearNumber_2);
            jsonObject.addProperty("yearNumber_3", yearNumber_3);
            jsonObject.addProperty("yearNumber_4", yearNumber_4);
            jsonObject.addProperty("yearNumber_5", yearNumber_5);
            jsonObject.addProperty("yearNumber_6", yearNumber_6);
            jsonObject.addProperty("yearNumber_7", yearNumber_7);

            jsonObject.addProperty("weekNumber_2", weekNumber_2);
            jsonObject.addProperty("weekNumber_3", weekNumber_3);
            jsonObject.addProperty("weekNumber_4", weekNumber_4);
            jsonObject.addProperty("weekNumber_5", weekNumber_5);
            jsonObject.addProperty("weekNumber_6", weekNumber_6);
            jsonObject.addProperty("weekNumber_7", weekNumber_7);


            try {
                Gson gson = new Gson();
                String jsonIn = new CommonTask(url, jsonObject.toString()).execute().get();
                JsonObject incomeWeekAllDetail = gson.fromJson(jsonIn, JsonObject.class);
                monday_order_number = incomeWeekAllDetail.get("monday_order_number").getAsInt();
                tuesday_order_number = incomeWeekAllDetail.get("tuesday_order_number").getAsInt();
                wednesday_order_number = incomeWeekAllDetail.get("wednesday_order_number").getAsInt();
                thursday_order_number = incomeWeekAllDetail.get("thursday_order_number").getAsInt();
                friday_order_number = incomeWeekAllDetail.get("friday_order_number").getAsInt();
                saturday_order_number = incomeWeekAllDetail.get("saturday_order_number").getAsInt();
                sunday_order_number = incomeWeekAllDetail.get("sunday_order_number").getAsInt();
//                Common.showToast(activity, "2222"+String.valueOf(monday_order_number));

                barEntryArrayList = new ArrayList<>();
                labelsNames = new ArrayList<>();
                MonthSales();

                for(int i = 0; i < monthDalesDataList.size(); i++  ){
                    String month = monthDalesDataList.get(i).getMonth();
                    int sales = monthDalesDataList.get(i).getSale();
                    barEntryArrayList.add(new BarEntry(i,sales));
                    labelsNames.add(month);
                }
                BarDataSet barDataSet = new BarDataSet(barEntryArrayList,"單日接單數");
                barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
                Description description = new Description();
                description.setText("星期幾");
                barChart.setDescription(description);
                BarData barData = new BarData(barDataSet);
                barChart.setData(barData);


                XAxis xAxis = barChart.getXAxis();
                xAxis.setValueFormatter(new IndexAxisValueFormatter(labelsNames));

                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setDrawAxisLine(false);
                xAxis.setGranularity(1f);
                xAxis.setLabelCount(labelsNames.size());

                xAxis.setLabelRotationAngle(0);
                barChart.animateY(5000);
                barChart.invalidate();


            } catch (Exception e) {
                Log.e(TAG, e.toString()); }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);

        }

    }

    private  void  MonthSales(){
        monthDalesDataList.clear();
        monthDalesDataList.add(new MonthDalesData("一",monday_order_number));
        monthDalesDataList.add(new MonthDalesData("二",tuesday_order_number));
        monthDalesDataList.add(new MonthDalesData("三",wednesday_order_number));
        monthDalesDataList.add(new MonthDalesData("四",thursday_order_number));
        monthDalesDataList.add(new MonthDalesData("五",friday_order_number));
        monthDalesDataList.add(new MonthDalesData("六",saturday_order_number));
        monthDalesDataList.add(new MonthDalesData("日",sunday_order_number));
    }

}
