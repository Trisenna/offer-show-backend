package com.offershow.model.dto;

import lombok.Data;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 导出请求 DTO
 */
@Data
public class ExportRequestDTO {
    /**
     * 导出文件格式 (csv/excel)
     */
    private String format = "csv";

    /**
     * 过滤条件
     */
    private ExportFilters filters;

    /**
     * 导出文件名
     */
    private String fileName;

    @Data
    public static class ExportFilters {
        /**
         * 公司名称列表
         */
        private List<String> companies;

        /**
         * 城市列表
         */
        private List<String> cities;

        /**
         * 开始日期
         */
        private String startDate;

        /**
         * 结束日期
         */
        private String endDate;
    }
}
