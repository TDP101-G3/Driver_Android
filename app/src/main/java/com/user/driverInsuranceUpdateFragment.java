package com.user;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lu.driver.Common;
import com.lu.driver.CommonTask;
import com.lu.driver.R;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Calendar;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class driverInsuranceUpdateFragment extends Fragment implements DatePickerDialog.OnDateSetListener {
    private String TAG = "TAG_driverInsuranceUpdateFragment";
    private FragmentActivity activity;
    private TextView tvInsuranceDate;
    private ImageView ivInsurance;
    private CommonTask updateTask;
    private byte[] image;
    private static final int REQ_TAKE_PICTURE = 0;
    private static final int REQ_PICK_IMAGE = 1;
    private static final int REQ_CROP_PICTURE = 2;
    private static final int PER_EXTERNAL_STORAGE = 201;
    private Uri contentUri, croppedImageUri;
    private ConstraintLayout layoutExpire;
    private static int year, month, day;
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
        activity.setTitle(R.string.textDriverSecure);
        return inflater.inflate(R.layout.fragment_driver_insurance_update, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvInsuranceDate = view.findViewById(R.id.tvInsuranceDate);
        layoutExpire = view.findViewById(R.id.layoutExpire);
        ivInsurance = view.findViewById(R.id.ivInsurance);
        final NavController navController;
        navController = Navigation.findNavController(view);

        // 選擇時間
        layoutExpire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);
                new DatePickerDialog(
                        activity,
                        driverInsuranceUpdateFragment.this,
                        driverInsuranceUpdateFragment.year, driverInsuranceUpdateFragment.month, driverInsuranceUpdateFragment.day)
                        .show();
                showExpireDate();
            }
        });

        // 完成上傳
        Button btCommit = view.findViewById(R.id.btCommitInsurance);
        btCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String expireDate = tvInsuranceDate.getText().toString();
                if (expireDate.length() <= 0) {
                    Common.showToast(activity, R.string.textDateIsInvalid);
                    return;
                }
                if (Common.networkConnected(activity)) {
                    String url = Common.URL_SERVER + "DriverServlet";
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", "updateInsurance");
                    jsonObject.addProperty("driver_id", driver_id);
                    jsonObject.addProperty("expireDate", expireDate);
                    // 有圖才上傳
                    if (image == null) {
                        Common.showToast(activity, R.string.textImageIsInvalid);
                        return;
                    } else {jsonObject.addProperty("imageBase64", Base64.encodeToString(image, Base64.DEFAULT));}
                    int count = 0;
                    updateTask = new CommonTask(url, jsonObject.toString());
                    try {
                        String result = updateTask.execute().get();
                        System.out.println("update result: " + result);
                        count = Integer.valueOf(result);
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
                /* 回前一個Fragment */
                navController.popBackStack();
            }
        });

        Button btInsuranceAdd = view.findViewById(R.id.btInsuranceAdd);
        btInsuranceAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                PopupMenu popupMenu = null;
                // 判斷版本
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    popupMenu = new PopupMenu(activity, view, Gravity.END);
                }
                popupMenu.inflate(R.menu.photo_menu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.takePicture:
                                askExternalStoragePermission();
                                takePicture();
                                break;
                            case R.id.pickPicture:
                                pickPicture();
                                break;
                        }
                        return false;
                    }
                });
                popupMenu.show();

            }
        });

    }

    /* 覆寫OnDateSetListener.onDateSet()以處理日期挑選完成事件。
           日期挑選完成會呼叫此方法，並傳入選取的年月日 */

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        driverInsuranceUpdateFragment.year = year;
        driverInsuranceUpdateFragment.month = month;
        driverInsuranceUpdateFragment.day = day;
        updateDisplay();
    }

    private void showExpireDate() {
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        updateDisplay();
    }

    private void updateDisplay() {
        tvInsuranceDate.setText(new StringBuilder().append(year).append("-")
                .append(pad(month + 1)).append("-").append(pad(day))
        );
    }

    private String pad(int number) {
        if (number >= 10) {
            return String.valueOf(number);
        } else {
            return "0" + number;
        }
    }

    private void showToast(Context context, int messageId) {
        Toast.makeText(context, messageId, Toast.LENGTH_SHORT).show();
    }

    // 詢問使用者 取用外部儲存體的公開檔案
    private void askExternalStoragePermission() {
        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE
        };
        int result = ContextCompat.checkSelfPermission(activity, permissions[0]);
        if (result == PackageManager.PERMISSION_DENIED) {
            requestPermissions(permissions, PER_EXTERNAL_STORAGE);
        }
    }

    private void takePicture(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File dir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (dir != null && !dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e(TAG, getString(R.string.textDirNotCreated));
                return;
            }
        }
        dir = new File(dir, "picture.jpg"); // 要存檔的路徑
        contentUri = FileProvider.getUriForFile( // 在manifest23行還有xml都要做事情
                activity, activity.getPackageName() + ".provider", dir);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);

        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            startActivityForResult(intent, REQ_TAKE_PICTURE); // 拍照
        } else {
            showToast(activity, R.string.textNoCameraApp);
        }
    }

    private void pickPicture() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQ_PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) { // 使用者決定要不要照片
            switch (requestCode) {
                case REQ_TAKE_PICTURE:
                    crop(contentUri);
                    break;
                case REQ_PICK_IMAGE:
                    Uri uri = data.getData();
                    crop(uri);
                    break;
                case REQ_CROP_PICTURE:
                    Log.d(TAG, "REQ_CROP_PICTURE: " + croppedImageUri.toString());
                    try {
                        Bitmap picture = BitmapFactory.decodeStream(
                                activity.getContentResolver().openInputStream(croppedImageUri));
                        ivInsurance.setImageBitmap(picture);
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        picture.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        image = out.toByteArray();
                    } catch (FileNotFoundException e) {
                        Log.e(TAG, e.toString());
                    }
                    break;
            }
        }
    }

    private void crop(Uri sourceImageUri) {
        File file = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        file = new File(file, "picture_cropped.jpg");
        croppedImageUri = Uri.fromFile(file);
        Uri destinationUri = Uri.fromFile(file);
        UCrop.of(sourceImageUri, destinationUri)
                //.withAspectRatio(1, 1) // 設定裁減比例
                //.withMaxResultSize(900, 900) // 設定結果尺寸不可超過指定寬高
                .start(activity, this, REQ_CROP_PICTURE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PER_EXTERNAL_STORAGE) {
            // 如果user不同意將資料儲存至外部儲存體的公開檔案，就將儲存按鈕設為disable
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(activity, R.string.textShouldGrant, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
