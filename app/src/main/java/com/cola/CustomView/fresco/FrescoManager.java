package com.cola.CustomView.fresco;

public class FrescoManager {

    //加载网络资源
    //Uri uri = Uri.parse("图片的网络url");
    //mDraweeView.setImageURI(uri);

    //显示图片呈圆角的，可以通过以下设置
    //public static void setRoundRadius(FrescoImageView iv, float radius) {
    //    RoundingParams params = new RoundingParams();
    //    params.setCornersRadius(radius);
    //    iv.getHierarchy().setRoundingParams(params);
    //}

    //有些时候，我们需要设置默认显示的图片，
    //以便于没有图片或加载错误时显示，
    //默认不设置failureImage时，
    //加载失败图片与默认图片一致
    //public static void setFrescoParam(FrescoImageView iv, int defaultImg,ScaleType scalType) {
    //    GenericDraweeHierarchy mHierarchy = iv.getHierarchy();
    //    mHierarchy.setActualImageScaleType(scalType);
    //    mHierarchy.setPlaceholderImage(defaultImg);
    //}

    //设置宽高比例
    //mDraweeView.setAspectRatio(1.3);

    //设置动态显示，Fresco很好的支持了Gif图片的显示
    //ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url)).build();
    //DraweeController controller =
    //Fresco.newDraweeControllerBuilder().setImageRequest(request).setAutoPlayAnimations(true).build();
    //fiv.setController(controller);

    //在你的application类onCreate方法中添加初始化
//    initFresco
//    ///////////////设置Fresco////////////////////////////////
//    private void initFresco() {
//        String path = null;
//        if (android.os.Build.VERSION.SDK_INT < 8) {
//            path = Environment.getExternalStorageDirectory().toString() + "/wk/application/";
//        } else {
//            File externalCacheDir = mContext.getExternalCacheDir();
//            if (externalCacheDir != null) {//这里会出现null的情况
//                path = externalCacheDir.getAbsolutePath();
//            } else {
//                path = Environment.getExternalStorageDirectory().toString() + "/wk/application/";
//            }
//        }
//        MemoryCacheParams 为fresco中的类，内存缓存配置参数
//        可以使用默认的，也可以使用自己配置的
//        final MemoryCacheParams memoryCacheParams = new MemoryCacheParams(15 * 1024 * KB, Integer.MAX_VALUE, 15 * 1024 * KB, Integer.MAX_VALUE, Integer.MAX_VALUE);
//        Supplier<MemoryCacheParams> mSupplierMemoryCacheParams = new Supplier<MemoryCacheParams>() {
//            @Override
//            public MemoryCacheParams get() {
//                return memoryCacheParams;
//            }
//        };
//        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this)
//                .setMainDiskCacheConfig(DiskCacheConfig.newBuilder(getApplicationContext())
//                        .setBaseDirectoryPath(new File(path))
//                        .setBaseDirectoryName("TMP_IMG_DIR")
//                        .setMaxCacheSize(15 * 1024 * KB)//默认缓存的最大大小。
//                        .setMaxCacheSizeOnLowDiskSpace(15 * 1024 * KB)//缓存的最大大小,使用设备时低磁盘空间。
//                        .setMaxCacheSizeOnVeryLowDiskSpace(15 * 1024 * KB)//缓存的最大大小,当设备极低磁盘空间
//                        .build())
//                .setProgressiveJpegConfig(new ProgressiveJpegConfig() {
//                    @Override
//                    public int getNextScanNumberToDecode(int i) {
//                        return i + 3;
//                    }
//
//                    @Override
//                    public QualityInfo getQualityInfo(int i) {
//                        boolean isGoodEnough = (i >= 2);
//                        return ImmutableQualityInfo.of(i, isGoodEnough, false);
//                    }
//                })
//                .setDownsampleEnabled(true)
//                .setBitmapMemoryCacheParamsSupplier(mSupplierMemoryCacheParams)
//                .build();
//        Fresco.initialize(this, config);  fresco初始化的时候可以初始化config（配置）
//    }

//    fresco是通过控件来实现它内部的优化缓存处理，我们使用的时候是通过控件来使用，具体如下
//    <com.facebook.drawee.view.SimpleDraweeView
//    android:id="@+id/user_avator"
//    不支持wrap_content 如果要设置宽高比, 需要在Java代码中指定setAspectRatio(1.33f);
//    android:layout_width="50dp"
//     不支持wrap_content
//    android:layout_height="50dp"
//    fresco:roundAsCircle="true"
//    fresco:roundedCornerRadius="180dp"
//    设置图片缩放. 通常使用focusCrop,该属性值会通过算法把人头像放在中间
//    fresco:actualImageScaleType="focusCrop"
//    下载成功之前显示的图片
//    fresco:placeholderImage="@color/wait_color"
//    fresco:failureImage="@drawable/error"  加载失败的时候显示的图片
//    fresco:retryImage="@drawable/retrying"加载失败,提示用户点击重新加载的图片(会覆盖failureImage的图片)
//    fresco:progressBarImage="@drawable/progress_bar"// 提示用户正在加载,和加载进度无关
//    fresco:roundAsCircle="false" 是不是设置圆圈
//    fresco:roundedCornerRadius="1dp" 圆角角度,180的时候会变成圆形图片
//    android:layout_centerVertical="true" />
//    大家可能看到，属性中存在fresco:开头的声明。
//    这个是fresco的自定义属性，如果我们需要使用其自定义属性，
//    必须在我们的xml根布局中添加声明/命名空间，下图红色部分
//    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
//    xmlns:fresco="http://schemas.android.com/apk/res-auto"
//    android:layout_width="match_parent"
//    android:layout_height="match_parent"
//    android:orientation="vertical">

//    修改图片尺寸
//    Uri uri = "file:///mnt/sdcard/MyApp/myfile.jpg";
//    int width = 50, height = 50;
//    ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
//            .setResizeOptions(new ResizeOptions(width, height))
//            .build();
//    PipelineDraweeController controller = Fresco.newDraweeControllerBuilder()
//            .setOldController(mDraweeView.getController())
//            .setImageRequest(request)
//            .build();
//    mSimpleDraweeView.setController(controller);

//    自动旋转
//    ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
//            .setAutoRotateEnabled(true)
//            .build();
    }
