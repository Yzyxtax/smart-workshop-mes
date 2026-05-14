package com.xtax.plicy;

import com.xtax.stateDomain.ActionEnum;

public interface PermissionPolicy {
    void check(Integer userId, ActionEnum action);
}
