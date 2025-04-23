package com.offershow.model.entity;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 统计信息实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Statistics {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 统计类型 (SALARY/TREND)
     */
    private String statisticType;

    /**
     * 统计维度 (COMPANY/POSITION/CITY)
     */
    private String dimension;

    /**
     * 维度值
     */
    private String dimensionValue;

    /**
     * 统计值
     */
    private BigDecimal statisticValue;

    /**
     * 统计样本数量
     */
    private Integer count;

    /**
     * 统计日期
     */
    private LocalDate statisticDate;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}