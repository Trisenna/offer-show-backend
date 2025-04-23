package com.offershow.model.entity;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 导出任务实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportTask {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务参数 (JSON格式)
     */
    private String taskParams;

    /**
     * 任务状态 (PENDING/PROCESSING/COMPLETED/FAILED)
     */
    private String status;

    /**
     * 导出文件URL
     */
    private String fileUrl;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 完成时间
     */
    private LocalDateTime completedAt;
}