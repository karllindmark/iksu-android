package com.ninetwozero.iksu.features.about;

import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;

import com.ninetwozero.iksu.R;

import java.util.ArrayList;
import java.util.List;

public class AboutLinksFragment extends BaseAboutListFragment {
    public static AboutLinksFragment newInstance() {
        final Bundle args = new Bundle();

        final AboutLinksFragment fragment = new AboutLinksFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public List<Link> getListOfLinks() {
        final List<Link> links = new ArrayList<>();
        links.add(new Link(getString(R.string.msg_about_send_email), "--> " + getString(R.string.developer_email), "mailto:" + getString(R.string.developer_email)));
        links.add(new Link(getString(R.string.msg_about_github), "--> " + getString(R.string.developer_project), AboutUiHelper.GITHUB_URL.toString()));
        links.add(new Link(getString(R.string.label_rate_the_app), "--> " + getString(R.string.app_name) + " @ Google Play", AboutUiHelper.PLAY_STORE_URL.toString()));
        return links;
    }

    @Override
    RecyclerView.ItemDecoration getItemDecoration() {
        return new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
    }
}
