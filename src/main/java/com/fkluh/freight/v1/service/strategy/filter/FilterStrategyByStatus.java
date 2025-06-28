package com.fkluh.freight.v1.service.strategy.filter;

import com.fkluh.freight.v1.model.dto.CustomPage;
import com.fkluh.freight.v1.model.dto.PackageDto;
import com.fkluh.freight.v1.model.DeliveryStatusEnum;
import com.fkluh.freight.v1.repository.PackageRepositoryV1;
import com.fkluh.freight.v1.mapper.PackageMapper;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class FilterStrategyByStatus implements FilterStrategy {
    @Autowired
    private PackageRepositoryV1 repository;

    @Autowired
    private PackageMapper mapper;

    @Override
    public boolean isApplicable(String status, String postcode, String deliveryDate) {
        return status != null && (status.equalsIgnoreCase(DeliveryStatusEnum.DELAYED.name().toLowerCase())
            || status.equalsIgnoreCase(DeliveryStatusEnum.ON_TIME.name().toLowerCase()));
    }

    @Override
    public CustomPage<PackageDto> apply(String status, String postcode, String deliveryDate, Pageable pageable) {
        if (status.equalsIgnoreCase(DeliveryStatusEnum.DELAYED.name().toLowerCase())) {
            return mapper.packageEntityPageToDtoPage(repository.findByStatusAndActualDeliveryDateAfterEstimatedDeliveryDate(DeliveryStatusEnum.DELIVERED, pageable));
        } else if (status.equalsIgnoreCase(DeliveryStatusEnum.ON_TIME.name().toLowerCase())) {
            return mapper.packageEntityPageToDtoPage(repository.findByStatusAndActualDeliveryDateOnOrBeforeEstimatedDeliveryDate(DeliveryStatusEnum.DELIVERED, pageable));
        }
        return CustomPage.empty();
    }
}