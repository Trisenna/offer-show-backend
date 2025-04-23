package com.offershow.model.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Offer 数据传输对象
 */
@Data
public class OfferDTO {
    /**
     * 公司名称
     */
    @NotBlank(message = "公司名称不能为空")
    @Length(max = 100, message = "公司名称长度不能超过100")
    private String companyName;

    /**
     * 职位名称
     */
    @NotBlank(message = "职位名称不能为空")
    @Length(max = 100, message = "职位名称长度不能超过100")
    private String position;

    /**
     * 工作城市
     */
    @NotBlank(message = "工作城市不能为空")
    @Length(max = 50, message = "工作城市长度不能超过50")
    private String city;

    /**
     * 薪资结构 (JSON对象)
     */
    @NotNull(message = "薪资结构不能为空")
    private Object salaryStructure;

    /**
     * 工作年限
     */
    @NotNull(message = "工作年限不能为空")
    @Min(value = 0, message = "工作年限不能小于0")
    @Max(value = 50, message = "工作年限不能大于50")
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
    @Min(value = 1, message = "面试难度最小为1")
    @Max(value = 5, message = "面试难度最大为5")
    private Integer interviewDifficulty;

    /**
     * 是否接受Offer
     */
    private Boolean isAccepted;

    /**
     * 拒绝原因
     */
    @Length(max = 255, message = "拒绝原因长度不能超过255")
    private String rejectReason;
}