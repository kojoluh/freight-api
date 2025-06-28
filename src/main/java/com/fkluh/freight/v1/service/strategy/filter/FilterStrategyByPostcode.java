package com.fkluh.freight.v1.service.strategy.filter;

import com.fkluh.freight.v1.model.dto.CustomPage;
import com.fkluh.freight.v1.model.dto.PackageDto;
import com.fkluh.freight.v1.mapper.PackageMapper;
import com.fkluh.freight.v1.repository.PackageRepositoryV1;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class FilterStrategyByPostcode implements FilterStrategy {

    @Autowired
    private PackageRepositoryV1 repository;

    @Autowired
    private PackageMapper mapper;

    @Override
    public boolean isApplicable(String status, String postcode, String deliveryDate) {
        return postcode != null && !postcode.isEmpty();
    }

    @Override
    public CustomPage<PackageDto> apply(String status, String postcode, String deliveryDate, Pageable pageable) {
        return mapper.packageEntityPageToDtoPage(repository.findDeliveredByRecipientPostcode(postcode, pageable));
    }
}
