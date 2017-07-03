package com.ninetwozero.iksu.features.about;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ninetwozero.iksu.BR;
import com.ninetwozero.iksu.R;

import java.util.ArrayList;
import java.util.List;

class AboutLinkAdapter extends RecyclerView.Adapter<AboutLinkAdapter.ViewHolder> {
    private static final int VIEW_TYPE_DEFAULT = 0;
    private static final int VIEW_TYPE_IMAGE = 1;

    private final Context context;
    private final LayoutInflater layoutInflater;
    private List<Link> links;
    private AboutListCallbacks callback;

    public AboutLinkAdapter(final Context context, final List<Link> links, AboutListCallbacks callback) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.links = new ArrayList<>(links);
        this.callback = callback;
    }

    @Override
    public AboutLinkAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(
            DataBindingUtil.inflate(
                layoutInflater,
                viewType == VIEW_TYPE_IMAGE ? R.layout.list_item_about_links : R.layout.list_item_about_dependencies,
                parent,
                false
            )
        );
    }

    @Override
    public void onBindViewHolder(AboutLinkAdapter.ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    private Link getItem(int position) {
        return links.get(position);
    }


    @Override
    public int getItemViewType(int position) {
        return getItem(position).hasIcon() ? VIEW_TYPE_IMAGE : VIEW_TYPE_DEFAULT;
    }

    @Override
    public int getItemCount() {
        return links == null ? 0 : links.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ViewDataBinding binding;

        public ViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            if (callback != null) {
                this.binding.setVariable(BR.callbacks, callback);
            }
        }

        public void bind(final Link link) {
            this.binding.setVariable(BR.link, link);
        }
    }
}
