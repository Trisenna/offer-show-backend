package com.offershow.service;

import com.offershow.model.dto.StatisticsQueryDTO;
import com.offershow.model.vo.StatisticsVO;

/**
 * 统计服务接口
 */
public interface StatisticsService {
    /**
     * 获取薪资统计数据
     *
     * @param queryDTO 查询条件
     * @return 统计结果
     */
    StatisticsVO getSalaryStatistics(StatisticsQueryDTO queryDTO);

    /**
     * 获取趋势统计数据
     *
     * @param queryDTO 查询条件
     * @return 统计结果
     */
    StatisticsVO getTrendStatistics(StatisticsQueryDTO queryDTO);
}