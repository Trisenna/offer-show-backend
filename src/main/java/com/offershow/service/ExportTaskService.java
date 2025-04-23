package com.offershow.service;

import com.offershow.model.dto.ExportRequestDTO;
import com.offershow.model.vo.ExportTaskVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 导出任务服务接口
 */
public interface ExportTaskService {
    /**
     * 创建导出任务
     *
     * @param exportRequestDTO 导出请求DTO
     * @return 导出任务视图对象
     */
    ExportTaskVO createExportTask(ExportRequestDTO exportRequestDTO);

    /**
     * 获取导出任务状态
     *
     * @param taskId 任务ID
     * @return 导出任务视图对象
     */
    ExportTaskVO getExportTaskStatus(Long taskId);

    /**
     * 处理待执行的导出任务
     */
    void processPendingExportTasks();

    /**
     * 导入Offer数据
     *
     * @param file 导入文件
     * @return 导入结果
     */
    Map<String, Object> importOffers(MultipartFile file);
}