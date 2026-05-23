package com.xtax.service;

import com.xtax.entity.Plan;
import com.xtax.enums.ActionEnum;

public interface planStateService {
    void handle(String planNo, ActionEnum action, Integer userId);
    void handleLinkage(Plan plan, ActionEnum action);
}
