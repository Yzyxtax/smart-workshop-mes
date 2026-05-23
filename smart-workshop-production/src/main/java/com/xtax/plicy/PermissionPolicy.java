package com.xtax.plicy;

import com.xtax.enums.ActionEnum;

public interface PermissionPolicy {
    void check(Integer userId, ActionEnum action);
}
