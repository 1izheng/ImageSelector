package com.yjz.imageSelector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yjz.imageSelector.view.ViewPagerFixed;

import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;

/**
 * 图片预览
 *
 * @author lizheng
 *         created at 2018/7/25 下午3:22
 */

public class PreviewImagesActivity extends AppCompatActivity {
    ViewPagerFixed pager;
    MyPagerAdapter adapter;
    private ArrayList<String> picList = new ArrayList<>();
    private TextView tvCancel, tvSend, tvIndex;

    public static void preViewSingle(Activity activity, String url, int requestCode) {
        Intent intent = new Intent(activity, PreviewImagesActivity.class);
        ArrayList<String> pic = new ArrayList<>();
        pic.add(url);
        intent.putExtra("pics", pic);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_pictures);

        pager = findViewById(R.id.pager);
        tvCancel = findViewById(R.id.tv_cancel);
        tvSend = findViewById(R.id.tv_send);
        tvIndex = findViewById(R.id.tv_index);


        picList = getIntent().getStringArrayListExtra("pics");
        if (picList != null && picList.size() != 0) {
            adapter = new MyPagerAdapter();
            pager.setAdapter(adapter);
        }

        tvIndex.setText(1 + "/" + picList.size());

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tvSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("pics", picList);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                tvIndex.setText(position + 1 + "/" + picList.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private class MyPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return picList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view.equals(o);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            PhotoView photoView = new PhotoView(PreviewImagesActivity.this);
            ImageSelector.getInstance().getImageLoader().displayPreviewImage(PreviewImagesActivity.this,picList.get(position),photoView);
            container.addView(photoView);
            return photoView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

}
