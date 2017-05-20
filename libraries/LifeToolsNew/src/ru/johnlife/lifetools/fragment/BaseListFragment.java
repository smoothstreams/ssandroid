package ru.johnlife.lifetools.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import ru.johnlife.lifetools.R;
import ru.johnlife.lifetools.adapter.BaseAdapter;
import ru.johnlife.lifetools.data.AbstractData;

public abstract class BaseListFragment<T extends AbstractData> extends BaseAbstractFragment {

	private Context context;

	private RecyclerView list;
	private BaseAdapter<T> adapter;
	private View emptyView;

	private RecyclerView.AdapterDataObserver adapterObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            emptyView.setVisibility(adapter.getCount() == 0 ? View.VISIBLE : View.GONE);
        }
    };

	protected abstract BaseAdapter<T> instantiateAdapter(Context context);

	protected int getLayoutId() {
		return R.layout.fragment_list;
	}

	public BaseAdapter<T> getAdapter(){
		return adapter;
	}

	@Override
	protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		context = inflater.getContext();
		ViewGroup view = new FrameLayout(context);
		view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		inflater.inflate(getLayoutId(), view, true);
		list = (RecyclerView) view.findViewById(getListId());
		if (null != list) {
			list.setHasFixedSize(true);
			list.setLayoutManager(getListLayoutManager());
			adapter = instantiateAdapter(context);
			list.setAdapter(adapter);
		}
		emptyView = inflater.inflate(getEmptyLayoutId(), view, false);
		emptyView.setLayoutParams(list.getLayoutParams());
		view.addView(emptyView);
		adapter.registerAdapterDataObserver(adapterObserver);
		adapterObserver.onChanged();
		return view;
	}

	@Override
	public void onDestroyView() {
		adapter.unregisterAdapterDataObserver(adapterObserver);
		super.onDestroyView();
	}

	protected int getEmptyLayoutId() {
		return R.layout.fragment_list_empty_view;
	}

	protected int getListId() {
		return R.id.list;
	}

	@NonNull
	protected LinearLayoutManager getListLayoutManager() {
		return new LinearLayoutManager(list.getContext());
	}

	public RecyclerView getList() {
		return list;
	}

	@Override
	public Context getContext() {
		return context;
	}
}
