package com.xtax.plicy;

import com.xtax.stateDomain.StateContext;

public interface GatePolicyInterface {
    // 验证计划发布条件
    void check(StateContext context);
}
