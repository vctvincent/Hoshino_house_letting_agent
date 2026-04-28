package com.reams.common.result;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

// 分页结果类
@Data

public class PageResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    // 总记录数
    private Long total;
    // 当前页码
    private Integer pageNum;
    // 每页大小
    private Integer pageSize;
    // 总页数
    private Integer pages;
    // 数据列表
    private List<T> list;

    public PageResult() {
    }

    public PageResult(Long total, List<T> list) {
        this.total = total;
        this.list = list;
    }

    public PageResult(Long total, Integer pageNum, Integer pageSize, List<T> list) {
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = (int) Math.ceil((double) total / pageSize);
        this.list = list;
    }

    public static <T> PageResult<T> of(Long total, List<T> list) {
        return new PageResult<>(total, list);
    }

    public static <T> PageResult<T> of(Long total, Integer pageNum, Integer pageSize, List<T> list) {
        return new PageResult<>(total, pageNum, pageSize, list);
    }
}
