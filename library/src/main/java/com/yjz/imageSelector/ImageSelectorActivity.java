package com.yjz.imageSelector;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;

import com.yjz.imageSelector.adapter.FolderAdapter;
import com.yjz.imageSelector.adapter.ImageRecyAdapter;
import com.yjz.imageSelector.bean.Folder;
import com.yjz.imageSelector.bean.Image;
import com.yjz.imageSelector.utils.FileUtils;
import com.yjz.imageSelector.utils.ImageDataSource;
import com.yjz.imageSelector.view.FolderPopUpWindow;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 多图选择
 */
public class ImageSelectorActivity extends FragmentActivity implements View.OnClickListener, ImageDataSource.OnImagesLoadedListener, ImageRecyAdapter.OnImageItemClickListener {

    public static final int REQUEST_PERMISSION_STORAGE = 0x01;
    public static final int REQUEST_PERMISSION_CAMERA = 0x02;
    /**
     * 是否需要裁剪
     */
    public static boolean isCrop = true;
    /**
     * 裁剪形状
     */
    public static boolean cropCircle = false;

    /**
     * 裁剪框宽高比
     */
    public static float whRatio = 1.0f;
    /**
     * 必选数量
     */
    private int mustCount;
    /**
     * 最大选择数量
     */
    private int maxSelectCount;
    /**
     * 是否直接拍照
     */
    private boolean defaultStartCamera = false;
    /**
     * 当前模式
     */
    private int currentMode;
    /**
     * 是否在第一格显示相机
     */
    private boolean mIsShowCamera = false;

    /**
     * 拍照返回
     */
    private static final int REQUEST_CAMERA = 100;
    /**
     * 预览页
     */
    private static final int REQUEST_PREVIEW = 101;
    /**
     * 裁剪成功返回
     */
    private static final int REQUEST_CROP = 103;

    private RecyclerView mRecyclerView;
    /**
     * 文件夹名称
     */
    private Button btnFolderName;
    /**
     * 预览按钮
     */
    private Button btnPreview;
    // 底部View
    private View mPopupAnchorView;
    // 确定
    private Button mSubmitButton;

    /**
     * 选择结果
     */
    private ArrayList<String> resultList;

    // 文件夹数据
    private ArrayList<Folder> mResultFolder = new ArrayList<>();

    private ImageRecyAdapter mImageRecyAdapter;

    private FolderAdapter mFolderAdapter;
    private FolderPopUpWindow mFolderPopupWindow;


    //拍照保存的临时文件
    private File camearFile;


    /**
     * @param activity
     * @param requestCode
     * @param maxNum
     * @param selectedMode
     * @param defaultStartCamera
     * @param isCrop
     * @param showCamera
     * @param resultList
     */
    public static void startSelect(Activity activity, int requestCode, int maxNum, int selectedMode, boolean defaultStartCamera, boolean isCrop, boolean cropCircle, float whRatio, boolean showCamera, ArrayList<String> resultList) {
        Intent intent = new Intent(activity, ImageSelectorActivity.class);
        // 最大可选择图片数量
        intent.putExtra(ImageSelector.EXTRA_SELECT_COUNT, maxNum);
        // 选择模式
        intent.putExtra(ImageSelector.EXTRA_SELECT_MODE, selectedMode);
        //是否直接开始拍照
        intent.putExtra(ImageSelector.DEFAULT_START_CAMERA, defaultStartCamera);
        //是否裁剪
        intent.putExtra(ImageSelector.EXTRA_DEFAULT_CROP, isCrop);
        //裁剪形状
        intent.putExtra(ImageSelector.EXTRA_CROP_CIRCLE, cropCircle);
        //裁剪比例
        intent.putExtra(ImageSelector.EXTRA_CROP_RATIO, whRatio);
        //是否显示相机
        intent.putExtra(ImageSelector.EXTRA_SHOW_CAMERA, showCamera);
        //已经选择的图片
        intent.putStringArrayListExtra(ImageSelector.EXTRA_DEFAULT_SELECTED_LIST, resultList);
        activity.startActivityForResult(intent, requestCode);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_selector_z);
        initIntent();
        if (defaultStartCamera) {
            if (!checkPermission(Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA);
            } else {
                showCameraAction();
            }
        }
        initView();
        initData();
    }

    private void initIntent() {
        Intent intent = getIntent();
        maxSelectCount = intent.getIntExtra(ImageSelector.EXTRA_SELECT_COUNT, 9);
        mustCount = intent.getIntExtra(ImageSelector.MUST_COUNT, 1);
        currentMode = intent.getIntExtra(ImageSelector.EXTRA_SELECT_MODE, ImageSelector.MODE_MULTI);
        mIsShowCamera = intent.getBooleanExtra(ImageSelector.EXTRA_SHOW_CAMERA, true);
        isCrop = intent.getBooleanExtra(ImageSelector.EXTRA_DEFAULT_CROP, true);
        cropCircle = intent.getBooleanExtra(ImageSelector.EXTRA_CROP_CIRCLE, false);
        whRatio = intent.getFloatExtra(ImageSelector.EXTRA_CROP_RATIO, 1.0f);

        resultList = intent.getStringArrayListExtra(ImageSelector.EXTRA_DEFAULT_SELECTED_LIST);
        if (resultList == null) {
            resultList = new ArrayList<>();
        }
        if (currentMode == ImageSelector.MODE_SINGLE) {
            //单选
            maxSelectCount = 1;
        }
        defaultStartCamera = getIntent().getBooleanExtra(ImageSelector.DEFAULT_START_CAMERA, false);
    }

    private void initView() {
        findViewById(R.id.btn_back).setOnClickListener(this);
        mSubmitButton = findViewById(R.id.btn_commit);
        mSubmitButton.setOnClickListener(this);
        mPopupAnchorView = findViewById(R.id.footer);
        btnFolderName = findViewById(R.id.btn_folder_name);
        // 初始化，加载所有图片
        btnFolderName.setText(R.string.folder_all);
        btnFolderName.setOnClickListener(this);
        btnPreview = findViewById(R.id.btn_preview);
        btnPreview.setOnClickListener(this);
        //初始化完成和预览
        onImageSelected();
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mImageRecyAdapter = new ImageRecyAdapter(this, currentMode == ImageSelector.MODE_SINGLE, mIsShowCamera, maxSelectCount);
        mImageRecyAdapter.setOnImageItemClickListener(this);
        mRecyclerView.setAdapter(mImageRecyAdapter);
        mFolderAdapter = new FolderAdapter(this, null);
    }


    /**
     * 首次加载所有图片
     */
    private void initData() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new ImageDataSource(ImageSelectorActivity.this, this);
            } else {
                ActivityCompat.requestPermissions(ImageSelectorActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_STORAGE);
            }
        } else {
            new ImageDataSource(ImageSelectorActivity.this, this);
        }
    }

    /**
     * 检查权限
     *
     * @param permission
     * @return
     */
    public boolean checkPermission(@NonNull String permission) {
        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new ImageDataSource(this, this);
            } else {
                Toast.makeText(getApplicationContext(), "权限被禁止，无法选择本地图片", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_PERMISSION_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //调用相机
                showCameraAction();
            } else {
                Toast.makeText(getApplicationContext(), "权限被禁止，无法打开相机", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * 创建弹出的ListView popup
     */
    private void createPopupFolderList() {
        mFolderPopupWindow = new FolderPopUpWindow(this, mFolderAdapter);
        mFolderPopupWindow.setOnItemClickListener(new FolderPopUpWindow.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                mFolderAdapter.setSelectIndex(position);
                mFolderPopupWindow.dismiss();
                Folder folder = (Folder) adapterView.getAdapter().getItem(position);
                if (null != folder) {
                    mImageRecyAdapter.setData(folder.images);
                    btnFolderName.setText(folder.name);
                }
            }
        });
        mFolderPopupWindow.setMargin(mPopupAnchorView.getHeight());
    }

    /**
     * 选择后返回
     */
    public void finishSelect() {
        Intent data = new Intent();
        data.putStringArrayListExtra(ImageSelector.EXTRA_RESULT, resultList);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 单选后返回
     *
     * @param path
     */
    public void onSingleImageSelected(String path) {
        Intent data = new Intent();
        resultList.add(path);
        data.putStringArrayListExtra(ImageSelector.EXTRA_RESULT, resultList);
        setResult(RESULT_OK, data);
        finish();
    }

    private void setImageSelected(String path) {
        if (resultList.contains(path)) {
            resultList.remove(path);
            onImageUnselected();
        } else {
            // 判断选择数量问题
            if (maxSelectCount == resultList.size()) {
                Toast.makeText(this, R.string.msg_amount_limit, Toast.LENGTH_SHORT).show();
                return;
            }
            resultList.add(path);
            onImageSelected();
        }
    }

    public void onImageSelected() {
        // 有图片之后，改变按钮状态
        if (resultList.size() > 0) {
            mSubmitButton.setText(getResources().getString(R.string.confirm) + "(" + resultList.size() + "/" + maxSelectCount + ")");
            btnPreview.setText(getResources().getString(R.string.preview) + "(" + resultList.size() + ")");
            if (mustCount != 0) {
                if (resultList.size() < mustCount) {
                    mSubmitButton.setEnabled(false);
                } else {
                    mSubmitButton.setEnabled(true);
                }
            } else {
                mSubmitButton.setEnabled(true);
            }
        } else {
            mSubmitButton.setText(getResources().getString(R.string.confirm));
            btnPreview.setText(R.string.preview);
            mSubmitButton.setEnabled(true);
        }
    }

    public void onImageUnselected() {
        mSubmitButton.setText(getResources().getString(R.string.confirm) + "(" + resultList.size() + "/" + maxSelectCount + ")");
        btnPreview.setText(getResources().getString(R.string.preview) + "(" + resultList.size() + ")");
        // 当为选择图片时候的状态
        if (resultList.size() == 0) {
            mSubmitButton.setText(getResources().getString(R.string.confirm));
            btnPreview.setText(R.string.preview);
            mSubmitButton.setEnabled(false);
        }
    }

    public void onCameraShot(File imageFile) {
        if (imageFile != null) {
            // 刷新系统相册
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)));

            Intent data = new Intent();
            resultList.add(imageFile.getAbsolutePath());
            data.putStringArrayListExtra(ImageSelector.EXTRA_RESULT, resultList);
            setResult(RESULT_OK, data);
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 相机拍照完成后，返回图片路径
        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == RESULT_OK) {
                //是否裁剪
                if (isCrop) {
                    //去裁剪
                    Intent intent = new Intent(this, ImageCropActivity.class);
                    intent.putExtra(ImageCropActivity.IMAGE_PATH, camearFile.getAbsolutePath());
                    intent.putExtra(ImageCropActivity.CROP_CIRCLE, cropCircle);
                    intent.putExtra(ImageCropActivity.W_H_RATIO, whRatio);
                    startActivityForResult(intent, REQUEST_CROP);
                } else {
                    if (camearFile != null) {
                        onCameraShot(camearFile);
                    }
                }
            } else {
                if (camearFile != null && camearFile.exists()) {
                    camearFile.delete();
                }
                if (defaultStartCamera) {
                    finish();
                }
            }
        } else if (requestCode == REQUEST_CROP && resultCode == RESULT_OK) {
            String path = data.getStringExtra(ImageSelector.EXTRA_RESULT);
            Intent intent = new Intent();
            resultList.add(path);
            intent.putStringArrayListExtra(ImageSelector.EXTRA_RESULT, resultList);
            setResult(RESULT_OK, intent);
            finish();
        } else if (requestCode == REQUEST_PREVIEW && resultCode == RESULT_OK) {
            finishSelect();
        }
    }

    /**
     * 调用相机拍照
     */
    private void showCameraAction() {
        // 跳转到系统照相机
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            camearFile = FileUtils.createTmpFile(this);
            if (camearFile != null) {
                Uri uri;
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                    uri = Uri.fromFile(camearFile);
                } else {
                    /**
                     * 7.0 调用系统相机拍照不再允许使用Uri方式，应该替换为FileProvider
                     */
                    uri = FileProvider.getUriForFile(this, FileUtils.getFileProviderName(this), camearFile);
                    //加入uri权限 要不三星手机不能拍照
                    List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                }
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            }
            startActivityForResult(cameraIntent, REQUEST_CAMERA);

        }
    }

    /**
     * 选择图片操作
     *
     * @param image
     */
    private void selectImageFromGrid(Image image, int mode) {
        if (image != null) {
            // 多选模式
            if (mode == ImageSelector.MODE_MULTI) {
                setImageSelected(image.path);
            } else if (mode == ImageSelector.MODE_SINGLE) {
                //单选模式 判断是否需要裁剪
                if (isCrop) {
                    //去裁剪
                    Intent intent = new Intent(this, ImageCropActivity.class);
                    intent.putExtra(ImageCropActivity.IMAGE_PATH, image.path);
                    intent.putExtra(ImageCropActivity.CROP_CIRCLE, cropCircle);
                    intent.putExtra(ImageCropActivity.W_H_RATIO, whRatio);
                    startActivityForResult(intent, REQUEST_CROP);
                } else {
                    //不裁剪
                    onSingleImageSelected(image.path);
                }
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //简单处理旋转屏幕..
        if (mFolderPopupWindow != null) {
            if (mFolderPopupWindow.isShowing()) {
                mFolderPopupWindow.dismiss();
            }
        }
        super.onConfigurationChanged(newConfig);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_back) {
            setResult(RESULT_CANCELED);
            finish();
        } else if (id == R.id.btn_commit) {
            if (resultList != null && resultList.size() > 0) {
                // 返回已选择的图片数据
                finishSelect();
            }
        } else if (id == R.id.btn_preview) {
            if (resultList.size() != 0) {
                Intent intent = new Intent(this, PreviewImagesActivity.class);
                intent.putExtra("pics", resultList);
                startActivityForResult(intent, REQUEST_PREVIEW);
            }
        } else if (id == R.id.btn_folder_name) {
            //创建popup
            createPopupFolderList();

            if (mFolderPopupWindow.isShowing()) {
                mFolderPopupWindow.dismiss();
            } else {
                mFolderPopupWindow.showAtLocation(mPopupAnchorView, Gravity.NO_GRAVITY, 0, 0);
                int index = mFolderAdapter.getSelectIndex();
                index = index == 0 ? index : index - 1;
                mFolderPopupWindow.setSelection(index);
            }

        }
    }

    @Override
    public void onImagesLoaded(ArrayList<Folder> imageFolders, ArrayList<Image> images) {
        this.mResultFolder = imageFolders;

        mImageRecyAdapter.setData(images);
        // 设定默认选择
        if (resultList != null && resultList.size() > 0) {
            mImageRecyAdapter.setDefaultSelected(resultList);
        }
        mFolderAdapter.setData(mResultFolder);
    }


    @Override
    public void onImageItemClick(Image image, int position) {
        selectImageFromGrid(image, currentMode);
    }

    @Override
    public void onCameraItemClick() {
        if (!checkPermission(Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA);
        } else {
            //调用相机拍照
            showCameraAction();
        }
    }


}
