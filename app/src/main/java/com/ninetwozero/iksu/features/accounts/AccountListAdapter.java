package com.ninetwozero.iksu.features.accounts;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.bumptech.glide.Glide;
import com.ninetwozero.iksu.BR;
import com.ninetwozero.iksu.R;
import com.ninetwozero.iksu.models.UserAccount;
import com.ninetwozero.iksu.utils.ApiHelper;
import com.ninetwozero.iksu.utils.DensityUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class AccountListAdapter extends RealmRecyclerViewAdapter<UserAccount, AccountListAdapter.ViewHolder> {
    private final LayoutInflater layoutInflater;
    private final int avatarSize;
    private final AccountListCallbacks listCallbacks;

    public AccountListAdapter(Context context, AccountListCallbacks accountListCallbacks, @Nullable OrderedRealmCollection<UserAccount> data, boolean autoUpdate) {
        super(data, autoUpdate);

        this.layoutInflater = LayoutInflater.from(context);
        this.avatarSize = DensityUtils.toPixels(context.getResources().getDimension(R.dimen.account_list_avatar_size));
        this.listCallbacks = accountListCallbacks;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewDataBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.list_item_account, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    @Override
    public void updateData(@Nullable OrderedRealmCollection<UserAccount> data) {
        super.updateData(data);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ViewDataBinding binding;

        @BindView(R.id.avatar)
        CircleImageView avatarView;

        @SuppressWarnings("unused") // ButterKnife
        @OnCheckedChanged(R.id.enabled)
        void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            listCallbacks.onToggleAccountState(getItem(getAdapterPosition()), !isChecked);
        }

        ViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            ButterKnife.bind(this, this.itemView);

            this.binding = binding;
            this.binding.setVariable(BR.handler, listCallbacks);
            this.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (listCallbacks != null) {
                        listCallbacks.onAccountLongClick(getItem(getAdapterPosition()));
                        return true;
                    }
                    return false;
                }
            });
        }

        void bind(final UserAccount userAccount) {
            Glide.with(this.binding.getRoot().getContext())
                .load(ApiHelper.buildGravatarUrl(userAccount.getUsername(), avatarSize))
                .error(R.drawable.ic_account_circle_black_24dp)
                .into(this.avatarView);

            this.binding.setVariable(BR.account, userAccount);
            this.binding.executePendingBindings();
        }
    }
}