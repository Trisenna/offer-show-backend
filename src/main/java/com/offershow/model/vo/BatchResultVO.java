package com.offershow.model.vo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;


/**
 * 批处理结果视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchResultVO<T> {
    /**
     * 成功数量
     */
    private int successCount;

    /**
     * 失败数量
     */
    private int failedCount;

    /**
     * 成功记录
     */
    private List<T> successRecords;

    /**
     * 失败记录
     */
    private List<Map<String, Object>> failedRecords;

    /**
     * 成功的ID列表 (用于批量删除)
     */
    private List<Long> successIds;

    /**
     * 失败的ID列表 (用于批量删除)
     */
    private List<Long> failedIds;
}
