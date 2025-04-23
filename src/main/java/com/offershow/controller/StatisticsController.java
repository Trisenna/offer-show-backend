package com.offershow.controller;

import com.offershow.model.dto.StatisticsQueryDTO;
import com.offershow.model.vo.StatisticsVO;
import com.offershow.service.StatisticsService;
import com.offershow.util.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统计信息控制器
 */
@Api(tags = "统计分析")
@RestController
@RequestMapping("/v1/statistics")
@RequiredArgsConstructor
@Validated
public class StatisticsController {

    private final StatisticsService statisticsService;

    @ApiOperation("获取薪资统计数据")
    @GetMapping("/salary")
    public ResponseResult<StatisticsVO> getSalaryStatistics(
            @ApiParam("统计维度") @RequestParam(value = "dimension", required = false, defaultValue = "company") String dimension,
            @ApiParam("开始日期") @RequestParam(value = "startDate", required = false) String startDate,
            @ApiParam("结束日期") @RequestParam(value = "endDate", required = false) String endDate) {

        StatisticsQueryDTO queryDTO = new StatisticsQueryDTO();
        queryDTO.setDimension(dimension);
        queryDTO.setStartDate(startDate);
        queryDTO.setEndDate(endDate);

        StatisticsVO statisticsVO = statisticsService.getSalaryStatistics(queryDTO);
        return ResponseResult.success(statisticsVO);
    }

    @ApiOperation("获取趋势统计数据")
    @GetMapping("/trend")
    public ResponseResult<StatisticsVO> getTrendStatistics(
            @ApiParam("统计类型") @RequestParam(value = "type", required = false, defaultValue = "monthly") String type,
            @ApiParam("统计维度") @RequestParam(value = "dimension", required = false, defaultValue = "all") String dimension,
            @ApiParam("维度值") @RequestParam(value = "dimensionValue", required = false) String dimensionValue,
            @ApiParam("开始日期") @RequestParam(value = "startDate", required = false) String startDate,
            @ApiParam("结束日期") @RequestParam(value = "endDate", required = false) String endDate) {

        StatisticsQueryDTO queryDTO = new StatisticsQueryDTO();
        queryDTO.setType(type);
        queryDTO.setDimension(dimension);
        queryDTO.setDimensionValue(dimensionValue);
        queryDTO.setStartDate(startDate);
        queryDTO.setEndDate(endDate);

        StatisticsVO statisticsVO = statisticsService.getTrendStatistics(queryDTO);
        return ResponseResult.success(statisticsVO);
    }
}