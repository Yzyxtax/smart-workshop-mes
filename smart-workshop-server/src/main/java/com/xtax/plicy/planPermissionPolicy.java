package com.xtax.plicy;

import com.xtax.enums.ActionEnum;
import com.xtax.mapper.userMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class planPermissionPolicy implements PermissionPolicy{
    @Autowired
    private userMapper userMapper;

    @Override
    public void check(Integer userId, ActionEnum action) {
        String position = userMapper.getUserById(userId).getPosition();
        switch (action){
            case PUBLISH:
            case CANCEL_PUBLISH:
            case PAUSE:
            case RESUME:
            case TERMINATE:
                if(!"生产主管".equals(position)){
                    throw new SecurityException(position + "无权限执行此操作");
                }
                break;
            case START_WORK:
            case FINISH_WORK:
                throw new SecurityException(position + "无权限执行此操作");
        }
    }
}
