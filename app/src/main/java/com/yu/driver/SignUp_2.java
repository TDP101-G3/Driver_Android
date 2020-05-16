package com.yu.driver;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lu.driver.Common;
import com.lu.driver.CommonTask;
import com.lu.driver.Driver;
import com.lu.driver.R;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class SignUp_2 extends Fragment {
    private final static String TAG = "TAG_Sign2";
    private Activity activity;
    private Button btSignUp;
    private ImageView ivIdFront, ivIdBack, ivLicenseFront, ivLicenseBack, ivDriverSecure;
    private File file_ivIdFront, file_ivIdBack, file_ivLicenseFront, file_ivLicenseBack, file_ivDriverSecure;
    private static final int REQ_TAKE_PICTURE = 0;
    private static final int REQ_PICK_PICTURE = 1;
    private static final int REQ_CROP_PICTURE = 2;
    private Uri contentUri;
    List<Bitmap> bitmaps;
    private int count = 0;
    private byte[] idFront, idBack, licenseFront, licenseBack, driverSecure;
//    private TextView tvResult;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity.setTitle(R.string.textSignUP);
        return inflater.inflate(R.layout.fragment_sign_up_2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btSignUp = view.findViewById(R.id.btSignUP);
//        tvResult = view.findViewById(R.id.tvResult);
        ivIdFront = view.findViewById(R.id.ivIdFront);
        ivIdBack = view.findViewById(R.id.ivIdBack);
        ivLicenseFront = view.findViewById(R.id.ivLicenseFront);
        ivLicenseBack = view.findViewById(R.id.ivLicenseBack);
        ivDriverSecure = view.findViewById(R.id.ivDriverSecure);
        bitmaps = new ArrayList<>();


        Bundle sign2 = getArguments();
        if (sign2 != null) {
            final String driver_name = sign2.getString("name");
            final String driver_password = sign2.getString("password");
            final String driver_email = sign2.getString("email");
            final String driver_phone = sign2.getString("phoneNumber");
            final String driver_bank_name = sign2.getString("bankAccountName");
            final String driver_bank_account = sign2.getString("bankAccount");
            final String driver_bank_code = sign2.getString("bankCode");
//            String text1 = "User name: " + driver_name + "; password: " + driver_password + "\n" + driver_bank_code;
//            tvResult.append(text1);


            ivIdFront.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File dir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    file_ivIdFront = new File(dir, "ivIdFront.jpg");
                    contentUri = FileProvider.getUriForFile(
                            activity, activity.getPackageName() + ".provider", file_ivIdFront);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);

                    if (intent.resolveActivity(activity.getPackageManager()) != null) {
                        startActivityForResult(intent, REQ_TAKE_PICTURE);
                    } else {
                        Common.showToast(activity, "啟動相機失敗");
                    }
                }
            });
            ivIdBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File dir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    file_ivIdBack = new File(dir, "ivIdBack.jpg");
                    contentUri = FileProvider.getUriForFile(
                            activity, activity.getPackageName() + ".provider", file_ivIdBack);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);

                    if (intent.resolveActivity(activity.getPackageManager()) != null) {
                        startActivityForResult(intent, REQ_TAKE_PICTURE);
                    } else {
                        Common.showToast(activity, "啟動相機失敗");
                    }
                }
            });
            ivLicenseFront.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File dir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    file_ivLicenseFront = new File(dir, "ivLicenseFront.jpg");
                    contentUri = FileProvider.getUriForFile(
                            activity, activity.getPackageName() + ".provider", file_ivLicenseFront);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);

                    if (intent.resolveActivity(activity.getPackageManager()) != null) {
                        startActivityForResult(intent, REQ_TAKE_PICTURE);
                    } else {
                        Common.showToast(activity, "啟動相機失敗");
                    }
                }
            });
            ivLicenseBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File dir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    file_ivLicenseBack = new File(dir, "ivLicenseBack.jpg");
                    contentUri = FileProvider.getUriForFile(
                            activity, activity.getPackageName() + ".provider", file_ivLicenseBack);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);

                    if (intent.resolveActivity(activity.getPackageManager()) != null) {
                        startActivityForResult(intent, REQ_TAKE_PICTURE);
                    } else {
                        Common.showToast(activity, "啟動相機失敗");
                    }
                }
            });
            ivDriverSecure.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File dir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    file_ivDriverSecure = new File(dir, "ivDriverSecure.jpg");
                    contentUri = FileProvider.getUriForFile(
                            activity, activity.getPackageName() + ".provider", file_ivDriverSecure);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);

                    if (intent.resolveActivity(activity.getPackageManager()) != null) {
                        startActivityForResult(intent, REQ_TAKE_PICTURE);
                    } else {
                        Common.showToast(activity, "啟動相機失敗");
                    }
                }
            });


            btSignUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (bitmaps.size() != 5) {//如果沒拍照，就不能上傳
                        Common.showToast(activity, "驗證資料不完整就不能繼續註冊哦！");
                        return;
                    } else {
                        if (Common.networkConnected(activity)) {
                            String url = Common.URL_SERVER + "DriverServlet";//連伺服器
                            Driver driver = new Driver(driver_name, driver_email, driver_password, driver_phone, driver_bank_name, driver_bank_account, driver_bank_code);
                            idFront = Common.bitmapToPNG(bitmaps.get(0));
                            idBack = Common.bitmapToPNG(bitmaps.get(1));
                            licenseFront = Common.bitmapToPNG(bitmaps.get(2));
                            licenseBack = Common.bitmapToPNG(bitmaps.get(3));
                            driverSecure = Common.bitmapToPNG(bitmaps.get(4));
                            JsonObject jsonObject = new JsonObject();   //建一個物件
                            jsonObject.addProperty("action", "signUp");
                            jsonObject.addProperty("driver", new Gson().toJson(driver));

                            jsonObject.addProperty("imageBase64", Base64.encodeToString(idFront, Base64.DEFAULT));
                            jsonObject.addProperty("idBackBase64", Base64.encodeToString(idBack, Base64.DEFAULT));
                            jsonObject.addProperty("licenseFrontBase64", Base64.encodeToString(licenseFront, Base64.DEFAULT));
                            jsonObject.addProperty("licenseBackBase64", Base64.encodeToString(licenseBack, Base64.DEFAULT));
                            jsonObject.addProperty("driverSecureBase64", Base64.encodeToString(driverSecure, Base64.DEFAULT));

                            int count = 0;
                            try {
                                String result = new CommonTask(url, jsonObject.toString()).execute().get();
                                count = Integer.parseInt(result);
                            } catch (Exception e) {
                                Log.e(TAG, e.toString());
                            }
                            if (count == 0) {
                                Common.showToast(activity, "註冊失敗");
                            } else {
                                Common.showToast(activity, "註冊成功，請重新登入");
                            }
                        } else {
                            Common.showToast(activity, "沒有連線");
                        }
//                        Navigation.findNavController(v).navigate(R.id.action_signUp_2_to_checkPhoneNumber);
                        Navigation.findNavController(v).navigate(R.id.action_signUp_2_to_login);
                    }

                }
            });
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQ_TAKE_PICTURE:
                    crop(contentUri);
                    break;
                case REQ_PICK_PICTURE:
                    crop(intent.getData());
                    break;
                case REQ_CROP_PICTURE:
                    handleCropResult(intent);
                    break;
            }

        }
    }


    private void crop(Uri sourceImageUri) {
        File file = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        file = new File(file, "picture_cropped.jpg");
        Uri destinationUri = Uri.fromFile(file);
        UCrop.of(sourceImageUri, destinationUri)
//                .withAspectRatio(16, 9) // 設定裁減比例
//                .withMaxResultSize(500, 500) // 設定結果尺寸不可超過指定寬高
                .start(activity, this, REQ_CROP_PICTURE);
    }

    private void handleCropResult(Intent intent) {

        Uri resultUri = UCrop.getOutput(intent);
        if (resultUri == null) {
            return;
        }
        Bitmap bitmap = null;
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                bitmap = BitmapFactory.decodeStream(
                        activity.getContentResolver().openInputStream(resultUri));
                bitmaps.add(bitmap);
                count++;

            } else {
                ImageDecoder.Source source =
                        ImageDecoder.createSource(activity.getContentResolver(), resultUri);
                bitmap = ImageDecoder.decodeBitmap(source);
                bitmaps.add(bitmap);
                count++;
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }

        if (bitmap != null) {
            switch (count) {
                case 1:
//                    Common.showToast(activity, "22222222222");
                    ivIdFront.setImageBitmap(bitmaps.get(0));
                    break;
                case 2:
                    ivIdBack.setImageBitmap(bitmaps.get(1));
                    break;
                case 3:
                    ivLicenseFront.setImageBitmap(bitmaps.get(2));
                    break;
                case 4:
                    ivLicenseBack.setImageBitmap(bitmaps.get(3));
                    break;
                case 5:
                    ivDriverSecure.setImageBitmap(bitmaps.get(4));
                    break;
            }
        }
    }
}



