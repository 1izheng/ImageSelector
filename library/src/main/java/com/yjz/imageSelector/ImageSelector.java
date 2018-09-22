package com.yjz.imageSelector;

import android.app.Activity;

import com.yjz.imageSelector.utils.ImageLoader;

import java.util.ArrayList;

/**
 * 图片选择参数构造
 *
 * @author lizheng
 * created at 2018/9/19 下午2:46
 */

public class ImageSelector {

    private static ImageLoader imageLoader;
    private static volatile ImageSelector sInstance;

    private ImageSelector() {
    }

    //单例模式
    public static ImageSelector getInstance() {
        if (sInstance == null) {
            synchronized (ImageSelector.class) {
                if (sInstance == null) {
                    sInstance = new ImageSelector();
                }
            }
        }
        return sInstance;
    }

    /**
     * 提供全局替换图片加载框架的接口，若切换其它框架，可以实现一键全局替换
     */
    public void setImageLoader(ImageLoader loader) {
        if (loader != null) {
            imageLoader = loader;
        }
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }


    /**
     * 选择结果，返回为 ArrayList 图片路径集合
     */
    public static final String EXTRA_RESULT = "select_result";
    /**
     * 必须选择的数量
     */
    public static final String MUST_COUNT = "must_count";
    /**
     * 最大图片选择数，int类型
     */
    public static final String EXTRA_SELECT_COUNT = "max_select_count";
    /**
     * 图片选择模式，int类型
     */
    public static final String EXTRA_SELECT_MODE = "select_count_mode";
    /**
     * 是否显示相机，boolean类型
     */
    public static final String EXTRA_SHOW_CAMERA = "show_camera";
    /**
     * 默认选择的数据集
     */
    public static final String EXTRA_DEFAULT_SELECTED_LIST = "default_result";
    /**
     * 是否裁剪
     */
    public static final String EXTRA_DEFAULT_CROP = "isCrop";

    /**
     * 裁剪形状
     */
    public static final String EXTRA_CROP_CIRCLE = "crop_circle";

    /**
     * 宽高比
     */
    public static final String EXTRA_CROP_RATIO = "crop_wh_ratio";

    /**
     * 是否直接拍照
     */
    public static final String DEFAULT_START_CAMERA = "default_start_camera";

    /**
     * 单选
     */
    public static final int MODE_SINGLE = 0;
    /**
     * 多选
     */
    public static final int MODE_MULTI = 1;

    public static ImageSelectorBuilder builder() {
        return new ImageSelectorBuilder();
    }

    public static class ImageSelectorBuilder {
        private boolean isCrop = true;
        private int mustCount = 1;
        private int maxCount = 9;
        private boolean defaultStartCamera = false;
        private boolean isSingleMode = false;
        private boolean showFirstCamera = true;
        private ArrayList<String> selected;
        private boolean cropCircle = false;
        private float whRatio;


        /**
         * 是否启用裁剪
         *
         * @param crop
         * @return
         */
        public ImageSelectorBuilder setCrop(boolean crop) {
            isCrop = crop;
            return this;
        }

        /**
         * 必须选择的数量
         *
         * @param mustCount
         * @return
         */
        public ImageSelectorBuilder setMustCount(int mustCount) {
            this.mustCount = mustCount;
            return this;
        }

        /**
         * 最大选择数量
         *
         * @param maxCount
         * @return
         */
        public ImageSelectorBuilder setMaxCount(int maxCount) {
            this.maxCount = maxCount;
            return this;
        }

        /**
         * 是否是单选
         *
         * @param singleMode
         * @return
         */
        public ImageSelectorBuilder setSingleMode(boolean singleMode) {
            this.isSingleMode = singleMode;
            return this;
        }

        /**
         * 是否直接打开相机拍照
         *
         * @param defaultStartCamera
         * @return
         */
        public ImageSelectorBuilder setDefaultStartCamera(boolean defaultStartCamera) {
            this.defaultStartCamera = defaultStartCamera;
            return this;
        }

        /**
         * 是否在第一项显示相机,默认显示
         *
         * @param showFirstCamera
         */
        public ImageSelectorBuilder showFirstCamera(boolean showFirstCamera) {
            this.showFirstCamera = showFirstCamera;
            return this;
        }

        /**
         * 接收从外面传进来的已选择的图片列表。当用户原来已经有选择过图片，现在重新打开
         * 选择器，允许用户把先前选过的图片传进来，并把这些图片默认为选中状态。
         *
         * @param selected
         * @return
         */
        public ImageSelectorBuilder setSelected(ArrayList<String> selected) {
            this.selected = selected;
            return this;
        }

        /**
         * 裁剪框形状
         *
         * @param cropCircle 是否圆形
         */
        public ImageSelectorBuilder setCropCircle(boolean cropCircle) {
            this.cropCircle = cropCircle;
            return this;
        }

        /**
         * 设置宽高比
         *
         * @param ratio
         * @return
         */
        public ImageSelectorBuilder setWhRatio(float ratio) {
            this.whRatio = ratio;
            return this;
        }

        /**
         * 起飞
         *
         * @param activity
         * @param requestCode
         */
        public void start(Activity activity, int requestCode) {
            int mode = isSingleMode ? MODE_SINGLE : MODE_MULTI;
            ImageSelectorActivity.startSelect(activity, requestCode, maxCount, mode, defaultStartCamera, isCrop, cropCircle, whRatio, showFirstCamera, selected);
        }


    }
}
