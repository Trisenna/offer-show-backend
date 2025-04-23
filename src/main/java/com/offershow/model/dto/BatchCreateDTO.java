package com.offershow.model.dto;

import lombok.Data;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 批量创建 Offer DTO
 */
@Data
public class BatchCreateDTO {
    /**
     * Offer列表
     */
    @NotEmpty(message = "Offer列表不能为空")
    @Size(max = 1000, message = "单次批量创建不能超过1000条")
    @Valid
    private List<OfferDTO> offers;
}

