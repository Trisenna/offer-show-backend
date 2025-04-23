package com.offershow.model.dto;

import lombok.Data;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;


/**
 * 批量删除 Offer DTO
 */
@Data
public class BatchDeleteDTO {
    /**
     * Offer ID列表
     */
    @NotEmpty(message = "ID列表不能为空")
    @Size(max = 1000, message = "单次批量删除不能超过1000条")
    private List<Long> ids;
}
