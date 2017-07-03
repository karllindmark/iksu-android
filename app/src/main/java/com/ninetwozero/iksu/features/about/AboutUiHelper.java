package com.ninetwozero.iksu.features.about;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.ninetwozero.iksu.R;
import com.ninetwozero.iksu.utils.Constants;

public class AboutUiHelper {
    public static final Uri GITHUB_URL = Uri.parse("https://github.com/ninetwozero/iksu-android");
    public static final Uri PLAY_STORE_URL = Uri.parse("https://play.google.com/store/apps/details?id=com.ninetwozero.iksu");

    public void openPlayStoreListing(final Context context, final String origin) {
        FirebaseAnalytics.getInstance(context).logEvent(Constants.RATE_APP, new Intent().putExtra("origin", origin).getExtras());

        try {
            final Intent intent = new Intent(Intent.ACTION_VIEW, PLAY_STORE_URL);
            context.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(context, "Unable to open Google Play", Toast.LENGTH_LONG).show();
        }
    }

    public void openChangelogDialog(final Context context) {
        new MaterialDialog.Builder(context)
            .title(R.string.label_changelog)
            .content(new ChangelogUtil(context).generate())
            .onAny(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    if (which == DialogAction.POSITIVE) {
                        openPlayStoreListing(context, "changelog");
                    }
                    dialog.dismiss();
                }
            })
            .neutralText(R.string.label_close)
            .positiveText(R.string.label_rate_the_app)
            .build().show();
    }

}
