package com.fkluh.freight.v1.service.strategy.filter;

import com.fkluh.freight.v1.exception.PackageValidationException;
import com.fkluh.freight.v1.model.dto.CustomPage;
import com.fkluh.freight.v1.model.dto.PackageDto;
import com.fkluh.freight.v1.exception.ErrorMessages;
import com.fkluh.freight.v1.mapper.PackageMapper;
import com.fkluh.freight.v1.repository.PackageRepositoryV1;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@AllArgsConstructor
public class FilterStrategyByDeliveryDate implements FilterStrategy {

    @Autowired
    private PackageRepositoryV1 repository;

    @Autowired
    private PackageMapper mapper;

    @Override
    public boolean isApplicable(String status, String postcode, String deliveryDate) {
        return deliveryDate != null && !deliveryDate.isEmpty();
    }

    @Override
    public CustomPage<PackageDto> apply(String status, String postcode, String deliveryDate, Pageable pageable) {
        LocalDate parsedDeliveryDate = sanitizeFilterInputDate(deliveryDate);
        return mapper.packageEntityPageToDtoPage(repository.findDeliveredByActualDeliveryDate(parsedDeliveryDate, pageable));
    }

    private LocalDate sanitizeFilterInputDate(String date) {
        LocalDate parsedDate = null;
        if (date != null && !date.isEmpty()) {
            try {
                parsedDate = LocalDate.parse(date);
            } catch (Exception e) {
                throw new PackageValidationException(ErrorMessages.DELIVERY_DATE_INVALID_FORMAT);
            }
        }
        return parsedDate;
    }
}
