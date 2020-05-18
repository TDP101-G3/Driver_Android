package com.user;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.lu.driver.Common;
import com.lu.driver.CommonTask;
import com.lu.driver.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;

import static android.app.Activity.RESULT_OK;

public class idUpdateFragment extends Fragment {
    private String TAG = "TAG_idUpdateFragment";
    private FragmentActivity activity;
    private ImageView ivIdFront, ivIdBack;
    private byte[] imageFront, imageBack;
    private File fileIdFront, fileIdBack;
    private static final int REQ_TAKE_PICTURE = 0;
    private static final int REQ_PICK_IMAGE = 1;
    private static final int REQ_CROP_PICTURE = 2;
    private static final int PER_EXTERNAL_STORAGE = 201;
    private Uri contentUri, croppedImageUri;
    private int action = 0;
    private int driver_id = 1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity.setTitle(R.string.textIdPhoto);
        return inflater.inflate(R.layout.fragment_id_update, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ivIdFront = view.findViewById(R.id.ivIdFront);
        ivIdBack = view.findViewById(R.id.ivIdBack);
        final NavController navController;
        navController = Navigation.findNavController(view);

        // 完成上傳
        Button btCommit = view.findViewById(R.id.btCommitId);
        btCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Common.networkConnected(activity)) {
                    String url = Common.URL_SERVER + "DriverServlet";
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", "updateId");
                    jsonObject.addProperty("driver_id", driver_id);
                    // 有圖才上傳
                    if (imageFront == null) {
                        Common.showToast(activity, R.string.textImageIsInvalid);
                        return;
                    } else {jsonObject.addProperty("imageFront", Base64.encodeToString(imageFront, Base64.DEFAULT));}
                    if (imageBack == null) {
                        Common.showToast(activity, R.string.textImageIsInvalid);
                        return;
                    } else {jsonObject.addProperty("imageBack", Base64.encodeToString(imageBack, Base64.DEFAULT));}
                    int count = 0;
                    try {
                        String result = new CommonTask(url, jsonObject.toString()).execute().get();
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

        Button btIdFrontAdd = view.findViewById(R.id.btIdFrontAdd);
        btIdFrontAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                action = 1;
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
                                takePicture("IdFront.jpg",fileIdFront);
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

        Button btIdBackAdd = view.findViewById(R.id.btIdBackAdd);
        btIdBackAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                action = 2;
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
                                takePicture("idBack.jpg", fileIdBack);
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

    private void takePicture(String filename, File myFile){
        // 這行就是利用intent去開啟Android的照相機介面，再然後拍完照，即呼叫onActivityResult
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 取得外部儲存資源
        File dir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (dir != null && !dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e(TAG, getString(R.string.textDirNotCreated));
                return;
            }
        }
        myFile = new File(dir, filename); // 要存檔的路徑
        contentUri = FileProvider.getUriForFile(
                activity, activity.getPackageName() + ".provider", myFile);
        // 新增一張照片，在開啟Android的照相機介面時，把這張照片指定為輸出檔案位置。
        // 將uri存入，MediaStore.EXTRA_OUTPUT是指定存储Uri的键，通過傳送一個EXTRA_OUTPUT的Extra給Intent，指定儲存圖像的路徑。
        intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);

        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            // 呼叫 onActivityResult()
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

        if (resultCode == RESULT_OK) { // 若操作成功
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
                    // 顯示縮圖
                    try {
                        // 顯示縮圖
                        Bitmap picture = BitmapFactory.decodeStream(
                                activity.getContentResolver().openInputStream(croppedImageUri));
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        picture.compress(Bitmap.CompressFormat.JPEG, 100, out);

                        if ( action == 1 ) {
                            ivIdFront.setImageBitmap(picture);
                            imageFront = out.toByteArray();
                        } else if ( action == 2 ){
                            ivIdBack.setImageBitmap(picture);
                            imageBack = out.toByteArray();
                        } else {
                            showToast(activity, R.string.textNoCameraApp);
                            return;
                        }
                        // 轉成可回傳資料型態
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
        // take care of exceptions
        try {
            // call the standard crop action intent (the user device may not support it)
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // the recipient of this Intent can read soruceImageUri's data
            cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            // set image source Uri and type
            cropIntent.setDataAndType(sourceImageUri, "image/*");
            // send crop message
            cropIntent.putExtra("crop", "true");
            // aspect ratio of the cropped area, 0 means user define
            cropIntent.putExtra("aspectX", 0); // this sets the max width
            cropIntent.putExtra("aspectY", 0); // this sets the max height
            // output with and height, 0 keeps original size
            cropIntent.putExtra("outputX", 0);
            cropIntent.putExtra("outputY", 0);
            // whether keep original aspect ratio
            cropIntent.putExtra("scale", true);
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, croppedImageUri);
            // whether return data by the intent
            cropIntent.putExtra("return-data", true);
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, REQ_CROP_PICTURE);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException e) {
            showToast(activity, R.string.textNoImageCropAppFound);
        }
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
