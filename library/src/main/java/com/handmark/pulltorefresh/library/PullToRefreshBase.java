/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.handmark.pulltorefresh.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.handmark.pulltorefresh.library.internal.FlipLoadingLayout;
import com.handmark.pulltorefresh.library.internal.LoadingLayout;
import com.handmark.pulltorefresh.library.internal.RotateLoadingLayout;
import com.handmark.pulltorefresh.library.internal.Utils;
import com.handmark.pulltorefresh.library.internal.ViewCompat;

/**
 * PullToRefreshBase实现了IPullToRefresh接口，实现了它的方法
 * 继承自LinearLayout，它是一个view
 * 被声明为抽象类型
 *
 * @param <T>
 */
public abstract class PullToRefreshBase<T extends View> extends LinearLayout implements IPullToRefresh<T> {

	// ===========================================================
	// Constants
	// ===========================================================

	static final boolean DEBUG = true;

	static final boolean USE_HW_LAYERS = false;

	static final String LOG_TAG = "PullToRefresh";

	static final float FRICTION = 2.0f;

	public static final int SMOOTH_SCROLL_DURATION_MS = 200;
	public static final int SMOOTH_SCROLL_LONG_DURATION_MS = 325;
	static final int DEMO_SCROLL_INTERVAL = 225;

	static final String STATE_STATE = "ptr_state";
	static final String STATE_MODE = "ptr_mode";
	static final String STATE_CURRENT_MODE = "ptr_current_mode";
	static final String STATE_SCROLLING_REFRESHING_ENABLED = "ptr_disable_scrolling";
	static final String STATE_SHOW_REFRESHING_VIEW = "ptr_show_refreshing_view";
	static final String STATE_SUPER = "ptr_super";

	// ===========================================================
	// Fields
	// ===========================================================

	private int mTouchSlop;
	private float mLastMotionX, mLastMotionY;
	private float mInitialMotionX, mInitialMotionY;

	private boolean mIsBeingDragged = false;
	private State mState = State.RESET;
	private Mode mMode = Mode.getDefault();

	private Mode mCurrentMode;
	public T mRefreshableView;
	private FrameLayout mRefreshableViewWrapper;

	private boolean mShowViewWhileRefreshing = true;
	private boolean mScrollingWhileRefreshingEnabled = false;
	private boolean mFilterTouchEvents = true;
	private boolean mOverScrollEnabled = true;
	private boolean mLayoutVisibilityChangesEnabled = true;

	private Interpolator mScrollAnimationInterpolator;
	private AnimationStyle mLoadingAnimationStyle = AnimationStyle.getDefault();

	private LoadingLayout mHeaderLayout;
	private LoadingLayout mFooterLayout;

	private OnRefreshListener<T> mOnRefreshListener;
	private OnRefreshListener2<T> mOnRefreshListener2;
	private OnPullEventListener<T> mOnPullEventListener;

	private SmoothScrollRunnable mCurrentSmoothScrollRunnable;

	// ===========================================================
	// Constructors
	// ===========================================================

	public PullToRefreshBase(Context context) {
		super(context);
		init(context, null);
	}

	public PullToRefreshBase(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public PullToRefreshBase(Context context, Mode mode) {
		super(context);
		mMode = mode;
		init(context, null);
	}

	public PullToRefreshBase(Context context, Mode mode, AnimationStyle animStyle) {
		super(context);
		mMode = mode;
		mLoadingAnimationStyle = animStyle;
		init(context, null);
	}

	/**
	 * PullToRefreshBase为一个LinearLayout
	 * addView()方法为linearLayout添加一个布局：scrollView或listView或recyclerView
	 *
	 * @param child
	 * @param index
	 * @param params
	 */
	@Override
	public void addView(View child, int index, ViewGroup.LayoutParams params) {
		if (DEBUG) {
			Log.d(LOG_TAG, "addView: " + child.getClass().getSimpleName());
		}

		final T refreshableView = getRefreshableView();
		// 添加的refreshableView必须是ViewGroup类型的，否则报异常
		if (refreshableView instanceof ViewGroup) {
			((ViewGroup) refreshableView).addView(child, index, params);
		} else {
			throw new UnsupportedOperationException("Refreshable View is not a ViewGroup so can't addView");
		}
	}

	@Override
	public final boolean demo() {
		if (mMode.showHeaderLoadingLayout() && isReadyForPullStart()) {
			smoothScrollToAndBack(-getHeaderSize() * 2);
			return true;
		} else if (mMode.showFooterLoadingLayout() && isReadyForPullEnd()) {
			smoothScrollToAndBack(getFooterSize() * 2);
			return true;
		}

		return false;
	}

	@Override
	public final Mode getCurrentMode() {
		return mCurrentMode;
	}

	@Override
	public final boolean getFilterTouchEvents() {
		return mFilterTouchEvents;
	}

	@Override
	public final ILoadingLayout getLoadingLayoutProxy() {
		return getLoadingLayoutProxy(true, true);
	}

	/**
	 * 获取LoadingLayout
	 * @param includeStart - Whether to include the Start/Header Views
	 * @param includeEnd - Whether to include the End/Footer Views
	 * @return
	 */
	@Override
	public final ILoadingLayout getLoadingLayoutProxy(boolean includeStart, boolean includeEnd) {
		return createLoadingLayoutProxy(includeStart, includeEnd);
	}

	@Override
	public final Mode getMode() {
		return mMode;
	}

	@Override
	public final T getRefreshableView() {
		return mRefreshableView;
	}

	@Override
	public final boolean getShowViewWhileRefreshing() {
		return mShowViewWhileRefreshing;
	}

	@Override
	public final State getState() {
		return mState;
	}

	/**
	 * @deprecated See {@link #isScrollingWhileRefreshingEnabled()}.
	 */
	public final boolean isDisableScrollingWhileRefreshing() {
		return !isScrollingWhileRefreshingEnabled();
	}

	@Override
	public final boolean isPullToRefreshEnabled() {
		return mMode.permitsPullToRefresh();
	}

	@Override
	public final boolean isPullToRefreshOverScrollEnabled() {
		return VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD && mOverScrollEnabled
				&& OverscrollHelper.isAndroidOverScrollEnabled(mRefreshableView);
	}

	@Override
	public final boolean isRefreshing() {
		return mState == State.REFRESHING || mState == State.MANUAL_REFRESHING;
	}

	@Override
	public final boolean isScrollingWhileRefreshingEnabled() {
		return mScrollingWhileRefreshingEnabled;
	}

	//onInterceptTouchEvent方法重写MotionEvent.ACTION_DOWN &&
	// mIsBeingDragged先拦截触摸事件，在action_move 时，
	// 根据设置刷新ing能否继续滑动的参数以及是否能刷新，
	// 判断是否拦截触摸事件if mScrollingWhileRefreshingEnabled &&
	// isRefreshing（），以及根据触摸滑动距离和Mode判断拦截Touch事件。
	@Override
	public final boolean onInterceptTouchEvent(MotionEvent event) {

		if (!isPullToRefreshEnabled()) {
			return false;
		}

		final int action = event.getAction();

		if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
			mIsBeingDragged = false;
			return false;
		}

		if (action != MotionEvent.ACTION_DOWN && mIsBeingDragged) {
			return true;
		}

		switch (action) {
			case MotionEvent.ACTION_MOVE: {
				// If we're refreshing, and the flag is set. Eat all MOVE events
				if (!mScrollingWhileRefreshingEnabled && isRefreshing()) {
					return true;
				}

				if (isReadyForPull()) {
					final float y = event.getY(), x = event.getX();
					final float diff, oppositeDiff, absDiff;

					// We need to use the correct values, based on scroll
					// direction
					switch (getPullToRefreshScrollDirection()) {
						case HORIZONTAL:
							diff = x - mLastMotionX;
							oppositeDiff = y - mLastMotionY;
							break;
						case VERTICAL:
						default:
							diff = y - mLastMotionY;
							oppositeDiff = x - mLastMotionX;
							break;
					}
					absDiff = Math.abs(diff);

					if (absDiff > mTouchSlop && (!mFilterTouchEvents || absDiff > Math.abs(oppositeDiff))) {
						if (mMode.showHeaderLoadingLayout() && diff >= 1f && isReadyForPullStart()) {
							mLastMotionY = y;
							mLastMotionX = x;
							mIsBeingDragged = true;
							if (mMode == Mode.BOTH) {
								mCurrentMode = Mode.PULL_FROM_START;
							}
						} else if (mMode.showFooterLoadingLayout() && diff <= -1f && isReadyForPullEnd()) {
							mLastMotionY = y;
							mLastMotionX = x;
							mIsBeingDragged = true;
							if (mMode == Mode.BOTH) {
								mCurrentMode = Mode.PULL_FROM_END;
							}
						}
					}
				}
				break;
			}
			case MotionEvent.ACTION_DOWN: {
				if (isReadyForPull()) {
					mLastMotionY = mInitialMotionY = event.getY();
					mLastMotionX = mInitialMotionX = event.getX();
					mIsBeingDragged = false;
				}
				break;
			}
		}

		return mIsBeingDragged;
	}

	/**
	 * 刷新结束调用的方法
	 * 当我们HeaderLayout 、FooterLayout视图弹出，
	 * 请求完了数据需要隐藏掉它们，这时候就需要用到它
	 */
	@Override
	public final void onRefreshComplete() {
		if (isRefreshing()) {
			setState(State.RESET);
		}
	}

	//而onTouchEvent方法
	// 内部则是根据各种状态判断设置当前的状态枚举类型State
	@Override
	public final boolean onTouchEvent(MotionEvent event) {

		if (!isPullToRefreshEnabled()) {
			return false;
		}

		// If we're refreshing, and the flag is set. Eat the event
		if (!mScrollingWhileRefreshingEnabled && isRefreshing()) {
			return true;
		}

		if (event.getAction() == MotionEvent.ACTION_DOWN && event.getEdgeFlags() != 0) {
			return false;
		}

		switch (event.getAction()) {
			case MotionEvent.ACTION_MOVE: {
				if (mIsBeingDragged) {
					mLastMotionY = event.getY();
					mLastMotionX = event.getX();
					pullEvent();
					return true;
				}
				break;
			}

			case MotionEvent.ACTION_DOWN: {
				if (isReadyForPull()) {
					mLastMotionY = mInitialMotionY = event.getY();
					mLastMotionX = mInitialMotionX = event.getX();
					return true;
				}
				break;
			}

			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP: {
				if (mIsBeingDragged) {
					mIsBeingDragged = false;

					if (mState == State.RELEASE_TO_REFRESH
							&& (null != mOnRefreshListener || null != mOnRefreshListener2)) {
						setState(State.REFRESHING, true);
						return true;
					}

					// If we're already refreshing, just scroll back to the top
					if (isRefreshing()) {
						smoothScrollTo(0);
						return true;
					}

					// If we haven't returned by here, then we're not in a state
					// to pull, so just reset
					setState(State.RESET);

					return true;
				}
				break;
			}
		}

		return false;
	}

	public final void setScrollingWhileRefreshingEnabled(boolean allowScrollingWhileRefreshing) {
		mScrollingWhileRefreshingEnabled = allowScrollingWhileRefreshing;
	}

	/**
	 * @deprecated See {@link #setScrollingWhileRefreshingEnabled(boolean)}
	 */
	public void setDisableScrollingWhileRefreshing(boolean disableScrollingWhileRefreshing) {
		setScrollingWhileRefreshingEnabled(!disableScrollingWhileRefreshing);
	}

	@Override
	public final void setFilterTouchEvents(boolean filterEvents) {
		mFilterTouchEvents = filterEvents;
	}

	/**
	 * @deprecated You should now call this method on the result of
	 *             {@link #getLoadingLayoutProxy()}.
	 */
	public void setLastUpdatedLabel(CharSequence label) {
		getLoadingLayoutProxy().setLastUpdatedLabel(label);
	}

	/**
	 * @deprecated You should now call this method on the result of
	 *             {@link #getLoadingLayoutProxy()}.
	 */
	public void setLoadingDrawable(Drawable drawable) {
		getLoadingLayoutProxy().setLoadingDrawable(drawable);
	}

	/**
	 * @deprecated You should now call this method on the result of
	 *             {@link #getLoadingLayoutProxy(boolean, boolean)}.
	 */
	public void setLoadingDrawable(Drawable drawable, Mode mode) {
		getLoadingLayoutProxy(mode.showHeaderLoadingLayout(), mode.showFooterLoadingLayout()).setLoadingDrawable(
				drawable);
	}

	@Override
	public void setLongClickable(boolean longClickable) {
		getRefreshableView().setLongClickable(longClickable);
	}

	@Override
	public final void setMode(Mode mode) {
		if (mode != mMode) {
			if (DEBUG) {
				Log.d(LOG_TAG, "Setting mode to: " + mode);
			}
			mMode = mode;
			updateUIForMode();
		}
	}

	public void setOnPullEventListener(OnPullEventListener<T> listener) {
		mOnPullEventListener = listener;
	}

	@Override
	public final void setOnRefreshListener(OnRefreshListener<T> listener) {
		mOnRefreshListener = listener;
		mOnRefreshListener2 = null;
	}

	@Override
	public final void setOnRefreshListener(OnRefreshListener2<T> listener) {
		mOnRefreshListener2 = listener;
		mOnRefreshListener = null;
	}

	/**
	 * @deprecated You should now call this method on the result of
	 *             {@link #getLoadingLayoutProxy()}.
	 * ILoadingLayout 接口的实现类LoadingLayoutProxy ,也是LoadingLayout的代理，
	 * 通过HashSet存储LoadingLayout，
	 * 设置LoadingLayout的属性则通过该代理来设置，实例如下：
	 */
	public void setPullLabel(CharSequence pullLabel) {
		getLoadingLayoutProxy().setPullLabel(pullLabel);
	}

	/**
	 * @deprecated You should now call this method on the result of
	 *             {@link #getLoadingLayoutProxy(boolean, boolean)}.
	 */
	public void setPullLabel(CharSequence pullLabel, Mode mode) {
		getLoadingLayoutProxy(mode.showHeaderLoadingLayout(), mode.showFooterLoadingLayout()).setPullLabel(pullLabel);
	}

	/**
	 * @param enable Whether Pull-To-Refresh should be used
	 * @deprecated This simple calls setMode with an appropriate mode based on
	 *             the passed value.
	 */
	public final void setPullToRefreshEnabled(boolean enable) {
		setMode(enable ? Mode.getDefault() : Mode.DISABLED);
	}

	@Override
	public final void setPullToRefreshOverScrollEnabled(boolean enabled) {
		mOverScrollEnabled = enabled;
	}

	@Override
	public final void setRefreshing() {
		setRefreshing(true);
	}

	//方法setRefreshing (boolean ) 原理是在改变State状态，从而改变ui
	@Override
	public final void setRefreshing(boolean doScroll) {
		if (!isRefreshing()) {
			setState(State.MANUAL_REFRESHING, doScroll);
		}
	}

	/**
	 * @deprecated You should now call this method on the result of
	 *             {@link #getLoadingLayoutProxy()}.
	 */
	public void setRefreshingLabel(CharSequence refreshingLabel) {
		getLoadingLayoutProxy().setRefreshingLabel(refreshingLabel);
	}

	/**
	 * @deprecated You should now call this method on the result of
	 *             {@link #getLoadingLayoutProxy(boolean, boolean)}.
	 */
	public void setRefreshingLabel(CharSequence refreshingLabel, Mode mode) {
		getLoadingLayoutProxy(mode.showHeaderLoadingLayout(), mode.showFooterLoadingLayout()).setRefreshingLabel(
				refreshingLabel);
	}

	/**
	 * @deprecated You should now call this method on the result of
	 *             {@link #getLoadingLayoutProxy()}.
	 */
	public void setReleaseLabel(CharSequence releaseLabel) {
		setReleaseLabel(releaseLabel, Mode.BOTH);
	}

	/**
	 * @deprecated You should now call this method on the result of
	 *             {@link #getLoadingLayoutProxy(boolean, boolean)}.
	 */
	public void setReleaseLabel(CharSequence releaseLabel, Mode mode) {
		getLoadingLayoutProxy(mode.showHeaderLoadingLayout(), mode.showFooterLoadingLayout()).setReleaseLabel(
				releaseLabel);
	}

	public void setScrollAnimationInterpolator(Interpolator interpolator) {
		mScrollAnimationInterpolator = interpolator;
	}

	@Override
	public final void setShowViewWhileRefreshing(boolean showView) {
		mShowViewWhileRefreshing = showView;
	}

	/**
	 * @return Either {@link Orientation#VERTICAL} or
	 *         {@link Orientation#HORIZONTAL} depending on the scroll direction.
	 */
	public abstract Orientation getPullToRefreshScrollDirection();

	final void setState(State state, final boolean... params) {
		mState = state;
		if (DEBUG) {
			Log.d(LOG_TAG, "State: " + mState.name());
		}

		switch (mState) {

			case RESET:
				onReset();  // 初始化到未刷新的状态
				break;
			case PULL_TO_REFRESH:
				onPullToRefresh();  // 开始下拉时的回调
				break;
			case RELEASE_TO_REFRESH:
				onReleaseToRefresh();  // 加载头部完全显示时的回调
				break;
			case REFRESHING:
			case MANUAL_REFRESHING:
				onRefreshing(params[0]);
				break;
			case OVERSCROLLING:
				// NO-OP
				break;
		}

		// Call OnPullEventListener
		if (null != mOnPullEventListener) {
			mOnPullEventListener.onPullEvent(this, mState, mCurrentMode);
		}
	}

	/**
	 * Used internally for adding view. Need because we override addView to
	 * pass-through to the Refreshable View
	 */
	protected final void addViewInternal(View child, int index, ViewGroup.LayoutParams params) {
		super.addView(child, index, params);
	}

	/**
	 * Used internally for adding view. Need because we override addView to
	 * pass-through to the Refreshable View
	 * 将包含listView的FrameLayout添加到LinearLayout
	 */
	protected final void addViewInternal(View child, ViewGroup.LayoutParams params) {
		super.addView(child, -1, params);
	}

	protected LoadingLayout createLoadingLayout(Context context, Mode mode, TypedArray attrs) {
		LoadingLayout layout = mLoadingAnimationStyle.createLoadingLayout(context, mode,
				getPullToRefreshScrollDirection(), attrs);
		layout.setVisibility(View.INVISIBLE);
		return layout;
	}

	/**
	 * 创建一个代理对象，这里可以选择代理对象中是否包含头布局，脚布局
	 * Used internally for {@link #getLoadingLayoutProxy(boolean, boolean)}.
	 * Allows derivative classes to include any extra LoadingLayouts.
	 */
	protected LoadingLayoutProxy createLoadingLayoutProxy(final boolean includeStart, final boolean includeEnd) {
		LoadingLayoutProxy proxy = new LoadingLayoutProxy();

		if (includeStart && mMode.showHeaderLoadingLayout()) {
			proxy.addLayout(mHeaderLayout);
		}
		if (includeEnd && mMode.showFooterLoadingLayout()) {
			proxy.addLayout(mFooterLayout);
		}

		return proxy;
	}

	/**
	 * This is implemented by derived classes to return the created View. If you
	 * need to use a custom View (such as a custom ListView), override this
	 * method and return an instance of your custom class.
	 * <p/>
	 * Be sure to set the ID of the view in this method, especially if you're
	 * using a ListActivity or ListFragment.
	 * 
	 * @param context Context to create view with
	 * @param attrs AttributeSet from wrapped class. Means that anything you
	 *            include in the XML layout declaration will be routed to the
	 *            created View
	 * @return New instance of the Refreshable View
	 */
	protected abstract T createRefreshableView(Context context, AttributeSet attrs);

	protected final void disableLoadingLayoutVisibilityChanges() {
		mLayoutVisibilityChangesEnabled = false;
	}

	protected final LoadingLayout getFooterLayout() {
		return mFooterLayout;
	}

	protected final int getFooterSize() {
		return mFooterLayout.getContentSize();
	}

	protected final LoadingLayout getHeaderLayout() {
		return mHeaderLayout;
	}

	protected final int getHeaderSize() {
		return mHeaderLayout.getContentSize();
	}

	protected int getPullToRefreshScrollDuration() {
		return SMOOTH_SCROLL_DURATION_MS;
	}

	protected int getPullToRefreshScrollDurationLonger() {
		return SMOOTH_SCROLL_LONG_DURATION_MS;
	}

	protected FrameLayout getRefreshableViewWrapper() {
		return mRefreshableViewWrapper;
	}

	/**
	 * Allows Derivative classes to handle the XML Attrs without creating a
	 * TypedArray themsevles
	 * 
	 * @param a - TypedArray of PullToRefresh Attributes
	 */
	protected void handleStyledAttributes(TypedArray a) {
	}

	/**
	 * Implemented by derived class to return whether the View is in a state
	 * where the user can Pull to Refresh by scrolling from the end.
	 * 
	 * @return true if the View is currently in the correct state (for example,
	 *         bottom of a ListView)
	 */
	protected abstract boolean isReadyForPullEnd();

	/**
	 * Implemented by derived class to return whether the View is in a state
	 * where the user can Pull to Refresh by scrolling from the start.
	 * 
	 * @return true if the View is currently the correct state (for example, top
	 *         of a ListView)
	 */
	protected abstract boolean isReadyForPullStart();

	/**
	 * Called by {@link #onRestoreInstanceState(Parcelable)} so that derivative
	 * classes can handle their saved instance state.
	 * 
	 * @param savedInstanceState - Bundle which contains saved instance state.
	 */
	protected void onPtrRestoreInstanceState(Bundle savedInstanceState) {
	}

	/**
	 * Called by {@link #onSaveInstanceState()} so that derivative classes can
	 * save their instance state.
	 * 
	 * @param saveState - Bundle to be updated with saved state.
	 */
	protected void onPtrSaveInstanceState(Bundle saveState) {
	}

	/**
	 * Called when the UI has been to be updated to be in the
	 * {@link State#PULL_TO_REFRESH} state.
	 * onPullToRefresh方法根据mCurrentMode调用
	 * HeaderLayout、FooterLayout（LoadingLayout）
	 * 各自的抽象方法具体实现稍后再说,诸如此类方法就不一一列举
	 */
	protected void onPullToRefresh() {
		switch (mCurrentMode) {
			case PULL_FROM_END:
				mFooterLayout.pullToRefresh();
				break;
			case PULL_FROM_START:
				mHeaderLayout.pullToRefresh();
				break;
			default:
				// NO-OP
				break;
		}
	}

	/**
	 * Called when the UI has been to be updated to be in the
	 * {@link State#REFRESHING} or {@link State#MANUAL_REFRESHING} state.
	 * 
	 * @param doScroll - Whether the UI should scroll for this event.
	 */
	protected void onRefreshing(final boolean doScroll) {
		if (mMode.showHeaderLoadingLayout()) {
			mHeaderLayout.refreshing();
		}
		if (mMode.showFooterLoadingLayout()) {
			mFooterLayout.refreshing();
		}

		if (doScroll) {
			if (mShowViewWhileRefreshing) {

				// Call Refresh Listener when the Scroll has finished
				OnSmoothScrollFinishedListener listener = new OnSmoothScrollFinishedListener() {
					@Override
					public void onSmoothScrollFinished() {
						callRefreshListener();
					}
				};

				switch (mCurrentMode) {
					case MANUAL_REFRESH_ONLY:
					case PULL_FROM_END:
						smoothScrollTo(getFooterSize(), listener);
						break;
					default:
					case PULL_FROM_START:
						smoothScrollTo(-getHeaderSize(), listener);
						break;
				}
			} else {
				smoothScrollTo(0);
			}
		} else {
			// We're not scrolling, so just call Refresh Listener now
			callRefreshListener();
		}
	}

	/**
	 * Called when the UI has been to be updated to be in the
	 * {@link State#RELEASE_TO_REFRESH} state.
	 */
	protected void onReleaseToRefresh() {
		switch (mCurrentMode) {
			case PULL_FROM_END:
				mFooterLayout.releaseToRefresh();
				break;
			case PULL_FROM_START:
				mHeaderLayout.releaseToRefresh();
				break;
			default:
				// NO-OP
				break;
		}
	}

	/**
	 * Called when the UI has been to be updated to be in the
	 * {@link State#RESET} state.
	 * setStatue方法里面调用onReset,
	 * 继续跟进发现LoadingLayout调用了reset方法，并且smoothScrollTo方法调用，
	 * 间接的new 了SmoothScrollRunnable，一个定时长的减速scrollTo动画执行
	 */
	protected void onReset() {
		mIsBeingDragged = false;
		mLayoutVisibilityChangesEnabled = true;

		// Always reset both layouts, just in case...
		mHeaderLayout.reset();  // 重置header
		mFooterLayout.reset();  // 重置footer

		smoothScrollTo(0);
	}

	@Override
	protected final void onRestoreInstanceState(Parcelable state) {
		if (state instanceof Bundle) {
			Bundle bundle = (Bundle) state;

			setMode(Mode.mapIntToValue(bundle.getInt(STATE_MODE, 0)));
			mCurrentMode = Mode.mapIntToValue(bundle.getInt(STATE_CURRENT_MODE, 0));

			mScrollingWhileRefreshingEnabled = bundle.getBoolean(STATE_SCROLLING_REFRESHING_ENABLED, false);
			mShowViewWhileRefreshing = bundle.getBoolean(STATE_SHOW_REFRESHING_VIEW, true);

			// Let super Restore Itself
			super.onRestoreInstanceState(bundle.getParcelable(STATE_SUPER));

			State viewState = State.mapIntToValue(bundle.getInt(STATE_STATE, 0));
			if (viewState == State.REFRESHING || viewState == State.MANUAL_REFRESHING) {
				setState(viewState, true);
			}

			// Now let derivative classes restore their state
			onPtrRestoreInstanceState(bundle);
			return;
		}

		super.onRestoreInstanceState(state);
	}

	@Override
	protected final Parcelable onSaveInstanceState() {
		Bundle bundle = new Bundle();

		// Let derivative classes get a chance to save state first, that way we
		// can make sure they don't overrite any of our values
		onPtrSaveInstanceState(bundle);

		bundle.putInt(STATE_STATE, mState.getIntValue());
		bundle.putInt(STATE_MODE, mMode.getIntValue());
		bundle.putInt(STATE_CURRENT_MODE, mCurrentMode.getIntValue());
		bundle.putBoolean(STATE_SCROLLING_REFRESHING_ENABLED, mScrollingWhileRefreshingEnabled);
		bundle.putBoolean(STATE_SHOW_REFRESHING_VIEW, mShowViewWhileRefreshing);
		bundle.putParcelable(STATE_SUPER, super.onSaveInstanceState());

		return bundle;
	}

	@Override
	protected final void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (DEBUG) {
			Log.d(LOG_TAG, String.format("onSizeChanged. W: %d, H: %d", w, h));
		}

		super.onSizeChanged(w, h, oldw, oldh);

		// We need to update the header/footer when our size changes
		refreshLoadingViewsSize();

		// Update the Refreshable View layout
		refreshRefreshableViewSize(w, h);

		/**
		 * As we're currently in a Layout Pass, we need to schedule another one
		 * to layout any changes we've made here
		 */
		post(new Runnable() {
			@Override
			public void run() {
				requestLayout();
			}
		});
	}

	/**
	 * Re-measure the Loading Views height, and adjust internal padding a necessary
	 */
	protected final void refreshLoadingViewsSize() {
		final int maximumPullScroll = (int) (getMaximumPullScroll() * 1.2f);

		// 获取linearLayout的四个方向的padding
		int pLeft = getPaddingLeft();
		int pTop = getPaddingTop();
		int pRight = getPaddingRight();
		int pBottom = getPaddingBottom();

		switch (getPullToRefreshScrollDirection()) {
			case HORIZONTAL:
				if (mMode.showHeaderLoadingLayout()) {
					mHeaderLayout.setWidth(maximumPullScroll);
					pLeft = -maximumPullScroll;
				} else {
					pLeft = 0;
				}

				if (mMode.showFooterLoadingLayout()) {
					mFooterLayout.setWidth(maximumPullScroll);
					pRight = -maximumPullScroll;
				} else {
					pRight = 0;
				}
				break;

				// 垂直方向刷新
			case VERTICAL:
				if (mMode.showHeaderLoadingLayout()) {
					mHeaderLayout.setHeight(maximumPullScroll);
					pTop = -maximumPullScroll;
				} else {
					pTop = 0;
				}

				if (mMode.showFooterLoadingLayout()) {
					mFooterLayout.setHeight(maximumPullScroll);
					pBottom = -maximumPullScroll;
				} else {
					pBottom = 0;
				}
				break;
		}

		if (DEBUG) {
			Log.d(LOG_TAG, String.format("Setting Padding. L: %d, T: %d, R: %d, B: %d", pLeft, pTop, pRight, pBottom));
		}
		// linearLayout的setPadding方法
		setPadding(pLeft, pTop, pRight, pBottom);  // 通过设置padding来隐藏header,footer
	}

	protected final void refreshRefreshableViewSize(int width, int height) {
		// We need to set the Height of the Refreshable View to the same as
		// this layout
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mRefreshableViewWrapper.getLayoutParams();

		switch (getPullToRefreshScrollDirection()) {
			case HORIZONTAL:
				if (lp.width != width) {
					lp.width = width;
					mRefreshableViewWrapper.requestLayout();
				}
				break;
			case VERTICAL:
				if (lp.height != height) {
					lp.height = height;
					mRefreshableViewWrapper.requestLayout();
				}
				break;
		}
	}

	/**
	 * Helper method which just calls scrollTo() in the correct scrolling
	 * direction.
	 * 
	 * @param value - New Scroll value
	 */
	protected final void setHeaderScroll(int value) {
		if (DEBUG) {
			Log.d(LOG_TAG, "setHeaderScroll: " + value);
		}

		// Clamp value to with pull scroll range
		final int maximumPullScroll = getMaximumPullScroll();
		value = Math.min(maximumPullScroll, Math.max(-maximumPullScroll, value));

		if (mLayoutVisibilityChangesEnabled) {
			if (value < 0) {
				mHeaderLayout.setVisibility(View.VISIBLE);
			} else if (value > 0) {
				mFooterLayout.setVisibility(View.VISIBLE);
			} else {
				mHeaderLayout.setVisibility(View.INVISIBLE);
				mFooterLayout.setVisibility(View.INVISIBLE);
			}
		}

		if (USE_HW_LAYERS) {
			/**
			 * Use a Hardware Layer on the Refreshable View if we've scrolled at
			 * all. We don't use them on the Header/Footer Views as they change
			 * often, which would negate any HW layer performance boost.
			 */
			ViewCompat.setLayerType(mRefreshableViewWrapper, value != 0 ? View.LAYER_TYPE_HARDWARE
					: View.LAYER_TYPE_NONE);
		}

		switch (getPullToRefreshScrollDirection()) {
			case VERTICAL:
				// 调用了LinearLayout的scrollTo方法，将LinearLayout的内容滑动至
				scrollTo(0, value);  // 将View中的内容在Y方向滑动value距离
				break;
			case HORIZONTAL:
				scrollTo(value, 0);
				break;
		}
	}

	/**
	 * Smooth Scroll to position using the default duration of
	 * {@value #SMOOTH_SCROLL_DURATION_MS} ms.
	 * 
	 * @param scrollValue - Position to scroll to
	 */
	protected final void smoothScrollTo(int scrollValue) {
		smoothScrollTo(scrollValue, getPullToRefreshScrollDuration());
	}

	/**
	 * Smooth Scroll to position using the default duration of
	 * {@value #SMOOTH_SCROLL_DURATION_MS} ms.
	 * 
	 * @param scrollValue - Position to scroll to
	 * @param listener - Listener for scroll
	 */
	protected final void smoothScrollTo(int scrollValue, OnSmoothScrollFinishedListener listener) {
		smoothScrollTo(scrollValue, getPullToRefreshScrollDuration(), 0, listener);
	}

	/**
	 * Smooth Scroll to position using the longer default duration of
	 * {@value #SMOOTH_SCROLL_LONG_DURATION_MS} ms.
	 * 
	 * @param scrollValue - Position to scroll to
	 */
	protected final void smoothScrollToLonger(int scrollValue) {
		smoothScrollTo(scrollValue, getPullToRefreshScrollDurationLonger());
	}

	/**
	 * Updates the View State when the mode has been set. This does not do any
	 * checking that the mode is different to current state so always updates.
	 * 更新UI，根据设置的mode，该方法在init()方法中，init()方法在构造方法中
	 */
	protected void updateUIForMode() {
		// We need to use the correct LayoutParam values, based on scroll
		// direction
		final LinearLayout.LayoutParams lp = getLoadingLayoutLayoutParams();

		// Remove Header, and then add Header Loading View again if needed
		if (this == mHeaderLayout.getParent()) {
			removeView(mHeaderLayout);
		}
		// 如果设置的下拉或者both,则加入头布局
		if (mMode.showHeaderLoadingLayout()) {
			addViewInternal(mHeaderLayout, 0, lp);  // header加入到最上面，index = 0
		}

		// Remove Footer, and then add Footer Loading View again if needed
		if (this == mFooterLayout.getParent()) {
			removeView(mFooterLayout);
		}
		// 如果设置的下拉或者both，则加入脚布局
		if (mMode.showFooterLoadingLayout()) {
			addViewInternal(mFooterLayout, lp);  // footer加入到最下面，index = -1
		}

		// Hide Loading Views  隐藏loadingView的方法
		refreshLoadingViewsSize();  // 通过setPadding()将footer和header隐藏掉

		// If we're not using Mode.BOTH, set mCurrentMode to mMode, otherwise
		// set it to pull down
		mCurrentMode = (mMode != Mode.BOTH) ? mMode : Mode.PULL_FROM_START;
	}

	private void addRefreshableView(Context context, T refreshableView) {
		mRefreshableViewWrapper = new FrameLayout(context);
		mRefreshableViewWrapper.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
		// 使用FrameLayout将listView添加进来，宽和高都匹配父容器
		mRefreshableViewWrapper.addView(refreshableView, ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);

		// 再将含有listView的frameLayout加入linearLayout中，frameLayout设置为宽和高都匹配父容器
		addViewInternal(mRefreshableViewWrapper, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
	}

	private void callRefreshListener() {
		if (null != mOnRefreshListener) {
			mOnRefreshListener.onRefresh(this);
		} else if (null != mOnRefreshListener2) {
			if (mCurrentMode == Mode.PULL_FROM_START) {
				mOnRefreshListener2.onPullDownToRefresh(this);
			} else if (mCurrentMode == Mode.PULL_FROM_END) {
				mOnRefreshListener2.onPullUpToRefresh(this);
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void init(Context context, AttributeSet attrs) {
		switch (getPullToRefreshScrollDirection()) {
			case HORIZONTAL:
				setOrientation(LinearLayout.HORIZONTAL);
				break;
			case VERTICAL:
			default:
				setOrientation(LinearLayout.VERTICAL);
				break;
		}

		setGravity(Gravity.CENTER);

		ViewConfiguration config = ViewConfiguration.get(context);
		mTouchSlop = config.getScaledTouchSlop();

		// Styleables from XML
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PullToRefresh);

		if (a.hasValue(R.styleable.PullToRefresh_ptrMode)) {
			mMode = Mode.mapIntToValue(a.getInteger(R.styleable.PullToRefresh_ptrMode, 0));
		}

		if (a.hasValue(R.styleable.PullToRefresh_ptrAnimationStyle)) {
			mLoadingAnimationStyle = AnimationStyle.mapIntToValue(a.getInteger(
					R.styleable.PullToRefresh_ptrAnimationStyle, 0));
		}

		// Refreshable View
		// By passing the attrs, we can add ListView/GridView params via XML
		mRefreshableView = createRefreshableView(context, attrs);
		addRefreshableView(context, mRefreshableView);

		// We need to create now layouts now
		// 在调用updateUIForMode之前将我们的头布局和脚布局先创建出来
		mHeaderLayout = createLoadingLayout(context, Mode.PULL_FROM_START, a);
		mFooterLayout = createLoadingLayout(context, Mode.PULL_FROM_END, a);

		/**
		 * Styleables from XML
		 */
		if (a.hasValue(R.styleable.PullToRefresh_ptrRefreshableViewBackground)) {
			Drawable background = a.getDrawable(R.styleable.PullToRefresh_ptrRefreshableViewBackground);
			if (null != background) {
				mRefreshableView.setBackgroundDrawable(background);
			}
		} else if (a.hasValue(R.styleable.PullToRefresh_ptrAdapterViewBackground)) {
			Utils.warnDeprecation("ptrAdapterViewBackground", "ptrRefreshableViewBackground");
			Drawable background = a.getDrawable(R.styleable.PullToRefresh_ptrAdapterViewBackground);
			if (null != background) {
				mRefreshableView.setBackgroundDrawable(background);
			}
		}

		if (a.hasValue(R.styleable.PullToRefresh_ptrOverScroll)) {
			mOverScrollEnabled = a.getBoolean(R.styleable.PullToRefresh_ptrOverScroll, true);
		}

		if (a.hasValue(R.styleable.PullToRefresh_ptrScrollingWhileRefreshingEnabled)) {
			mScrollingWhileRefreshingEnabled = a.getBoolean(
					R.styleable.PullToRefresh_ptrScrollingWhileRefreshingEnabled, false);
		}

		// Let the derivative classes have a go at handling attributes, then
		// recycle them...
		// 最开始的时候就为listView添加头布局，创建脚布局
		handleStyledAttributes(a);
		a.recycle();

		// Finally update the UI for the modes
		updateUIForMode();  // 更新UI，根据设置的mode，该方法在init()方法中，init()方法在构造方法中
	}

	private boolean isReadyForPull() {
		switch (mMode) {
			case PULL_FROM_START:
				return isReadyForPullStart();
			case PULL_FROM_END:
				return isReadyForPullEnd();
			case BOTH:
				return isReadyForPullEnd() || isReadyForPullStart();
			default:
				return false;
		}
	}

	/**
	 * Actions a Pull Event
	 * 
	 * @return true if the Event has been handled, false if there has been no
	 *         change
	 */
	private void pullEvent() {
		final int newScrollValue;
		final int itemDimension;
		final float initialMotionValue, lastMotionValue;

		switch (getPullToRefreshScrollDirection()) {
			case HORIZONTAL:
				initialMotionValue = mInitialMotionX;
				lastMotionValue = mLastMotionX;
				break;
			case VERTICAL:
			default:
				initialMotionValue = mInitialMotionY;
				lastMotionValue = mLastMotionY;
				break;
		}

		switch (mCurrentMode) {
			case PULL_FROM_END:
				newScrollValue = Math.round(Math.max(initialMotionValue - lastMotionValue, 0) / FRICTION);
				itemDimension = getFooterSize();
				break;
			case PULL_FROM_START:
			default:
				newScrollValue = Math.round(Math.min(initialMotionValue - lastMotionValue, 0) / FRICTION);
				itemDimension = getHeaderSize();
				break;
		}

		setHeaderScroll(newScrollValue);

		if (newScrollValue != 0 && !isRefreshing()) {
			float scale = Math.abs(newScrollValue) / (float) itemDimension;
			switch (mCurrentMode) {
				case PULL_FROM_END:
					mFooterLayout.onPull(scale);
					break;
				case PULL_FROM_START:
				default:
					mHeaderLayout.onPull(scale);
					break;
			}

			if (mState != State.PULL_TO_REFRESH && itemDimension >= Math.abs(newScrollValue)) {
				setState(State.PULL_TO_REFRESH);
			} else if (mState == State.PULL_TO_REFRESH && itemDimension < Math.abs(newScrollValue)) {
				setState(State.RELEASE_TO_REFRESH);
			}
		}
	}

	private LinearLayout.LayoutParams getLoadingLayoutLayoutParams() {
		switch (getPullToRefreshScrollDirection()) {
			case HORIZONTAL:
				return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.MATCH_PARENT);
			case VERTICAL:
			default:
				return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
		}
	}

	//PullToRefreshBase类构造函数初始化了触摸敏感系数mTouchSlop，
	// 并创建添加HeaderLayout、FooterLayout,
	// 再调用updateUIForMode方法更具Mode修改调整UI，
	// refreshLoadingViewsSize方法调整LoadingLayout相关大小，
	// 而影响其本质的因素，先看下面这个方法
	//FRICTION这个参数固定值2.0，根据父控件宽高/固定系数得到（左右上下方向）上拉下拉对应的HeaderLayout 、FooterLayout的宽高，
	// 如果我们想缩小HeaderLayout的高度只需要加大固定系数FRICTION,但是的注意，
	// 别改得太大了导致布局显示出问题。
	private int getMaximumPullScroll() {
		switch (getPullToRefreshScrollDirection()) {
			case HORIZONTAL:
				return Math.round(getWidth() / FRICTION);
			case VERTICAL:
			default:
				return Math.round(getHeight() / FRICTION);
		}
	}

	/**
	 * Smooth Scroll to position using the specific duration
	 * 
	 * @param scrollValue - Position to scroll to
	 * @param duration - Duration of animation in milliseconds
	 */
	private final void smoothScrollTo(int scrollValue, long duration) {
		smoothScrollTo(scrollValue, duration, 0, null);
	}

	private final void smoothScrollTo(int newScrollValue, long duration, long delayMillis,
			OnSmoothScrollFinishedListener listener) {
		if (null != mCurrentSmoothScrollRunnable) {
			mCurrentSmoothScrollRunnable.stop();
		}

		final int oldScrollValue;
		switch (getPullToRefreshScrollDirection()) {
			case HORIZONTAL:
				oldScrollValue = getScrollX();
				break;
			case VERTICAL:
			default:
				oldScrollValue = getScrollY();
				break;
		}

		if (oldScrollValue != newScrollValue) {
			if (null == mScrollAnimationInterpolator) {
				// Default interpolator is a Decelerate Interpolator
				mScrollAnimationInterpolator = new DecelerateInterpolator();
			}
			mCurrentSmoothScrollRunnable = new SmoothScrollRunnable(oldScrollValue, newScrollValue, duration, listener);

			if (delayMillis > 0) {
				postDelayed(mCurrentSmoothScrollRunnable, delayMillis);
			} else {
				post(mCurrentSmoothScrollRunnable);
			}
		}
	}

	private final void smoothScrollToAndBack(int y) {
		smoothScrollTo(y, SMOOTH_SCROLL_DURATION_MS, 0, new OnSmoothScrollFinishedListener() {

			@Override
			public void onSmoothScrollFinished() {
				smoothScrollTo(0, SMOOTH_SCROLL_DURATION_MS, DEMO_SCROLL_INTERVAL, null);
			}
		});
	}

	public static enum AnimationStyle {
		/**
		 * This is the default for Android-PullToRefresh. Allows you to use any
		 * drawable, which is automatically rotated and used as a Progress Bar.
		 * 默认使用旋转的进度条 ProgressBar
		 */
		ROTATE,

		/**
		 * This is the old default, and what is commonly used on iOS. Uses an
		 * arrow image which flips depending on where the user has scrolled.
		 * 箭头图像翻转根据用户手势
		 */
		FLIP;

		// 默认状态使用旋转进度条
		static AnimationStyle getDefault() {
			return ROTATE;
		}

		/**
		 * Maps an int to a specific mode. This is needed when saving state, or
		 * inflating the view from XML where the mode is given through a attr
		 * int.
		 * 
		 * @param modeInt - int to map a Mode to
		 * @return Mode that modeInt maps to, or ROTATE by default.
		 */
		static AnimationStyle mapIntToValue(int modeInt) {
			switch (modeInt) {
				case 0x0:
				default:
					return ROTATE;
				case 0x1:
					return FLIP;
			}
		}

		/**
		 * 创建头布局的方法
		 * 根据getDefault()返回不同的布局，我们如果想不使用系统的布局，使用自己设置的布局类型的话，可以在此处进行更改
		 */
		LoadingLayout createLoadingLayout(Context context, Mode mode, Orientation scrollDirection, TypedArray attrs) {
			switch (this) {
				case ROTATE:
				default:
					return new RotateLoadingLayout(context, mode, scrollDirection, attrs);
				case FLIP:
					return new FlipLoadingLayout(context, mode, scrollDirection, attrs);
			}
		}
	}

	public static enum Mode {

		/**
		 * Disable all Pull-to-Refresh gesture and Refreshing handling
		 * 禁用刷新加载
		 */
		DISABLED(0x0),

		/**
		 * Only allow the user to Pull from the start of the Refreshable View to
		 * refresh. The start is either the Top or Left, depending on the
		 * scrolling direction.
		 * 仅仅支持下拉刷新
		 */
		PULL_FROM_START(0x1),

		/**
		 * Only allow the user to Pull from the end of the Refreshable View to
		 * refresh. The start is either the Bottom or Right, depending on the
		 * scrolling direction.
		 * 仅仅支持上啦加载更多
		 */
		PULL_FROM_END(0x2),

		/**
		 * Allow the user to both Pull from the start, from the end to refresh.
		 * 上啦下拉都支持
		 */
		BOTH(0x3),

		/**
		 * Disables Pull-to-Refresh gesture handling, but allows manually
		 * setting the Refresh state via
		 * {@link PullToRefreshBase#setRefreshing() setRefreshing()}.
		 * 只允许使用代码触动刷新
		 */
		MANUAL_REFRESH_ONLY(0x4);

		/**
		 * @deprecated Use {@link #PULL_FROM_START} from now on.
		 */
		public static Mode PULL_DOWN_TO_REFRESH = Mode.PULL_FROM_START;

		/**
		 * @deprecated Use {@link #PULL_FROM_END} from now on.
		 */
		public static Mode PULL_UP_TO_REFRESH = Mode.PULL_FROM_END;

		/**
		 * Maps an int to a specific mode. This is needed when saving state, or
		 * inflating the view from XML where the mode is given through a attr
		 * int.
		 * 
		 * @param modeInt - int to map a Mode to
		 * @return Mode that modeInt maps to, or PULL_FROM_START by default.
		 */
		static Mode mapIntToValue(final int modeInt) {
			for (Mode value : Mode.values()) {
				if (modeInt == value.getIntValue()) {
					return value;
				}
			}

			// If not, return default
			return getDefault();
		}

		//默认状态只支持刷新
		static Mode getDefault() {
			return PULL_FROM_START;
		}

		private int mIntValue;

		// The modeInt values need to match those from attrs.xml
		Mode(int modeInt) {
			mIntValue = modeInt;
		}

		/**
		 * @return true if the mode permits Pull-to-Refresh
		 * 如果当前模式允许刷新则返回true
		 */
		boolean permitsPullToRefresh() {
			return !(this == DISABLED || this == MANUAL_REFRESH_ONLY);
		}

		/**
		 * @return true if this mode wants the Loading Layout Header to be shown
		 * 如果该模式下能加载显示header部分，则返回true
		 */
		public boolean showHeaderLoadingLayout() {
			return this == PULL_FROM_START || this == BOTH;
		}

		/**
		 * @return true if this mode wants the Loading Layout Footer to be shown
		 * 如果该模式下能加载显示footer部分，则返回true
		 */
		public boolean showFooterLoadingLayout() {
			return this == PULL_FROM_END || this == BOTH || this == MANUAL_REFRESH_ONLY;
		}

		int getIntValue() {
			return mIntValue;
		}

	}

	// ===========================================================
	// Inner, Anonymous Classes, and Enumerations
	// ===========================================================

	/**
	 * Simple Listener that allows you to be notified when the user has scrolled
	 * to the end of the AdapterView. See (
	 * {@link PullToRefreshAdapterViewBase#setOnLastItemVisibleListener}.
	 * 
	 * @author Chris Banes
	 */
	public static interface OnLastItemVisibleListener {

		/**
		 *
		 * Called when the user has scrolled to the end of the list
		 */
		public void onLastItemVisible();

	}

	/**
	 * Listener that allows you to be notified when the user has started or
	 * finished a touch event. Useful when you want to append extra UI events
	 * (such as sounds). See (
	 * {@link PullToRefreshAdapterViewBase#setOnPullEventListener}.
	 * 
	 * @author Chris Banes
	 * 上啦和下拉的Event事件回调接口类OnPullEventListener
	 */
	public static interface OnPullEventListener<V extends View> {

		/**
		 * Called when the internal state has been changed, usually by the user
		 * pulling.
		 * 
		 * @param refreshView - View which has had it's state change.
		 * @param state - The new state of View.
		 * @param direction - One of {@link Mode#PULL_FROM_START} or
		 *            {@link Mode#PULL_FROM_END} depending on which direction
		 *            the user is pulling. Only useful when <var>state</var> is
		 *            {@link State#PULL_TO_REFRESH} or
		 *            {@link State#RELEASE_TO_REFRESH}.
		 *                  通过用户上下拉引起状态改变，把触摸事件回调
		 */
		public void onPullEvent(final PullToRefreshBase<V> refreshView, State state, Mode direction);

	}

	/**
	 * Simple Listener to listen for any callbacks to Refresh.
	 * 
	 * @author Chris Banes
	 */
	//这个接口只支持刷新模式
	public static interface OnRefreshListener<V extends View> {

		/**
		 * onRefresh will be called for both a Pull from start, and Pull from
		 * end
		 * 下拉结束后能够刷新（滑动距离>=阈值）调用onRefresh回掉函数
		 */
		public void onRefresh(final PullToRefreshBase<V> refreshView);

	}

	/**
	 * An advanced version of the Listener to listen for callbacks to Refresh.
	 * This listener is different as it allows you to differentiate between Pull
	 * Ups, and Pull Downs.
	 * 
	 * @author Chris Banes
	 * 当前模式支持刷新和加载更多
	 */
	//这个接口
	public static interface OnRefreshListener2<V extends View> {
		// TODO These methods need renaming to START/END rather than DOWN/UP

		/**
		 * onPullDownToRefresh will be called only when the user has Pulled from
		 * the start, and released.
		 * 下拉刷新回调该函数
		 */
		public void onPullDownToRefresh(final PullToRefreshBase<V> refreshView);

		/**
		 * onPullUpToRefresh will be called only when the user has Pulled from
		 * the end, and released.
		 * 上拉加载更多回调该函数
		 */
		public void onPullUpToRefresh(final PullToRefreshBase<V> refreshView);

	}

	//方向，枚举
	public static enum Orientation {
		VERTICAL, HORIZONTAL;
	}

	public static enum State {

		/**
		 * When the UI is in a state which means that user is not interacting
		 * with the Pull-to-Refresh function.
		 * 重置初始化状态
		 */
		RESET(0x0),

		/**
		 * When the UI is being pulled by the user, but has not been pulled far
		 * enough so that it refreshes when released.
		 * 拉动距离不足指定阈值，进行释放
		 */
		PULL_TO_REFRESH(0x1),

		/**
		 * When the UI is being pulled by the user, and <strong>has</strong>
		 * been pulled far enough so that it will refresh when released.
		 * 拉动距离大于等于指定阈值，进行释放
		 */
		RELEASE_TO_REFRESH(0x2),

		/**
		 * When the UI is currently refreshing, caused by a pull gesture.
		 * 由于用户手势操作，引起当前UI刷新
		 */
		REFRESHING(0x8),

		/**
		 * When the UI is currently refreshing, caused by a call to
		 * {@link PullToRefreshBase#setRefreshing() setRefreshing()}.
		 * 由于代码调用setRefreshing引起刷新UI
		 */
		MANUAL_REFRESHING(0x9),

		/**
		 * When the UI is currently overscrolling, caused by a fling on the
		 * Refreshable View.
		 * 由于结束滑动，可以刷新视图
		 */
		OVERSCROLLING(0x10);

		/**
		 * Maps an int to a specific state. This is needed when saving state.
		 * 
		 * @param stateInt - int to map a State to
		 * @return State that stateInt maps to
		 */
		static State mapIntToValue(final int stateInt) {
			for (State value : State.values()) {
				if (stateInt == value.getIntValue()) {
					return value;
				}
			}

			// If not, return default
			return RESET;
		}

		private int mIntValue;

		State(int intValue) {
			mIntValue = intValue;
		}

		int getIntValue() {
			return mIntValue;
		}
	}

	//一个滑动相关联的Runnable
	// 实现类SmoothScrollRunnable以及一个滑动结束的监听
	// 接口OnSmoothScrollFinishedListener
	final class SmoothScrollRunnable implements Runnable {
		private final Interpolator mInterpolator;
		private final int mScrollToY;
		private final int mScrollFromY;
		private final long mDuration;
		private OnSmoothScrollFinishedListener mListener;

		private boolean mContinueRunning = true;
		private long mStartTime = -1;
		private int mCurrentY = -1;


		public SmoothScrollRunnable(int fromY, int toY, long duration, OnSmoothScrollFinishedListener listener) {
			mScrollFromY = fromY;
			mScrollToY = toY;
			mInterpolator = mScrollAnimationInterpolator;
			mDuration = duration;
			mListener = listener;
		}

		@Override
		public void run() {

			/**
			 * Only set mStartTime if this is the first time we're starting,
			 * else actually calculate the Y delta
			 */
			if (mStartTime == -1) {
				mStartTime = System.currentTimeMillis();
			} else {

				/**
				 * We do do all calculations in long to reduce software float
				 * calculations. We use 1000 as it gives us good accuracy and
				 * small rounding errors
				 */
				long normalizedTime = (1000 * (System.currentTimeMillis() - mStartTime)) / mDuration;
				normalizedTime = Math.max(Math.min(normalizedTime, 1000), 0);

				final int deltaY = Math.round((mScrollFromY - mScrollToY)
						* mInterpolator.getInterpolation(normalizedTime / 1000f));
				mCurrentY = mScrollFromY - deltaY;
				// 根据计算的距离设置Hearlayout的滑动，
				// 该方法控制HeaderLayout、FooterLayout的显示与否，
				// 同时还根据参数控制硬件加速渲染相关，最终目的调用了scrollTo方法。
				setHeaderScroll(mCurrentY);
			}

			// If we're not at the target Y, keep going...
			if (mContinueRunning && mScrollToY != mCurrentY) {
				ViewCompat.postOnAnimation(PullToRefreshBase.this, this);
			} else {
				if (null != mListener) {
					//滑动结束了回调
					mListener.onSmoothScrollFinished();
				}
			}
		}

		public void stop() {
			mContinueRunning = false;
			//停止滑动，并移除监听
			removeCallbacks(this);
		}
	}

	static interface OnSmoothScrollFinishedListener {
		void onSmoothScrollFinished();
	}

}
