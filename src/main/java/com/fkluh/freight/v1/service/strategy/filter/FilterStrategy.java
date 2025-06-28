package com.fkluh.freight.v1.service.strategy.filter;

import com.fkluh.freight.v1.model.dto.CustomPage;
import com.fkluh.freight.v1.model.dto.PackageDto;
import org.springframework.data.domain.Pageable;

public interface FilterStrategy {
    boolean isApplicable(String status, String postcode, String deliveryDate);
    CustomPage<PackageDto> apply(String status, String postcode, String deliveryDate, Pageable pageable);
}