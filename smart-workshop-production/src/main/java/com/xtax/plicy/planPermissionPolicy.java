package com.xtax.plicy;

import com.xtax.mapper.userMapper;
import com.xtax.stateDomain.ActionEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class planPermissionPolicy implements PermissionPolicy{
    @Autowired
    private userMapper userMapper;

    /**
     * 统一权限校验入口
     *
     * @param userId   当前操作用户
     * @param action 执行动作
     * @throws SecurityException 如果权限不足则抛出异常
     */
    @Override
    public void check(Integer userId, ActionEnum action) {
        // 1. 获取用户职位信息
        String position = userMapper.getUserById(userId).getPosition();
        // 2. 根据职位信息进行权限校验
        switch (action){
            case PUBLISH:
            case CANCEL_PUBLISH:
            case PAUSE:
            case RESUME:
                if(!"生产主管".equals(position)){
                    throw new SecurityException(position + "无权限执行此操作");
                }else {
                    return;
                }
            case START_WORK:
            case FINISH_WORK:
                throw new SecurityException(position + "无权限执行此操作");
        }
    }
}
