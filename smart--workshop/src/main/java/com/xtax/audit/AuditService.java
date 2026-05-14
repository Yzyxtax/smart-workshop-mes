package com.xtax.audit;

import com.xtax.mapper.auditMapper;
import com.xtax.pojo.StatusHistory;
import com.xtax.stateDomain.StateContext;
import com.xtax.stateDomain.StateEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    @Autowired
    private auditMapper auditMapper;

    public void record(String targetType, StateContext context, StateEnum fromStatus, StateEnum toStatus) {
        auditMapper.addAudit(
                "plan",
                context.getBizNo(),
                fromStatus.getCode(),
                toStatus.getCode(),
                context.getUserId()
        );
    }

    public StateEnum getLastPauseRecord(String targetType, String targetNo) {
        return auditMapper.getLastPauseRecord(targetType, targetNo);
    }
}
