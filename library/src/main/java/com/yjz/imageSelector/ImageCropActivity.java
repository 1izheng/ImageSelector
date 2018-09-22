package com.yjz.imageSelector;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;

import com.yjz.imageSelector.utils.FileUtils;
import com.yjz.imageSelector.utils.ScreenUtils;
import com.yjz.imageSelector.view.CropImageView;

import java.io.File;

/**
 * 图片裁剪
 *
 * @author lizheng
 *         created at 2018/9/20 上午11:46
 */

public class ImageCropActivity extends FragmentActivity implements View.OnClickListener, CropImageView.OnBitmapSaveCompleteListener {

    private CropImageView mCropImageView;
    private Bitmap mBitmap;
    private boolean mIsSaveRectangle;
    private int mOutputX;
    private int mOutputY;
    public static final String IMAGE_PATH = "imagePath";
    public static final String CROP_CIRCLE = "cropCircle";
    public static final String W_H_RATIO = "whRatio";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_crop);

        //初始化View
        findViewById(R.id.btn_back).setOnClickListener(this);
        Button btnCommit = (Button) findViewById(R.id.btn_commit);
        btnCommit.setOnClickListener(this);
        mCropImageView = (CropImageView) findViewById(R.id.cv_crop_image);
        mCropImageView.setOnBitmapSaveCompleteListener(this);

        String imagePath = getIntent().getStringExtra(IMAGE_PATH);
        boolean cropCircle = getIntent().getBooleanExtra(CROP_CIRCLE, false);
        float whRatio = getIntent().getFloatExtra(W_H_RATIO, 1.0f);

        //裁剪保存的宽度
        int width = ScreenUtils.getScreenWidth(this);
        int height = whRatio == 1 ? width : (int) (width * whRatio);

        //裁剪保存的宽高
        mOutputX = width;
        mOutputY = height;

        mIsSaveRectangle = false;

        //裁剪框形状
        if (cropCircle) {
            mCropImageView.setFocusStyle(CropImageView.Style.CIRCLE);
            //如果是圆形,比例1:1
            height = width;
        } else {
            mCropImageView.setFocusStyle(CropImageView.Style.RECTANGLE);
        }
        //方框的宽
        mCropImageView.setFocusWidth(width);
        //方框的高
        mCropImageView.setFocusHeight(height);


        //缩放图片
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        options.inSampleSize = calculateInSampleSize(options, displayMetrics.widthPixels, displayMetrics.heightPixels);
        options.inJustDecodeBounds = false;
        mBitmap = BitmapFactory.decodeFile(imagePath, options);
        //设置默认旋转角度
        mCropImageView.setImageBitmap(mCropImageView.rotate(mBitmap, FileUtils.getBitmapDegree(imagePath)));
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int width = options.outWidth;
        int height = options.outHeight;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = width / reqWidth;
            } else {
                inSampleSize = height / reqHeight;
            }
        }
        return inSampleSize;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_back) {
            setResult(RESULT_CANCELED);
            finish();
        } else if (id == R.id.btn_commit) {
            mCropImageView.saveBitmapToFile(FileUtils.getCropCacheFile(this), mOutputX, mOutputY, mIsSaveRectangle);
        }
    }

    @Override
    public void onBitmapSaveSuccess(File file) {
        Intent intent = new Intent();
        intent.putExtra(ImageSelector.EXTRA_RESULT, file.getAbsolutePath());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBitmapSaveError(File file) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCropImageView.setOnBitmapSaveCompleteListener(null);
        if (null != mBitmap && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }
}
