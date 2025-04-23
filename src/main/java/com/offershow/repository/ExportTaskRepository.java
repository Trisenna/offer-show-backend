package com.offershow.repository;

import com.offershow.model.entity.ExportTask;

import java.util.List;

/**
 * 导出任务仓库接口
 */
public interface ExportTaskRepository {
    /**
     * 插入导出任务
     *
     * @param exportTask 导出任务实体
     * @return 影响行数
     */
    int insert(ExportTask exportTask);

    /**
     * 更新导出任务
     *
     * @param exportTask 导出任务实体
     * @return 影响行数
     */
    int update(ExportTask exportTask);

    /**
     * 根据 ID 查询导出任务
     *
     * @param id 导出任务 ID
     * @return 导出任务实体
     */
    ExportTask findById(Long id);

    /**
     * 查询所有待处理的导出任务
     *
     * @return 导出任务列表
     */
    List<ExportTask> findPendingTasks();

    /**
     * 更新任务状态
     *
     * @param id     任务ID
     * @param status 状态
     * @return 影响行数
     */
    int updateStatus(Long id, String status);

    /**
     * 更新任务文件URL
     *
     * @param id      任务ID
     * @param fileUrl 文件URL
     * @return 影响行数
     */
    int updateFileUrl(Long id, String fileUrl);

    /**
     * 删除指定日期之前的已完成任务
     *
     * @param date 日期
     * @return 影响行数
     */
    int deleteCompletedTasksBefore(String date);
}