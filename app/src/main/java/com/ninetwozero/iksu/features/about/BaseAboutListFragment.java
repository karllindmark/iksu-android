package com.ninetwozero.iksu.features.about;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ninetwozero.iksu.R;
import com.ninetwozero.iksu.app.IksuApp;
import com.ninetwozero.iksu.common.ui.BaseFragment;

import java.util.List;

import butterknife.BindView;

public abstract class BaseAboutListFragment extends BaseFragment {
    @BindView(R.id.recyclerview)
    protected RecyclerView recyclerView;

    private AboutListCallbacks onClickCallback = new AboutListCallbacks() {
        @Override
        public void onLinkClicked(Link link) {
            final Uri uri = Uri.parse(link.getUrl());

            Intent intent = null;
            if (link.getUrl().startsWith("mailto:")) {
                intent = new Intent(Intent.ACTION_SEND).setData(uri); // TODO
            } else {
                intent = new Intent(Intent.ACTION_VIEW).setData(uri);
            }

            try {
                getActivity().startActivity(intent);
            } catch (ActivityNotFoundException ignored) {
                // ignored
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_about_links, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(getItemDecoration());
        recyclerView.setAdapter(new AboutLinkAdapter(IksuApp.getContext(), getListOfLinks(), onClickCallback));
    }

    abstract List<Link> getListOfLinks();

    abstract RecyclerView.ItemDecoration getItemDecoration();
}
