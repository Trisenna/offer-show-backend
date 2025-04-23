package com.offershow.repository;

import com.offershow.model.entity.Offer;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Offer 仓库接口
 */
public interface OfferRepository {
    /**
     * 插入 Offer
     *
     * @param offer Offer 实体
     * @return 影响行数
     */
    int insert(Offer offer);

    /**
     * 更新 Offer
     *
     * @param offer Offer 实体
     * @return 影响行数
     */
    int update(Offer offer);

    /**
     * 根据 ID 查询 Offer
     *
     * @param id Offer ID
     * @return Offer 实体
     */
    Offer findById(Long id);

    /**
     * 根据关键词查询 Offer
     *
     * @param keyword 关键词
     * @param offset  偏移量
     * @param limit   限制数量
     * @return Offer 列表
     */
    List<Offer> findByKeyword(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 统计符合关键词的 Offer 数量
     *
     * @param keyword 关键词
     * @return 数量
     */
    int countByKeyword(@Param("keyword") String keyword);

    /**
     * 根据 ID 列表批量查询 Offer
     *
     * @param ids ID 列表
     * @return Offer 列表
     */
    List<Offer> findByIds(@Param("ids") List<Long> ids);

    /**
     * 查询指定时间范围内的所有 Offer
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return Offer 列表
     */
    List<Offer> findByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);
}