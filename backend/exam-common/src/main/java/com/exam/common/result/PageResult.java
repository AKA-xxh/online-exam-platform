package com.exam.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页返回格式
 * <p>
 * 所有分页查询接口统一使用此格式包装数据。
 * data.records = 当前页数据列表
 * data.total = 总记录数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前页数据 */
    private List<T> records;

    /** 总记录数 */
    private long total;

    /** 当前页码 */
    private long page;

    /** 每页大小 */
    private long pageSize;

    /** 总页数 */
    private long totalPages;

    /**
     * 快捷构建空分页
     */
    public static <T> PageResult<T> empty(long page, long pageSize) {
        return new PageResult<>(Collections.emptyList(), 0, page, pageSize, 0);
    }

    /**
     * 从 MyBatis-Plus 分页对象构建
     */
    public static <T> PageResult<T> from(com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> mpPage) {
        return new PageResult<>(
                mpPage.getRecords(),
                mpPage.getTotal(),
                mpPage.getCurrent(),
                mpPage.getSize(),
                mpPage.getPages()
        );
    }
}
