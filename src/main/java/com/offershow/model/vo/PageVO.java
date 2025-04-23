package com.offershow.model.vo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 分页视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageVO<T> {
    /**
     * 总记录数
     */
    private long total;

    /**
     * 总页数
     */
    private int pages;

    /**
     * 当前页码
     */
    private int current;

    /**
     * 每页大小
     */
    private int size;

    /**
     * 记录列表
     */
    private List<T> records;
}
