package com.offershow.model.entity;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Offer 实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Offer {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 职位名称
     */
    private String position;

    /**
     * 工作城市
     */
    private String city;

    /**
     * 薪资结构 (JSON格式)
     */
    private String salaryStructure;

    /**
     * 工作年限
     */
    private Integer workYears;

    /**
     * 工作描述
     */
    private String jobDescription;

    /**
     * 面试流程
     */
    private String interviewProcess;

    /**
     * 面试难度 (1-5)
     */
    private Integer interviewDifficulty;

    /**
     * 是否接受Offer
     */
    private Boolean isAccepted;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 是否删除 (逻辑删除)
     */
    private Boolean isDeleted;
}