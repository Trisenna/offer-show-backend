package com.offershow.model.dto;

import lombok.Data;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;


/**
 * 统计查询 DTO
 */
@Data
public class StatisticsQueryDTO {
    /**
     * 统计维度
     */
    private String dimension;

    /**
     * 维度值 (当需要针对特定维度值查询时使用)
     */
    private String dimensionValue;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;

    /**
     * 统计类型 (月度/季度)
     */
    private String type;
}