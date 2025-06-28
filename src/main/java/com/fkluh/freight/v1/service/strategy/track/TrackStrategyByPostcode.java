package com.fkluh.freight.v1.service.strategy.track;

import com.fkluh.freight.v1.exception.PackageNotFoundException;
import com.fkluh.freight.v1.model.dto.CustomPage;
import com.fkluh.freight.v1.model.dto.PackageDto;
import com.fkluh.freight.v1.mapper.PackageMapper;
import com.fkluh.freight.v1.model.Package;
import com.fkluh.freight.v1.repository.PackageRepositoryV1;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TrackStrategyByPostcode implements TrackStrategy {
    @Autowired
    private PackageRepositoryV1 repository;
    @Autowired
    private PackageMapper mapper;

    @Override
    public boolean isApplicable(String trackingNumber, String email, String postcode) {
        return postcode != null && !postcode.isEmpty();
    }

    @Override
    public CustomPage<PackageDto> apply(String trackingNumber, String email, String postcode, Pageable pageable) {
        Page<Package> packagePage = repository.findByRecipientPostcode(postcode, pageable);
        if (packagePage == null || packagePage.isEmpty()) throw new PackageNotFoundException(String.format("Package with postcode %s not found.", postcode));
        return mapper.packageEntityPageToDtoPage(packagePage);
    }
}
