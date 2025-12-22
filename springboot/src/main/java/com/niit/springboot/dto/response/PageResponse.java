package com.niit.springboot.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class PageResponse<T> {
    private Integer page;
    private Integer size;
    private Integer total;
    private Integer totalPages;
    private List<T> items;

    public PageResponse(Integer page, Integer size, Integer total, List<T> items) {
        this.page = page;
        this.size = size;
        this.total = total;
        this.totalPages = (int) Math.ceil((double) total / size);
        this.items = items;
    }
}