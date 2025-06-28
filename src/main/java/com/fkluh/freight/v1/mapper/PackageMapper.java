package com.fkluh.freight.v1.mapper;

import com.fkluh.freight.v1.model.dto.CustomPage;
import com.fkluh.freight.v1.model.dto.PackageDto;
import com.fkluh.freight.v1.model.dto.PostcodeByCountDto;
import com.fkluh.freight.v1.model.Package;
import com.fkluh.freight.v1.model.DeliveryStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
public class PackageMapper {

    public Package packageDtoToEntity(PackageDto dto) {
        if (dto == null) {
            return null;
        }

        Package entity = new Package();
        entity.setTrackingNumber(dto.getTrackingNumber());
        entity.setEmail(dto.getEmail());
        entity.setRecipientPostcode(dto.getRecipientPostcode());
        entity.setEstimatedDeliveryDate(dto.getEstimatedDeliveryDate());
        entity.setActualDeliveryDate(dto.getActualDeliveryDate());
        entity.setStatus(dto.getStatus() != null ? DeliveryStatusEnum.valueOf(dto.getStatus()) : null);
        return entity;
    }

    public PackageDto packageEntityToDto(Package entity) {
        if (entity == null) {
            return null;
        }

        PackageDto dto = new PackageDto();
        dto.setTrackingNumber(entity.getTrackingNumber());
        dto.setEmail(entity.getEmail());
        dto.setRecipientPostcode(entity.getRecipientPostcode());
        dto.setEstimatedDeliveryDate(entity.getEstimatedDeliveryDate());
        dto.setActualDeliveryDate(entity.getActualDeliveryDate());
        dto.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        return dto;
    }

    public CustomPage<PackageDto> packageEntityPageToDtoPage(Page<Package> page) {
        if (page == null) {
            return new CustomPage<>(List.of(), 0, 0);
        }
        List<PackageDto> dtoList = page.getContent().stream()
            .map(this::packageEntityToDto)
            .toList();
        return new CustomPage<>(dtoList, page.getNumber(), page.getSize());
    }

    public CustomPage<PackageDto> singleEntityToDtoPage(Package pkg) {
        if (pkg == null) {
            return new CustomPage<>(List.of(), 0, 0);
        }
        PackageDto dto = packageEntityToDto(pkg);
        return new CustomPage<>(List.of(dto), 0, 1);
    }

    public CustomPage<PostcodeByCountDto> postcodeEntityPageToDtoPage(Page<PostcodeByCountDto> page) {
        if (page == null) {
            return new CustomPage<>(List.of(), 0, 0);
        }
        return new CustomPage<>(page.map(Function.identity()).getContent(), page.getNumber(), page.getSize());
    }

}