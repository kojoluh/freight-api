package com.fkluh.freight.v1.service;

import com.fkluh.freight.v1.model.dto.CustomPage;
import com.fkluh.freight.v1.model.dto.PackageDto;
import com.fkluh.freight.v1.model.dto.PostcodeByCountDto;

public interface PackageServiceV1 {
    PackageDto addPackage(PackageDto pkg);

    CustomPage<PackageDto> trackPackages(String trackingNumber, String email, String postcode, int page, int size);

    PackageDto updatePackage(String trackingNumber, String actualDeliveryDate);

    void removePackage(String trackingNumber);

    CustomPage<PackageDto> filterPackages(String status, String postcode, String deliveryDate, int page, int size);

    CustomPage<PostcodeByCountDto> filterPostcodeByMostDelayedPackages(int page, int size);
}