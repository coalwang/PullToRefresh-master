package com.cola.CustomView.view.CustiomView;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.netease.wangkai1.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class AnimationView extends LinearLayout {

    private static final long ANIM_DURATION = 1000;
    private List<View> viewList;
    private int LINE_LONG ;
    private int LINE_SHORT ;
    private int WIDTH;
    private int MARGIN;
    private int color = R.color.mColor;
    private ValueAnimator valueAnimator;

    public AnimationView(Context context) {
        super(context);
        initData();
    }

    public AnimationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initData();
    }
//
//    public AnimationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        initData();
//    }

    private void initData(){
        LINE_LONG = getResources().getDimensionPixelSize(R.dimen.line_height);
        LINE_SHORT = getResources().getDimensionPixelSize(R.dimen.line_short);
        WIDTH = getResources().getDimensionPixelSize(R.dimen.width);
        MARGIN = getResources().getDimensionPixelSize(R.dimen.margin);
        setOrientation(HORIZONTAL);
        setBackgroundColor(getResources().getColor(R.color.colorAccent));
        setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        viewList = new ArrayList<>(4);
        viewList.add(newLineView(LINE_SHORT));
        viewList.add(newLineView(LINE_LONG));
        viewList.add(newLineView(LINE_SHORT));
        viewList.add(newLineView(LINE_LONG));

        for (View view : viewList)
            addView(view);
    }

    private View newLineView(int height){
        View view = new View(getContext());
        view.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        LayoutParams params = new LayoutParams(WIDTH, height);
        params.setMargins(MARGIN, 0, MARGIN, 0);
        view.setLayoutParams(params);
        //setPivotY,设置描点的位置
        //view.setPivotY(height);
        return view;
    }

    private void resetViews() {
        for (View view : viewList)
            view.setScaleY(1);
    }

    public void startAnim(){
        valueAnimator = ValueAnimator.ofFloat(0f, 1f, 0f);
        valueAnimator.setDuration(ANIM_DURATION);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float)animation.getAnimatedValue();
//                float shortLineScaleY = 1 + (LINE_LONG - LINE_SHORT) * value / LINE_SHORT;
////                float longLineScaleY = 1 - (LINE_LONG - LINE_SHORT) * value / LINE_LONG;
                float shortLineScaleY = 1 + value ;
                float longLineScaleY = 1 - value ;
                viewList.get(0).setScaleY(shortLineScaleY);
                viewList.get(1).setScaleY(longLineScaleY);
                viewList.get(2).setScaleY(shortLineScaleY);
                viewList.get(3).setScaleY(longLineScaleY);
            }
        });
        valueAnimator.start();
    }

    public void stopAnim(){
        if (valueAnimator != null){
            valueAnimator.cancel();
            resetViews();
        }
    }

}
