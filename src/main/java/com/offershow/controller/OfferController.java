package com.offershow.controller;

import com.offershow.model.dto.OfferDTO;
import com.offershow.model.vo.OfferVO;
import com.offershow.model.vo.PageVO;
import com.offershow.service.OfferService;
import com.offershow.util.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * Offer 控制器
 */
@Api(tags = "Offer管理")
@RestController
@RequestMapping("/v1/offers")
@RequiredArgsConstructor
@Validated
public class OfferController {

    private final OfferService offerService;

    @ApiOperation("创建Offer")
    @PostMapping
    public ResponseResult<OfferVO> createOffer(@RequestBody @Validated OfferDTO offerDTO) {
        OfferVO offerVO = offerService.createOffer(offerDTO);
        return ResponseResult.success(offerVO);
    }

    @ApiOperation("更新Offer")
    @PutMapping("/{id}")
    public ResponseResult<OfferVO> updateOffer(
            @ApiParam("Offer ID") @PathVariable("id") Long id,
            @RequestBody @Validated OfferDTO offerDTO) {
        OfferVO offerVO = offerService.updateOffer(id, offerDTO);
        return ResponseResult.success(offerVO);
    }

    @ApiOperation("部分更新Offer")
    @PatchMapping("/{id}")
    public ResponseResult<OfferVO> patchOffer(
            @ApiParam("Offer ID") @PathVariable("id") Long id,
            @RequestBody OfferDTO offerDTO) {
        OfferVO offerVO = offerService.patchOffer(id, offerDTO);
        return ResponseResult.success(offerVO);
    }

    @ApiOperation("获取Offer详情")
    @GetMapping("/{id}")
    public ResponseResult<OfferVO> getOffer(
            @ApiParam("Offer ID") @PathVariable("id") Long id) {
        OfferVO offerVO = offerService.getOfferById(id);
        return ResponseResult.success(offerVO);
    }

    @ApiOperation("删除Offer")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteOffer(
            @ApiParam("Offer ID") @PathVariable("id") Long id) {
        offerService.deleteOffer(id);
        return ResponseResult.success();
    }

    @ApiOperation("搜索Offer")
    @GetMapping("/search")
    public ResponseResult<PageVO<OfferVO>> searchOffers(
            @ApiParam("搜索关键词") @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @ApiParam("页码") @RequestParam(value = "page", required = false, defaultValue = "1") @Min(1) Integer page,
            @ApiParam("每页大小") @RequestParam(value = "size", required = false, defaultValue = "10") @Min(1) @Max(100) Integer size) {
        PageVO<OfferVO> pageResult = offerService.searchOffers(keyword, page, size);
        return ResponseResult.success(pageResult);
    }
}