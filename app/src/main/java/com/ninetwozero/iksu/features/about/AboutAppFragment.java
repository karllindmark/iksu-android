package com.ninetwozero.iksu.features.about;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ninetwozero.iksu.R;
import com.ninetwozero.iksu.common.ui.BaseFragment;

import butterknife.BindView;
import butterknife.OnClick;

import static android.content.Intent.ACTION_VIEW;

public class AboutAppFragment extends BaseFragment {
    private static final int SURPRISE_LIMIT = 32;

    @BindView(R.id.version_number)
    protected TextView versionNumber;

    private int logoClickCounter = 0;
    private Toast toast;

    public static AboutAppFragment newInstance() {
        final Bundle args = new Bundle();

        final AboutAppFragment fragment = new AboutAppFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_about_app, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupVersionTextView();
    }

    @SuppressWarnings("unused") // Used by ButterKnife
    @OnClick(R.id.app_name)
    public void onLogoClicked(final View view) {
        String message = null;

        switch (++this.logoClickCounter) {
            case 3:
                message = "...";
                break;
            case 5:
                message = ":-)";
                break;
            case 13:
                message = ":-D";
                break;
            case 20:
                message = getString(R.string.random_drumroll);
                break;
            case SURPRISE_LIMIT - 3:
                message = "3...";
                break;
            case SURPRISE_LIMIT - 2:
                message = "2...";
                break;
            case SURPRISE_LIMIT - 1:
                message = "1...";
                break;
            case SURPRISE_LIMIT:
                onLogoClickedTooManyTimes();
                break;
        }

        if (message != null) {
            if (toast != null) {
                toast.cancel();
            }

            toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @SuppressWarnings("unused") // Used by ButterKnife
    @OnClick({R.id.button_changelog, R.id.button_rate})
    public void onButtonClicked(final View view) {
        final AboutUiHelper helper = new AboutUiHelper();
        if (view.getId() == R.id.button_rate) {
            helper.openPlayStoreListing(getContext(), "about");
        } else if (view.getId() == R.id.button_changelog){
            helper.openChangelogDialog(getContext());
        }
    }

    private void setupVersionTextView() {
            final Activity activity = getActivity();
            String version = getString(R.string.label_unknown_version);

            if (activity != null) {
                try {
                    final PackageManager manager = getActivity().getPackageManager();
                    if (manager != null) {
                        version = "v" + manager.getPackageInfo(activity.getPackageName(), 0).versionName;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
            versionNumber.setText(version);
    }

    private void onLogoClickedTooManyTimes() {
        startActivity(new Intent(ACTION_VIEW).setData(Uri.parse("https://www.youtube.com/watch?v=MKN39OJ_CvM")));
        this.logoClickCounter = 0;
    }
}
