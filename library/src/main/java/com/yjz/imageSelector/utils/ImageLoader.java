package com.yjz.imageSelector.utils;

import android.content.Context;
import android.widget.ImageView;

/**
 * 外部实现这个类去加载图片,自定义图片加载框架
 * @author lizheng
 * created at 2018/9/21 下午5:32
 */

public interface ImageLoader {

    void displayImage(Context context, String path, ImageView imageView);

    void displayPreviewImage(Context context, String path, ImageView imageView);
}
