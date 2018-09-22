package com.yjz.imageSelector.utils;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;

import com.yjz.imageSelector.bean.Folder;
import com.yjz.imageSelector.bean.Image;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片数据
 * @author lizheng 
 * created at 2018/9/21 下午4:18
 */

public class ImageDataSource implements LoaderManager.LoaderCallbacks<Cursor> {

    // 不同loader定义
    private static final int LOADER_ALL = 0;
    private static final int LOADER_CATEGORY = 1;
    private final String[] IMAGE_PROJECTION = {
            MediaStore.Images.Media.DATA,         //图片的真实路径  /storage/emulated/0/pp/downloader/wallpaper/aaa.jpg
            MediaStore.Images.Media.DISPLAY_NAME, //图片的显示名称  aaa.jpg
            MediaStore.Images.Media.DATE_ADDED,   //图片被添加的时间，long型  1450518608
            MediaStore.Images.Media._ID};
    // 文件夹数据
    private ArrayList<Folder> imageFolders = new ArrayList<>();

    private FragmentActivity activity;
    private OnImagesLoadedListener loadedListener; //图片加载完成的回调接口


    public ImageDataSource(FragmentActivity activity, OnImagesLoadedListener loadedListener) {
        this.activity = activity;
        this.loadedListener = loadedListener;
        LoaderManager loaderManager = activity.getSupportLoaderManager();
        loaderManager.initLoader(LOADER_ALL, null, this);//加载所有的图片
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //扫描所有文件夹
        if (id == LOADER_ALL) {
            CursorLoader cursorLoader = new CursorLoader(activity,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                    null, null, IMAGE_PROJECTION[2] + " DESC");
            return cursorLoader;
        } else if (id == LOADER_CATEGORY) {
            CursorLoader cursorLoader = new CursorLoader(activity,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                    IMAGE_PROJECTION[0] + " like '%" + args.getString("path") + "%'", null, IMAGE_PROJECTION[2] + " DESC");
            return cursorLoader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        imageFolders.clear();
        if (data != null) {
            ArrayList<Image> allImages = new ArrayList<>(); //所有图片集合,不分文件夹
            int count = data.getCount();
            if (count > 0) {
                while (data.moveToNext()) {
                    //查询
                    String path = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                    String name = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
                    long dateTime = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));

                    //文件是否有效
                    File file = new File(path);
                    if (!file.exists() || file.length() <= 0) {
                        continue;
                    }

                    Image image = null;
                    if (!TextUtils.isEmpty(name)) {
                        image = new Image(path, name, dateTime);
                        allImages.add(image);
                    }

                    // 获取文件夹名称
                    File imageFile = new File(path);
                    File folderFile = imageFile.getParentFile();
                    Folder folder = new Folder();
                    folder.name = folderFile.getName();
                    folder.path = folderFile.getAbsolutePath();
                    //根据父路径分类存放图片
                    if (!imageFolders.contains(folder)) {
                        List<Image> imageList = new ArrayList<>();
                        imageList.add(image);
                        folder.images = imageList;
                        folder.cover = image;
                        imageFolders.add(folder);
                    } else {
                        // 更新
                        Folder f = imageFolders.get(imageFolders.indexOf(folder));
                        f.images.add(image);
                    }
                }
                //防止没有图片报异常
                if (data.getCount() > 0 && allImages.size()>0) {
                    //构造所有图片的集合
                    Folder allImagesFolder = new Folder();
                    allImagesFolder.name = "所有图片";
                    allImagesFolder.path = "/";
                    allImagesFolder.cover = allImages.get(0);
                    allImagesFolder.images = allImages;
                    imageFolders.add(0, allImagesFolder);  //确保第一条是所有图片
                }

                //回调接口,通知加载完毕
                loadedListener.onImagesLoaded(imageFolders,allImages);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }


    /**
     * 所有图片加载完成的回调接口
     */
    public interface OnImagesLoadedListener {
        void onImagesLoaded(ArrayList<Folder> imageFolders, ArrayList<Image> images);
    }
}
