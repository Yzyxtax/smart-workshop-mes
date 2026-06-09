package com.xtax.plicy;

import com.xtax.enums.ActionEnum;
import com.xtax.mapper.userMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 工单权限策略
 * 工单层是最靠近现场的执行层，权限模型区分员工和主管两类角色：
 *
 * | 操作          | 角色           | 附加校验                       |
 * |--------------|---------------|------------------------------|
 * | START_WORK   | 员工           | 必须为本工单的派工人员（userId 匹配）  |
 * | FINISH_WORK  | 员工           | 必须为本工单的派工人员（userId 匹配）  |
 * | PAUSE        | 员工 / 主管     | 员工需 userId 匹配；主管可跨工单操作   |
 * | RESUME       | 员工 / 主管     | 员工需 userId 匹配；主管可跨工单操作   |
 * | TERMINATE    | 主管           | 需"车间主任"或"生产主管"角色         |
 * | PUBLISH / CANCEL_PUBLISH | 系统触发 | 由订单发布联动，不校验用户角色       |
 *
 * 注意：员工级别的 userId 匹配校验在 workOrderStateServiceImpl 中完成，
 *       本类只做角色级别的校验。
 */
@Component
public class workOrderPermissionPolicy implements PermissionPolicy {
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

            case START_WORK:
            case FINISH_WORK:
                // 员工操作：角色校验仅限于非主管人员
                // 具体 userId 匹配在 workOrderStateServiceImpl 中校验
                break;

            case PAUSE:
            case RESUME:
                // 员工或主管均可操作
                break;

            case TERMINATE:
                // 需"车间主任"或"生产主管"角色
                if (!"车间主任".equals(position) && !"生产主管".equals(position)) {
                    throw new SecurityException(position + "无权限执行此操作");
                }
                break;
        }
    }
}
