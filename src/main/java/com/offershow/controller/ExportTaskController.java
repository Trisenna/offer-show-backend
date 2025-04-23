package com.offershow.controller;

import com.offershow.model.dto.ExportRequestDTO;
import com.offershow.model.vo.ExportTaskVO;
import com.offershow.service.ExportTaskService;
import com.offershow.util.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 导入导出控制器
 */
@Api(tags = "导入导出管理")
@RestController
@Validated
@RequiredArgsConstructor
public class ExportTaskController {

    private final ExportTaskService exportTaskService;

    @ApiOperation("导出Offer数据")
    @PostMapping("/v1/offers/export")
    public ResponseResult<ExportTaskVO> exportOffers(@RequestBody @Validated ExportRequestDTO exportRequestDTO) {
        ExportTaskVO taskVO = exportTaskService.createExportTask(exportRequestDTO);
        return ResponseResult.success(taskVO);
    }

    @ApiOperation("获取导出任务状态")
    @GetMapping("/v1/export-tasks/{taskId}")
    public ResponseResult<ExportTaskVO> getExportTaskStatus(
            @ApiParam("任务ID") @PathVariable("taskId") Long taskId) {
        ExportTaskVO taskVO = exportTaskService.getExportTaskStatus(taskId);
        return ResponseResult.success(taskVO);
    }

    @ApiOperation("导入Offer数据")
    @PostMapping("/v1/offers/import")
    public ResponseResult<Map<String, Object>> importOffers(
            @ApiParam("导入文件") @RequestParam("file") MultipartFile file) {
        Map<String, Object> result = exportTaskService.importOffers(file);
        return ResponseResult.success(result);
    }
}