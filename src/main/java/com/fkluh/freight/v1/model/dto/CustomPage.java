package com.fkluh.freight.v1.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class CustomPage<T> {
    private List<T> content;
    private int page;
    private int size;

    public boolean isEmpty() {
        return content == null || content.isEmpty();
    }

    public static <T> CustomPage<T> empty() {
        return new CustomPage<>(List.of(), 0, 0);
    }

    public static <T> CustomPage<T> of(List<T> content, int page, int size) {
        return new CustomPage<>(content, page, size);
    }


    public <R> CustomPage<R> map(java.util.function.Function<T, R> mapper) {
        List<R> mappedContent = content.parallelStream()
                .map(mapper)
                .toList();
        return new CustomPage<>(mappedContent, page, size);
    }

    public static <T> CustomPage<T> fromPage(org.springframework.data.domain.Page<T> page) {
        return new CustomPage<>(
                page.getContent(),
                page.getNumber(),
                page.getSize()
        );
    }
}
