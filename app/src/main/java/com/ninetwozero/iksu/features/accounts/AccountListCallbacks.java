package com.ninetwozero.iksu.features.accounts;

import com.ninetwozero.iksu.models.UserAccount;

public interface AccountListCallbacks {
    void onAccountClick(final UserAccount account);

    void onAccountLongClick(final UserAccount account);

    void onToggleAccountState(final UserAccount account, final boolean disabled);
}
