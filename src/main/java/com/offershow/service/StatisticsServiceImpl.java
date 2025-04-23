package com.offershow.service;

import com.offershow.exception.BusinessException;
import com.offershow.model.dto.StatisticsQueryDTO;
import com.offershow.model.entity.Statistics;
import com.offershow.model.vo.StatisticsVO;
import com.offershow.repository.StatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 统计服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final StatisticsRepository statisticsRepository;

    @Override
    public StatisticsVO getSalaryStatistics(StatisticsQueryDTO queryDTO) {
        // 检查参数
        String dimension = StringUtils.defaultIfBlank(queryDTO.getDimension(), "COMPANY");
        if (!Arrays.asList("COMPANY", "POSITION", "CITY").contains(dimension.toUpperCase())) {
            throw new BusinessException("不支持的统计维度: " + dimension);
        }

        // 处理日期范围
        String startDate = queryDTO.getStartDate();
        String endDate = queryDTO.getEndDate();
        if (StringUtils.isBlank(endDate)) {
            endDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        }
        if (StringUtils.isBlank(startDate)) {
            startDate = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ISO_DATE);
        }

        // 获取统计数据
        List<Statistics> statisticsList = statisticsRepository.findByTypeAndDimension(
                "SALARY", dimension.toUpperCase(), startDate, endDate);

        // 转换为VO
        List<StatisticsVO.StatisticsItem> items = statisticsList.stream()
                .map(this::convertToStatisticsItem)
                .collect(Collectors.toList());

        return StatisticsVO.builder()
                .dimension(dimension)
                .statistics(items)
                .build();
    }

    @Override
    public StatisticsVO getTrendStatistics(StatisticsQueryDTO queryDTO) {
        // 检查参数
        String type = StringUtils.defaultIfBlank(queryDTO.getType(), "MONTHLY");
        if (!Arrays.asList("MONTHLY", "WEEKLY").contains(type.toUpperCase())) {
            throw new BusinessException("不支持的统计类型: " + type);
        }

        String dimension = StringUtils.defaultIfBlank(queryDTO.getDimension(), "ALL");
        if (!Arrays.asList("ALL", "COMPANY").contains(dimension.toUpperCase())) {
            throw new BusinessException("不支持的统计维度: " + dimension);
        }

        // 处理日期范围
        String startDate = queryDTO.getStartDate();
        String endDate = queryDTO.getEndDate();
        if (StringUtils.isBlank(endDate)) {
            endDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        }
        if (StringUtils.isBlank(startDate)) {
            if ("MONTHLY".equalsIgnoreCase(type)) {
                startDate = LocalDate.now().minusMonths(12).format(DateTimeFormatter.ISO_DATE);
            } else {
                startDate = LocalDate.now().minusWeeks(12).format(DateTimeFormatter.ISO_DATE);
            }
        }

        List<Statistics> statisticsList;
        String trendDimension = type.toUpperCase();

        if ("ALL".equalsIgnoreCase(dimension)) {
            // 获取整体趋势数据
            statisticsList = statisticsRepository.findByTypeAndDimension(
                    "TREND", trendDimension, startDate, endDate);
        } else {
            // 获取特定维度的趋势数据
            String dimensionValue = queryDTO.getDimensionValue();
            if (StringUtils.isBlank(dimensionValue)) {
                throw new BusinessException("维度值不能为空");
            }

            // 针对特定公司的趋势
            String fullDimension = trendDimension + "_" + dimension.toUpperCase();
            statisticsList = statisticsRepository.findByTypeAndDimensionValue(
                    "TREND", fullDimension, dimensionValue, startDate, endDate);
        }

        // 转换为趋势项
        List<StatisticsVO.TrendItem> trends = new ArrayList<>();
        for (Statistics statistics : statisticsList) {
            StatisticsVO.TrendItem item = StatisticsVO.TrendItem.builder()
                    .period(formatPeriod(statistics.getStatisticDate(), type))
                    .count(statistics.getCount())
                    .avgSalary(statistics.getStatisticValue())
                    .build();

            trends.add(item);
        }

        return StatisticsVO.builder()
                .type(type)
                .dimension(dimension)
                .dimensionValue(queryDTO.getDimensionValue())
                .trends(trends)
                .build();
    }

    /**
     * 转换为统计项
     *
     * @param statistics 统计实体
     * @return 统计项
     */
    private StatisticsVO.StatisticsItem convertToStatisticsItem(Statistics statistics) {
        return StatisticsVO.StatisticsItem.builder()
                .dimensionValue(statistics.getDimensionValue())
                .avgBaseSalary(statistics.getStatisticValue())
                .avgTotalSalary(statistics.getStatisticValue().multiply(stats.getMultiplier()))
                .count(statistics.getCount())
                .build();
    }

    /**
     * 格式化时间段
     *
     * @param date 日期
     * @param type 类型
     * @return 格式化后的时间段
     */
    private String formatPeriod(LocalDate date, String type) {
        if ("MONTHLY".equalsIgnoreCase(type)) {
            return date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        } else {
            // 周度统计，返回日期作为期间结束日期
            return date.format(DateTimeFormatter.ISO_DATE);
        }
    }

    /**
     * 统计计算工具类
     */
    private static class stats {
        /**
         * 获取总薪资倍数
         * 基于经验值，总体薪资约为基本薪资的1.8-2.5倍
         *
         * @return 倍数
         */
        public static java.math.BigDecimal getMultiplier() {
            return new java.math.BigDecimal("2.2");
        }
    }
}