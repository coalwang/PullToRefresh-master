package com.cola.CustomView.view.pulltorefresh;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;

public class PullToRefreshRecyclerViewBase extends PullToRefreshBase<RecyclerView> {

    //当滑动到list的末端的时候call
    private OnLastItemVisibleListener mOnLastItemVisibleListener;
    //list的末端的item是否可见
    private boolean mLastItemVisible;
    private RecyclerView.LayoutManager mLayoutManager;

    //#########################################四个构造方法#############################
    public PullToRefreshRecyclerViewBase(Context context) {
        super(context);
        init();
    }

    public PullToRefreshRecyclerViewBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PullToRefreshRecyclerViewBase(Context context, Mode mode) {
        super(context, mode);
        init();
    }

    public PullToRefreshRecyclerViewBase(Context context, Mode mode, AnimationStyle animStyle) {
        super(context, mode, animStyle);
        init();
    }

    //###############################四个需要继承的抽象方法####################
    @Override
    public Orientation getPullToRefreshScrollDirection() {
        return Orientation.VERTICAL;
    }

    @Override
    protected RecyclerView createRefreshableView(Context context, AttributeSet attrs) {
        RecyclerView recyclerView = new RecyclerView(getContext());
        return recyclerView;
    }

    @Override
    protected boolean isReadyForPullEnd() {
//        int lastVisiblePosition = mRefreshableView.getChildPosition(mRefreshableView.getChildAt(mRefreshableView.getChildCount() -1));
//        if (lastVisiblePosition >= mRefreshableView.getAdapter().getItemCount()-1) {
//            return mRefreshableView.getChildAt(mRefreshableView.getChildCount() - 1).getBottom() <= mRefreshableView.getBottom();
//        }
//        return false;
        return isLastItemVisible();
    }

    @Override
    protected boolean isReadyForPullStart() {
        return isFirstItemVisible();
    }

    //###############################################################
    private void init(){
        //mRefreshableView:内容部分的view，如listView,RecyclerView
        mRefreshableView.addOnScrollListener(mOnScrollListener);
//        setmOnLastItemVisibleListener(new OnLastItemVisibleListener() {
//            @Override
//            public void onLastItemVisible() {
//                Toast.makeText(getContext(), "已经拉倒最底部啦", Toast.LENGTH_SHORT).show();
//            }
//        });
    }
    //重写滑动监听
    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        //滑动状态变化是调用的方法
        //scrollState有三种状态，分别是
        //开始滚动（SCROLL_STATE_FLING），
        //正在滚动(SCROLL_STATE_TOUCH_SCROLL),
        //已经停止（SCROLL_STATE_IDLE）
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            //
            if(newState == RecyclerView.SCROLL_STATE_IDLE && mOnLastItemVisibleListener!= null && mLastItemVisible){
                mOnLastItemVisibleListener.onLastItemVisible();
            }
        }

        //滑动过程中调用的方法
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if(mLayoutManager == null){
                mLayoutManager = recyclerView.getLayoutManager();
                return;
            }
            //总共的item数量
            int totalItemCount = mLayoutManager.getItemCount();
            int firstVisibleItem = 0;
            //可见的item数量
            int visibleItemCount = mLayoutManager.getChildCount();
            //得到第一个可见的item的位置
            if(mLayoutManager instanceof GridLayoutManager){
                firstVisibleItem = ((GridLayoutManager)mLayoutManager).findFirstVisibleItemPosition();
            }else if(mLayoutManager instanceof GridLayoutManager){
                firstVisibleItem = ((LinearLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
            }
            //如果第一个可见的item的位置加上可见的item的数目大于总的item数目，则最后一个item肯定属于可见状态
            mLastItemVisible = (totalItemCount > 0) && (firstVisibleItem + visibleItemCount >= totalItemCount);
        }
    };

    public void setAdapter(RecyclerView.Adapter adapter){
        mRefreshableView.setAdapter(adapter);
    }

    public void setmOnLastItemVisibleListener(OnLastItemVisibleListener listener){
        mOnLastItemVisibleListener  = listener;
    }

    private boolean isFirstItemVisible() {
        RecyclerView.Adapter<RecyclerView.ViewHolder> adapter = mRefreshableView.getAdapter();
        if(adapter == null || adapter.getItemCount() == 0){
            return true;
        }
        int firstVisiblePosition = 0;
        if (mLayoutManager instanceof GridLayoutManager) {
            firstVisiblePosition = ((GridLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
        } else if (mLayoutManager instanceof LinearLayoutManager){
            firstVisiblePosition = ((LinearLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
        }
        /**
         * This check should really just be:
         * mRefreshableView.getFirstVisiblePosition() == 0, but PtRListView
         * internally use a HeaderView which messes the positions up. For
         * now we'll just add one to account for it and rely on the inner
         * condition which checks getTop().
         */
        //第一个正在显示的item的位置大于1，则第一个item肯定不可见
        if(firstVisiblePosition <= 1){
            View firstVisibleChild = mRefreshableView.getChildAt(0);
            if(firstVisibleChild != null){
                return firstVisibleChild.getTop() >= mRefreshableView.getTop();
            }
        }else {
            return false;
        }
        return true; //默认情况返回true
    }

    private boolean isLastItemVisible() {

        final RecyclerView.Adapter<RecyclerView.ViewHolder> adapter = mRefreshableView.getAdapter();

        if (null == adapter || adapter.getItemCount() == 0) {
            return true;
        } else {
            int lastVisiblePosition = 0;
            if(mLayoutManager instanceof GridLayoutManager) {
                lastVisiblePosition = ((GridLayoutManager) mLayoutManager).findLastVisibleItemPosition();
            } else {
                lastVisiblePosition = ((LinearLayoutManager) mLayoutManager).findLastVisibleItemPosition();
            }
            final int lastItemPosition = mRefreshableView.getChildCount() - 1;
            if (lastVisiblePosition >= lastItemPosition - 1) {
                int firstVisiblePosition = 0;
                if(mLayoutManager instanceof GridLayoutManager) {
                    firstVisiblePosition = ((GridLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
                } else {
                    firstVisiblePosition = ((LinearLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
                }
                final int childIndex = lastVisiblePosition - firstVisiblePosition;
                final View lastVisibleChild = mRefreshableView.getChildAt(childIndex);
                if (lastVisibleChild != null) {
                    return lastVisibleChild.getBottom() <= mRefreshableView.getBottom();
                }
            }
        }
        return false;
    }
}
