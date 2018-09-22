package com.yjz.imageSelector.sample;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yjz.imageSelector.utils.ImageLoader;

import java.io.File;

/**
 * @author lizheng
 * @date created at 2018/9/21 下午5:13
 */
public class GlideImageLoader implements ImageLoader {

    @Override
    public void displayImage(Context context, String path, ImageView imageView) {

        Glide.with(context).load(new File(path))
                .error(R.drawable.default_error)
                .placeholder(R.drawable.default_error)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(imageView);

    }

    /**
     * 显示预览大图
     * @param context
     * @param path
     * @param imageView
     */
    @Override
    public void displayPreviewImage(Context context, String path, ImageView imageView) {
        Glide.with(context).load(new File(path))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }
}
