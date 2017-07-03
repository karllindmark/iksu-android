package com.ninetwozero.iksu.features.about;

import android.content.Context;

import com.ninetwozero.iksu.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ChangelogUtil {
    private static final String RELEASE_A = "ABDOMINAL";

    private final Context context;

    private List<String> releaseOrder = new ArrayList<String>() {{
        add(RELEASE_A);
    }};

    private Map<String, Integer> releaseMapping = new HashMap<String, Integer>() {{
        put(RELEASE_A, R.string.changelog_v_abdominal);
    }};

    public ChangelogUtil(Context context) {
        this.context = context;
    }

    public String generate() {
        StringBuilder out = new StringBuilder(releaseOrder.size() * 48);
        for (int i = 0, max = releaseOrder.size(); i < max; i++) {
            out.append(context.getString(R.string.label_the_x_release, releaseOrder.get(i))).append("\n\n");
            out.append(context.getString(releaseMapping.get(releaseOrder.get(i))));

            if (releaseOrder.size() > 1) {
                out.append("\n\n");
            }
        }
        return out.toString();
    }
}
