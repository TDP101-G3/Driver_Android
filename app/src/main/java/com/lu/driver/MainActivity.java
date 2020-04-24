package com.lu.driver;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpActionBar();
        initDrawer();
        NavController navController =
                Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    private void setUpActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // 設定ActionBar標題列的左上角可以加上按鈕
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    // 初始化抽屜選單功能
    private void initDrawer() {
        drawerLayout = findViewById(R.id.drawerLayout);
        // 建立ActionBarDrawerToggle監聽器，監聽抽屜開關的狀態
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.textOpen, R.string.textClose);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        // 將左上角按鈕動畫與抽屜選單開關同步化
        actionBarDrawerToggle.syncState();

        navigationView = findViewById(R.id.navigationView);
    }

    // 點擊標題列上的按鈕會呼叫此方法
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 如果抽屜開啟，將之關閉；如果關閉，將之開啟
        if (item.getItemId() == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
