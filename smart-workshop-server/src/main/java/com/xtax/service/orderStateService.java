package com.xtax.service;

import com.xtax.entity.ProductionOrder;
import com.xtax.enums.ActionEnum;

public interface orderStateService {
    void handle(String orderNo, ActionEnum action, Integer userId);
    void handleLinkage(ProductionOrder order, ActionEnum action);
}
