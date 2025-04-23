package com.offershow.repository;

import com.offershow.model.entity.Statistics;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 统计信息仓库接口
 */
public interface StatisticsRepository {
    /**
     * 插入统计信息
     *
     * @param statistics 统计信息实体
     * @return 影响行数
     */
    int insert(Statistics statistics);

    /**
     * 批量插入统计信息
     *
     * @param statisticsList 统计信息实体列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<Statistics> statisticsList);

    /**
     * 根据统计类型和维度查询统计信息
     *
     * @param statisticType 统计类型
     * @param dimension     统计维度
     * @param startDate     开始日期
     * @param endDate       结束日期
     * @return 统计信息列表
     */
    List<Statistics> findByTypeAndDimension(
            @Param("statisticType") String statisticType,
            @Param("dimension") String dimension,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);

    /**
     * 根据统计类型、维度和维度值查询统计信息
     *
     * @param statisticType  统计类型
     * @param dimension      统计维度
     * @param dimensionValue 维度值
     * @param startDate      开始日期
     * @param endDate        结束日期
     * @return 统计信息列表
     */
    List<Statistics> findByTypeAndDimensionValue(
            @Param("statisticType") String statisticType,
            @Param("dimension") String dimension,
            @Param("dimensionValue") String dimensionValue,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);

    /**
     * 删除指定日期之前的统计数据
     *
     * @param date 日期
     * @return 影响行数
     */
    int deleteStatisticsBefore(String date);

    /**
     * 检查指定日期的统计数据是否已存在
     *
     * @param statisticType  统计类型
     * @param dimension      统计维度
     * @param statisticDate  统计日期
     * @return 数量
     */
    int checkStatisticsExists(
            @Param("statisticType") String statisticType,
            @Param("dimension") String dimension,
            @Param("statisticDate") String statisticDate);
}