package com.offershow.service;

import com.offershow.model.dto.OfferDTO;
import com.offershow.model.vo.OfferVO;
import com.offershow.model.vo.PageVO;

import java.util.List;

/**
 * Offer 服务接口
 */
public interface OfferService {
    /**
     * 创建 Offer
     *
     * @param offerDTO Offer 数据传输对象
     * @return 创建后的 Offer 视图对象
     */
    OfferVO createOffer(OfferDTO offerDTO);

    /**
     * 更新 Offer
     *
     * @param id       Offer ID
     * @param offerDTO Offer 数据传输对象
     * @return 更新后的 Offer 视图对象
     */
    OfferVO updateOffer(Long id, OfferDTO offerDTO);

    /**
     * 部分更新 Offer
     *
     * @param id       Offer ID
     * @param offerDTO Offer 数据传输对象
     * @return 更新后的 Offer 视图对象
     */
    OfferVO patchOffer(Long id, OfferDTO offerDTO);

    /**
     * 根据 ID 获取 Offer
     *
     * @param id Offer ID
     * @return Offer 视图对象
     */
    OfferVO getOfferById(Long id);

    /**
     * 删除 Offer
     *
     * @param id Offer ID
     * @return 是否删除成功
     */
    boolean deleteOffer(Long id);

    /**
     * 搜索 Offer
     *
     * @param keyword 关键词
     * @param page    页码
     * @param size    每页大小
     * @return 分页结果
     */
    PageVO<OfferVO> searchOffers(String keyword, int page, int size);

    /**
     * 批量创建 Offer
     *
     * @param offerDTOs Offer DTO 列表
     * @return 创建后的 Offer 列表
     */
    List<OfferVO> batchCreateOffers(List<OfferDTO> offerDTOs);

    /**
     * 批量删除 Offer
     *
     * @param ids Offer ID 列表
     * @return 删除成功的 ID 列表
     */
    List<Long> batchDeleteOffers(List<Long> ids);
}