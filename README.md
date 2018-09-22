# ImageSelector
Android图片选择器，仿微信的图片选择器的样式和效果。
支持图片单选、多选、裁剪形状自定义、裁剪比例设置、解耦图片加载框架。


![相册](https://github.com/1izheng/ImageSelector/blob/master/images/111.jpg)  ![文件夹](https://github.com/1izheng/ImageSelector/blob/master/images/222.jpg)  ![预览](https://github.com/1izheng/ImageSelector/blob/master/images/333.jpg)



### 1、引入依赖

build.gradle在添加以下代码

```
compile 'com.yjz:ImageSelector:1.0.1'

```

### 2、配置AndroidManifest.xml

```
//储存卡的读写权限
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
//调用相机权限
<uses-permission android:name="android.permission.CAMERA" />
```

### 3、使用
* 初始化图片加载库

```
项目的Application中设置图片加载库
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //设置图片加载框架
        ImageSelector.getInstance().setImageLoader(new GlideImageLoader());
    }
}
```

* 新建类实现ImageLoader接口

```

//我这里使用Glide库，其他库一样的道理
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


```

* 调用

```
	ImageSelector.builder()
                        .setCrop(true) //是否裁剪 默认false
                        .setSingleMode(true) //是否单选
                        .setCropCircle(false) //是否裁剪圆形  默认true:矩形
                        .setWhRatio(0.5f) //高宽比 0.5 表示 高/宽 = 0.5 默认1.0f即正方形
                        .setDefaultStartCamera(false) //是否直接启动相机
                        .setMaxCount(9) //最大选择数量
                        .setMustCount(1) //必选数量
                        .setSelected(new ArrayList<String>()) //默认选择图片集合
                        .start(MainActivity.this, REQUEST_CODE);

```

### 方法

| **方法名称** | **描述** | **参数类型** | **默认值** |
| --- | ---| --- | --- |
| setCrop | 是否裁剪 | boolean | false |
| setCropCircle | 裁剪形状圆形 | boolean | false(方形） |
| setWhRatio | 高宽比(高/宽) | float | 1.0f |
| setMaxCount | 最大选择数量 | int | 9 |
| setSingleMode | 是否单选模式| boolean | false |
| setDefaultStartCamera | 是否直接拍照 | boolean | false |
| showFirstCamera | 第一个位置是否显示相机 | boolean | true |
| setSelected | 默认选择图片集合 | - | - |



* 接收选择结果

```
@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (data != null && requestCode == REQUEST_CODE) {
                final List<String> images = data.getStringArrayListExtra(ImageSelector.EXTRA_RESULT);
                Toast.makeText(this, "选择数据长度->:" + images.size(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "没有数据", Toast.LENGTH_SHORT).show();
            }
        }
    }

```
