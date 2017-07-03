package com.ninetwozero.iksu.features.about;

import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;

import com.ninetwozero.iksu.R;

import java.util.ArrayList;
import java.util.List;

public class AboutDependenciesFragment extends BaseAboutListFragment {
    private final String LICENSE_APACHE2 = "Apache 2.0";
    private final String LICENSE_MIT = "MIT LICENSE";

    public static AboutDependenciesFragment newInstance() {
        final Bundle args = new Bundle();

        final AboutDependenciesFragment fragment = new AboutDependenciesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    List<Link> getListOfLinks() {
        final List<Link> links = new ArrayList<>();
        links.add(new Link(getString(R.string.x_by_y, "Android Support Libraries", "Google"), LICENSE_APACHE2, "https://developer.android.com/topic/libraries/support-library/index.html"));
        links.add(new Link(getString(R.string.x_by_y, "ButterKnife", "Jake Wharton"), LICENSE_APACHE2, "https://github.com/JakeWharton/butterknife/blob/master/LICENSE.txt"));
        links.add(new Link(getString(R.string.x_by_y, "CircleImageView", "Henning Dodenhof"), LICENSE_APACHE2, "https://github.com/hdodenhof/CircleImageView/blob/master/LICENSE.txt"));
        links.add(new Link(getString(R.string.x_by_y, "Firebase", "Firebase"), LICENSE_APACHE2, "https://firebase.google.com/docs/android/"));
        links.add(new Link(getString(R.string.x_by_y, "Glide", "BumpTech"), "BSD, MIT and Apache 2.0", "https://github.com/bumptech/glide/blob/master/LICENSE"));
        links.add(new Link(getString(R.string.x_by_y, "Material Dialogs", "Adrian Follestad"), LICENSE_MIT, "https://github.com/afollestad/material-dialogs/blob/master/LICENSE.txt"));
        links.add(new Link(getString(R.string.x_by_y, "Moshi", "Square"), LICENSE_APACHE2, "https://github.com/square/moshi/blob/master/LICENSE.txt"));
        links.add(new Link(getString(R.string.x_by_y, "Realm Java", "Realm"), LICENSE_APACHE2, "https://github.com/realm/realm-java/blob/master/LICENSE"));
        links.add(new Link(getString(R.string.x_by_y, "RecyclerView Fastscroll", "Tim Malseed"), LICENSE_APACHE2, "https://github.com/timusus/RecyclerView-FastScroll/blob/master/NOTICE"));
        links.add(new Link(getString(R.string.x_by_y, "Retrofit", "Square"), LICENSE_APACHE2, "https://github.com/square/retrofit/blob/master/LICENSE.txt"));
        links.add(new Link(getString(R.string.x_by_y, "ThreeTenABP", "Jake Wharton"), LICENSE_APACHE2, "https://github.com/jakewharton/threetenabp/blob/master/LICENSE.txt"));
        return links;
    }

    @Override
    RecyclerView.ItemDecoration getItemDecoration() {
        return new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
    }
}
