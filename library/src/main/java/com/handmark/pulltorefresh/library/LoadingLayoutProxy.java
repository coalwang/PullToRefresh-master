package com.handmark.pulltorefresh.library;

import java.util.HashSet;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import com.handmark.pulltorefresh.library.internal.LoadingLayout;


/**
 * 代理模式中的的角色：
 * ISubject：抽象对象，接口，定义了一些抽象方法
 * RealSubject：真实对象，是实现了抽象对象角色的类
 * Proxy：代理对象，内部含有真实对象的引用，从而操作真实对象，代理对象可以在执行
 * 真实对象的操作时附加其他操作，相当于对真实对象进行封装，
 * 代理对象和真实对象都实现了抽象对象接口
 */
public class LoadingLayoutProxy implements ILoadingLayout {

	private final HashSet<LoadingLayout> mLoadingLayouts;

	LoadingLayoutProxy() {
		// 实例化HashSet，规定泛型为LoadingLayout
		mLoadingLayouts = new HashSet<LoadingLayout>();
	}

	/**
	 * This allows you to add extra LoadingLayout instances to this proxy. This
	 * is only necessary if you keep your own instances, and want to have them
	 * included in any
	 * {@link PullToRefreshBase#createLoadingLayoutProxy(boolean, boolean)
	 * createLoadingLayoutProxy(...)} calls.
	 * 
	 * @param layout - LoadingLayout to have included.
	 */
	// 将loadingLayout加入到hashSet
	public void addLayout(LoadingLayout layout) {
		if (null != layout) {
			mLoadingLayouts.add(layout);
		}
	}

	// 接口中的方法
	// 方法中调用的是真实对象的该方法
	@Override
	public void setLastUpdatedLabel(CharSequence label) {
		for (LoadingLayout layout : mLoadingLayouts) {
			layout.setLastUpdatedLabel(label);
		}
	}

	// 接口中的方法
	// 方法中调用的是真实对象的该方法
	@Override
	public void setLoadingDrawable(Drawable drawable) {
		for (LoadingLayout layout : mLoadingLayouts) {
			layout.setLoadingDrawable(drawable);
		}
	}

	// 接口中的方法
	// 方法中调用的是真实对象的该方法
	@Override
	public void setRefreshingLabel(CharSequence refreshingLabel) {
		for (LoadingLayout layout : mLoadingLayouts) {
			layout.setRefreshingLabel(refreshingLabel);
		}
	}

	// 接口中的方法
	// 方法中调用的是真实对象的该方法
	@Override
	public void setPullLabel(CharSequence label) {
		for (LoadingLayout layout : mLoadingLayouts) {
			layout.setPullLabel(label);
		}
	}

	// 接口中的方法
	// 方法中调用的是真实对象的该方法
	@Override
	public void setReleaseLabel(CharSequence label) {
		for (LoadingLayout layout : mLoadingLayouts) {
			layout.setReleaseLabel(label);
		}
	}

	// 接口中的方法
	// 方法中调用的是真实对象的该方法
	public void setTextTypeface(Typeface tf) {
		for (LoadingLayout layout : mLoadingLayouts) {
			layout.setTextTypeface(tf);
		}
	}
}
