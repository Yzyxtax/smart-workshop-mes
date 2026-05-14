package com.xtax.service;

import com.xtax.pojo.ProductionOrder;
import com.xtax.stateDomain.ActionEnum;

public interface orderStateService {
    void handle(String orderNo, ActionEnum action, Integer userId);
    void handleLinkage(ProductionOrder order, ActionEnum action);
}
