package com.xtax.mapper;

import com.xtax.pojo.StatusHistory;
import com.xtax.stateDomain.StateEnum;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface auditMapper {
    // 添加审计记录
    @Insert("insert into status_history(target_type, target_no, old_status, new_status, operator_id, created_time) values(#{targetType}, #{targetNo}, #{oldStatus}, #{newStatus}, #{operatorId}, NOW())")
    void addAudit(String targetType, String targetNo, String oldStatus, String newStatus, Integer operatorId);

    //查询某个实体最近一次暂停的记录
    @Select("select old_status from status_history where target_type = #{targetType} and target_no = #{targetNo} and new_status = 'PAUSED' order by created_time desc limit 1")
    public StateEnum getLastPauseRecord(String targetType, String targetNo);
}
