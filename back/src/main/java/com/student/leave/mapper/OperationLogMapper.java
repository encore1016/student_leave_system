package com.student.leave.mapper;

import com.student.leave.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 操作日志Mapper接口
 */
@Mapper
public interface OperationLogMapper {
    
    /**
     * 根据ID查询操作日志
     */
    OperationLog selectById(@Param("id") Long id);
    
    /**
     * 插入操作日志
     */
    int insert(OperationLog operationLog);
    
    /**
     * 查询操作日志列表
     */
    List<OperationLog> selectList(@Param("userId") Long userId,
                                   @Param("module") String module,
                                   @Param("startTime") Date startTime,
                                   @Param("endTime") Date endTime);
    
    /**
     * 删除操作日志
     */
    int deleteById(@Param("id") Long id);
}
