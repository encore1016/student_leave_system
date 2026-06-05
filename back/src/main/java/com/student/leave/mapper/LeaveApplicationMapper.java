package com.student.leave.mapper;

import com.student.leave.entity.LeaveApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 请假申请Mapper接口
 */
@Mapper
public interface LeaveApplicationMapper {
    
    /**
     * 根据ID查询请假申请
     */
    LeaveApplication selectById(@Param("id") Long id);
    
    /**
     * 插入请假申请
     */
    int insert(LeaveApplication leaveApplication);
    
    /**
     * 更新请假申请
     */
    int update(LeaveApplication leaveApplication);
    
    /**
     * 更新请假状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    
    /**
     * 查询用户的请假列表
     */
    List<LeaveApplication> selectByUserId(@Param("userId") Long userId, 
                                          @Param("status") String status,
                                          @Param("type") String type,
                                          @Param("startTime") Date startTime,
                                          @Param("endTime") Date endTime);
    
    /**
     * 查询班级的请假列表
     */
    List<LeaveApplication> selectByClassId(@Param("classId") String classId,
                                           @Param("status") String status,
                                           @Param("type") String type,
                                           @Param("startTime") Date startTime,
                                           @Param("endTime") Date endTime);
    
    /**
     * 查询学院的请假列表
     */
    List<LeaveApplication> selectByCollegeId(@Param("collegeId") String collegeId,
                                             @Param("status") String status,
                                             @Param("type") String type,
                                             @Param("startTime") Date startTime,
                                             @Param("endTime") Date endTime);
    
    /**
     * 查询所有请假列表
     */
    List<LeaveApplication> selectAll(@Param("status") String status,
                                     @Param("type") String type,
                                     @Param("startTime") Date startTime,
                                     @Param("endTime") Date endTime);
    
    /**
     * 检查时间冲突
     */
    int checkTimeConflict(@Param("userId") Long userId,
                         @Param("startTime") Date startTime,
                         @Param("endTime") Date endTime,
                         @Param("excludeId") Long excludeId);
    
    /**
     * 查询即将到期的请假记录
     */
    List<LeaveApplication> selectExpiringSoon(@Param("hours") Integer hours);
    
    /**
     * 查询已逾期未销假的记录
     */
    List<LeaveApplication> selectOverdue();
}
