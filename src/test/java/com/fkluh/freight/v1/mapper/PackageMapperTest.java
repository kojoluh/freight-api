package com.fkluh.freight.v1.mapper;

import com.fkluh.freight.v1.model.dto.PackageDto;
import com.fkluh.freight.v1.model.Package;
import com.fkluh.freight.v1.model.DeliveryStatusEnum;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PackageMapperTest {

    private final PackageMapper mapper = new PackageMapper();

    @Test
    void packageDtoToEntity_happyPath() {
        PackageDto dto = new PackageDto();
        dto.setTrackingNumber("12345");
        dto.setEmail("test@kojo.com");
        dto.setRecipientPostcode("12345");
        dto.setEstimatedDeliveryDate(LocalDate.now().plusDays(3));
        dto.setActualDeliveryDate(LocalDate.now());
        dto.setStatus(DeliveryStatusEnum.IN_TRANSIT.name());

        Package entity = mapper.packageDtoToEntity(dto);

        assertNotNull(entity);
        assertEquals(dto.getTrackingNumber(), entity.getTrackingNumber());
        assertEquals(dto.getEmail(), entity.getEmail());
        assertEquals(dto.getRecipientPostcode(), entity.getRecipientPostcode());
        assertEquals(dto.getEstimatedDeliveryDate(), entity.getEstimatedDeliveryDate());
        assertEquals(dto.getActualDeliveryDate(), entity.getActualDeliveryDate());
        assertEquals(DeliveryStatusEnum.IN_TRANSIT, entity.getStatus());
    }

    @Test
    void packageDtoToEntity_nullDto() {
        Package entity = mapper.packageDtoToEntity(null);
        assertNull(entity);
    }

    @Test
    void packageEntityToDto_happyPath() {
        Package entity = new Package();
        entity.setTrackingNumber("12345");
        entity.setEmail("test@kojo.com");
        entity.setRecipientPostcode("12345");
        entity.setEstimatedDeliveryDate(LocalDate.now().plusDays(3));
        entity.setActualDeliveryDate(LocalDate.now());
        entity.setStatus(DeliveryStatusEnum.IN_TRANSIT);

        PackageDto dto = mapper.packageEntityToDto(entity);

        assertNotNull(dto);
        assertEquals(entity.getTrackingNumber(), dto.getTrackingNumber());
        assertEquals(entity.getEmail(), dto.getEmail());
        assertEquals(entity.getRecipientPostcode(), dto.getRecipientPostcode());
        assertEquals(entity.getEstimatedDeliveryDate(), dto.getEstimatedDeliveryDate());
        assertEquals(entity.getActualDeliveryDate(), dto.getActualDeliveryDate());
        assertEquals(DeliveryStatusEnum.IN_TRANSIT.name(), dto.getStatus());
    }

    @Test
    void packageEntityToDto_nullEntity() {
        PackageDto dto = mapper.packageEntityToDto(null);
        assertNull(dto);
    }
}