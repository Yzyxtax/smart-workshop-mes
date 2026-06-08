package com.xtax.plicy;

import com.xtax.stateDomain.StateContext;

/**
 * 门禁校验接口
 * 用于在关键状态变更（如 PUBLISH）前校验业务前置条件
 */
public interface GatePolicyInterface {
    /** 验证计划发布条件 */
    void check(StateContext context);

    /**
     * 验证订单发布条件
     * @param context  状态上下文（bizNo = orderNo）
     * @param planNo   订单所属计划编号
     * @param lineNo   订单绑定的产线编号
     */
    void checkOrder(StateContext context, String planNo, String lineNo);
}
