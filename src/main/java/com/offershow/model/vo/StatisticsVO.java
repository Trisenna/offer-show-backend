package com.offershow.model.vo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;


/**
 * 统计信息视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsVO {
    /**
     * 统计类型
     */
    private String type;

    /**
     * 统计维度
     */
    private String dimension;

    /**
     * 维度值
     */
    private String dimensionValue;

    /**
     * 统计列表
     */
    private List<StatisticsItem> statistics;

    /**
     * 趋势数据
     */
    private List<TrendItem> trends;

    /**
     * 统计项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatisticsItem {
        /**
         * 维度值
         */
        private String dimensionValue;

        /**
         * 平均基本薪资
         */
        private BigDecimal avgBaseSalary;

        /**
         * 平均总薪资
         */
        private BigDecimal avgTotalSalary;

        /**
         * 数量
         */
        private int count;
    }

    /**
     * 趋势项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendItem {
        /**
         * 时间段
         */
        private String period;

        /**
         * 数量
         */
        private int count;

        /**
         * 平均薪资
         */
        private BigDecimal avgSalary;
    }
}