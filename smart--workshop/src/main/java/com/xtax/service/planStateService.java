package com.xtax.service;

import com.xtax.pojo.Plan;
import com.xtax.stateDomain.ActionEnum;

public interface planStateService {
    void handle(String planNo, ActionEnum action, Integer userId);
    void handleLinkage(Plan plan, ActionEnum action);
}
