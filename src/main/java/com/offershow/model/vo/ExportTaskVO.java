package com.offershow.model.vo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;


/**
 * 批处理结果视图对象

/**
 * 导出任务视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportTaskVO {
    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 完成时间
     */
    private LocalDateTime completedAt;
}
