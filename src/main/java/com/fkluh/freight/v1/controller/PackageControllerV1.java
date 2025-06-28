package com.fkluh.freight.v1.controller;

import com.fkluh.freight.v1.model.dto.CustomPage;
import com.fkluh.freight.v1.model.dto.PackageDto;
import com.fkluh.freight.v1.model.dto.PackageUpdateDto;
import com.fkluh.freight.v1.service.PackageServiceV1Impl;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController("packageControllerV1")
@RequestMapping("/api/v1/packages")
public class PackageControllerV1 {

    private PackageServiceV1Impl service;

    public PackageControllerV1(PackageServiceV1Impl service) {}

    @Operation(summary = "Add a new package. Creates a new package in the system.\n"
        + "The package must have a tracking number, email, recipient postcode, and estimated delivery date.\n"
        + "The actual delivery date is optional and can be updated later."
    )
    @PostMapping
    public ResponseEntity<PackageDto> addPackage(@Valid @RequestBody PackageDto pkg) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addPackage(pkg));
    }

    @Operation(summary = "Tracks package(s) by its tracking number, email, and "
        + "recipient postcode. If all parameters are provided, it returns a single package;"
        + "if only email and postcode are provided, it returns all packages for that email and postcode;"
        + "if only email is provided, it returns all packages for that email;"
        + "if only postcode is provided, it returns all packages for that postcode."
    )
    @GetMapping("/track")
    public ResponseEntity<CustomPage<PackageDto>> trackPackage(
        @RequestParam(required = false) String trackingNumber,
        @RequestParam(required = false) String email,
        @RequestParam(required = false) String recipientPostcode,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.trackPackages(trackingNumber, email, recipientPostcode, page, size));
    }

    @Operation(
        summary = "Updates the delivery details of a package.\n"
            + "The actual delivery date is required to update the package status to DELIVERED."
    )
    @PutMapping("/{trackingNumber}")
    public ResponseEntity<PackageDto> updatePackage(
        @PathVariable String trackingNumber,
        @Valid @RequestBody PackageUpdateDto pkgUpdateDto
    ) {
        return ResponseEntity.ok(service.updatePackage(trackingNumber, pkgUpdateDto.getActualDeliveryDate()));
    }

    @Operation(summary = "Remove a package. Deletes a package by its tracking number.")
    @DeleteMapping("/{trackingNumber}")
    public ResponseEntity<Void> removePackage(@PathVariable String trackingNumber) {
        service.removePackage(trackingNumber);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Filters packages by status, postcode, delivery date, "
        + "or finds the postcode with the most delayed packages. If 'findMostDelayedPostcode' is true, it returns "
        + "the postcode with the most delayed packages. If 'status', 'postcode', and 'deliveryDate' are provided, "
        + "it filters packages accordingly."
    )
    @GetMapping("/filter")
    public ResponseEntity<?> filterPackages(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String postcode,
        @RequestParam(required = false) String deliveryDate,
        @RequestParam(required = false, defaultValue = "false") boolean findMostDelayedPostcode,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        if (findMostDelayedPostcode &&
            ((status == null || status.isEmpty()) &&
            (postcode == null || postcode.isEmpty()) &&
            (deliveryDate == null || deliveryDate.isEmpty())
        )) {
            return ResponseEntity.ok(service.filterPostcodeByMostDelayedPackages(page, size));
        }
        return ResponseEntity.ok(service.filterPackages(status, postcode, deliveryDate, page, size));
    }
}