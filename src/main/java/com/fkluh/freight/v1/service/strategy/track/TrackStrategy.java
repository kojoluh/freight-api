package com.fkluh.freight.v1.service.strategy.track;

import com.fkluh.freight.v1.model.dto.CustomPage;
import com.fkluh.freight.v1.model.dto.PackageDto;
import org.springframework.data.domain.Pageable;

public interface TrackStrategy {
    boolean isApplicable(String trackingNumber, String email, String postcode);
    CustomPage<PackageDto> apply(String trackingNumber, String email, String postcode, Pageable pageable);
}
