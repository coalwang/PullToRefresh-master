package com.cola.CustomView.fresco;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;

public class FrescoUtil {

    public static final String TAG = "FrescoUtil";

    /**
     * 获取bitmap，获取SimpleDraweeView加载的bitmap，通过subscribe方法返回一个bitmap
     * 即网络请求或是缓存请求或是硬盘请求
     */
//    public static void getBitmap(Context context, String url, final OnGetBitmapCallback callback) {
//        if (url == null) {
//            if (callback != null)
//                callback.onFailure();
//            return;
//        }
//        // 切换免流
//        url = "xxxxxxxxxxxxxxxxxxxxxxxxx"
//        // #########################
//        ImagePipeline imagePipeline = Fresco.getImagePipeline();
//        ImageRequest imageRequest = ImageRequestBuilder
//                .newBuilderWithSource(Uri.parse(url))
//                .setRequestPriority(Priority.HIGH)
//                .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
//                .build();
//        DataSource<CloseableReference<CloseableImage>> dataSource =
//                imagePipeline.fetchDecodedImage(imageRequest, context);
//        通过subscribe方法返回一个bitmap
//        dataSource.subscribe(new BaseBitmapDataSubscriber() {
//            @Override
//            public void onNewResultImpl(Bitmap bitmap) {
//                PalLog.v(TAG, "onNewResultImpl");
//                if (callback == null) {
//                    return;
//                }
//
//                if (bitmap == null) {
//                    PalLog.d(TAG, "Bitmap data source returned success, but bitmap null.");
//                    callback.onFailure();
//                    return;
//                }
//                callback.onSuccess(bitmap);
//            }
//
//            @Override
//            public void onFailureImpl(DataSource dataSource) {
//                PalLog.v(TAG, "onFailureImpl");
//                if (callback == null) {
//                    return;
//                }
//                callback.onFailure();
//            }
//        }, CallerThreadExecutor.getInstance());
//    }
//
//    public interface OnGetBitmapCallback {
//        void onSuccess(Bitmap bitmap);
//
//        void onFailure();
//    }
//
//
//    /**
//     * 展示图片
//     * 直接使用uri，或者将uri进行其他处理
//     * @param imageView
//     * @param url
//     */
//    public static void setImageURI(SimpleDraweeView imageView, String url) {
//        //
//        if (TextUtils.isEmpty(url) == false) {
//            imageView.setImageURI(url);
//        } else {
//            imageView.setImageURI("");
//        }
//    }
//
//    /**
//     * 展示图片
//     *
//     * @param path
//     * @param draweeView
//     */
//    public static void displayImage(String path, SimpleDraweeView draweeView) {

//        // #########################
//        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(path))
//                .build();
//        DraweeController controller = Fresco.newDraweeControllerBuilder()
//                .setImageRequest(request)
//                .build();
//        draweeView.setController(controller);
//    }
//
//    /**
//     * 显示图片，支持gif,
//     * 不裁剪
//     *
//     * @param imageView
//     * @param url
//     */
//    public static void displayImage(SimpleDraweeView imageView, String url) {
//        // #########################
//        if (!TextUtils.isEmpty(url)) {
//            Uri uri = Uri.parse(url);
//            DraweeController draweeController =
//                    Fresco.newDraweeControllerBuilder()
//                            .setUri(uri)
//                            .setAutoPlayAnimations(true) // 设置加载图片完成后是否直接进行播放
//                            .build();
//            imageView.setController(draweeController);
//        } else {
//            imageView.setImageURI(Uri.parse(""));
//        }
//    }
//
//    /**
//     * 显示图片，支持gif,
//     * 不裁剪
//     *
//     * @param imageView
//     * @param url
//     */
//    public static void displayImage(final SimpleDraweeView imageView, String url, final String origUrl) {
//        // #########################
//        if (!TextUtils.isEmpty(url)) {
//            Uri uri = Uri.parse(url);
//            DraweeController draweeController =
//                    Fresco.newDraweeControllerBuilder()
//                            .setUri(uri)
//                            .setControllerListener(new BaseControllerListener<ImageInfo>() {
//                                @Override
//                                public void onFailure(String id, Throwable throwable) {
//                                    super.onFailure(id, throwable);
//                                    if (!TextUtils.isEmpty(origUrl)) {
//                                        imageView.setImageURI(origUrl);
//                                    }
//                                }
//                            })
//                            .setAutoPlayAnimations(true) // 设置加载图片完成后是否直接进行播放
//                            .build();
//            imageView.setController(draweeController);
//        } else {
//            imageView.setImageURI(Uri.parse(""));
//        }
//    }
//
//
//    /**
//     * @param path
//     * @param draweeView
//     * @param resizeOptions
//     */
//    public static void displayImage(String path, SimpleDraweeView draweeView, ResizeOptions resizeOptions) {
//        // #########################
//        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(path))
//                .setResizeOptions(resizeOptions)
//                .build();
//        DraweeController controller = Fresco.newDraweeControllerBuilder()
//                .setImageRequest(request)
//                .build();
//        draweeView.setController(controller);
//    }
//
//    /**
//     * @param path
//     * @param draweeView
//     * @param resizeOptions
//     * @param listener
//     */
//    public static void displayImage(String path, SimpleDraweeView draweeView, ResizeOptions resizeOptions, ControllerListener listener) {
//        // #########################
//        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(path))
//                .setResizeOptions(resizeOptions)
//                .build();
//        DraweeController controller = Fresco.newDraweeControllerBuilder()
//                .setImageRequest(request)
//                .setControllerListener(listener)
//                .setAutoPlayAnimations(true)
//                .build();
//        draweeView.setController(controller);
//    }
//
//    /**
//     * @param path
//     * @param draweeView
//     * @param resizeOptions
//     * @param processor
//     */
//    public static void displayImage(String path, SimpleDraweeView draweeView, ResizeOptions resizeOptions, Postprocessor processor) {
//        // #########################
//        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(path))
//                .setPostprocessor(processor)
//                .setResizeOptions(resizeOptions)
//                .build();
//        PipelineDraweeController controller =
//                (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
//                        .setImageRequest(request)
//                        .setOldController(draweeView.getController())
//                        .build();
//        draweeView.setController(controller);
//    }
//
//
//    /**
//     * 显示图片，支持gif
//     *
//     * @param imageView
//     * @param url
//     * @param width
//     * @param height
//     */
//    public static void displayImage(SimpleDraweeView imageView, String url, int width, int height) {

//        // #########################
//        if (!TextUtils.isEmpty(url)) {
//
//            if (url.endsWith(".gif")) {
//                Uri uri = Uri.parse(url);
//                DraweeController draweeController =
//                        Fresco.newDraweeControllerBuilder()
//                                .setUri(uri)
//                                .setAutoPlayAnimations(true) // 设置加载图片完成后是否直接进行播放
//                                .build();
//                imageView.setController(draweeController);
//            } else {
//                url = ImageUtil.getUrl(url, width, height);  根据width和height来对链接进行变换
//                imageView.setImageURI(url);
//            }
//        } else {
//            imageView.setImageURI(Uri.parse(""));
//        }
//    }
}
