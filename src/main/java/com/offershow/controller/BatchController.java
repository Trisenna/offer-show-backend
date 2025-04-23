package com.offershow.controller;

import com.offershow.model.dto.BatchCreateDTO;
import com.offershow.model.dto.BatchDeleteDTO;
import com.offershow.model.vo.BatchResultVO;
import com.offershow.model.vo.OfferVO;
import com.offershow.service.OfferService;
import com.offershow.util.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 批处理控制器
 */
@Api(tags = "批处理管理")
@RestController
@RequestMapping("/v1/offers/batch")
@RequiredArgsConstructor
@Validated
public class BatchController {

    private final OfferService offerService;

    @ApiOperation("批量创建Offer")
    @PostMapping
    public ResponseResult<BatchResultVO<OfferVO>> batchCreateOffers(@RequestBody @Validated BatchCreateDTO batchCreateDTO) {
        List<OfferVO> successRecords = offerService.batchCreateOffers(batchCreateDTO.getOffers());

        // 构建返回结果
        int totalCount = batchCreateDTO.getOffers().size();
        int successCount = successRecords.size();
        int failedCount = totalCount - successCount;

        BatchResultVO<OfferVO> result = BatchResultVO.<OfferVO>builder()
                .successCount(successCount)
                .failedCount(failedCount)
                .successRecords(successRecords)
                .failedRecords(new ArrayList<>()) // 简化处理，不返回失败详情
                .build();

        return ResponseResult.success(result);
    }

    @ApiOperation("批量删除Offer")
    @DeleteMapping
    public ResponseResult<BatchResultVO<Object>> batchDeleteOffers(@RequestBody @Validated BatchDeleteDTO batchDeleteDTO) {
        List<Long> ids = batchDeleteDTO.getIds();
        List<Long> successIds = offerService.batchDeleteOffers(ids);

        // 计算失败的ID
        List<Long> failedIds = new ArrayList<>(ids);
        failedIds.removeAll(successIds);

        // 构建返回结果
        BatchResultVO<Object> result = BatchResultVO.builder()
                .successCount(successIds.size())
                .failedCount(failedIds.size())
                .successIds(successIds)
                .failedIds(failedIds)
                .build();

        return ResponseResult.success(result);
    }
}