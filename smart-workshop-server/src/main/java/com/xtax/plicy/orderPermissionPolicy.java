package com.xtax.plicy;

import com.xtax.enums.ActionEnum;
import com.xtax.mapper.userMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 生产订单权限策略
 * 订单层属于主管权限：只有"车间主任"和"生产主管"可以管理订单
 * - PAUSE / RESUME：需"车间主任"或"生产主管"角色（人工干预）
 * - TERMINATE：需"车间主任"或"生产主管"角色（人工干预）
 * - START_WORK / FINISH_WORK：事实驱动（由工单层联动触发），不校验用户权限
 * - PUBLISH / CANCEL_PUBLISH：系统自动触发（由计划发布联动），不校验用户权限
 */
@Component
public class orderPermissionPolicy implements PermissionPolicy {
    @Autowired
    private userMapper userMapper;

    @Override
    public void check(Integer userId, ActionEnum action) {
        String position = userMapper.getUserById(userId).getPosition();
        switch (action) {
            case PUBLISH:
            case CANCEL_PUBLISH:
                // 系统联动触发，不做角色校验
                break;
            case PAUSE:
            case RESUME:
            case TERMINATE:
                // 订单层主管权限：只有车间主任和生产主管可以操作
                if (!"车间主任".equals(position) && !"生产主管".equals(position)) {
                    throw new SecurityException(position + "无权限执行此操作");
                }
                break;
            case START_WORK:
            case FINISH_WORK:
                // 事实驱动：由工单状态变更联动触发
                break;
        }
    }
}
