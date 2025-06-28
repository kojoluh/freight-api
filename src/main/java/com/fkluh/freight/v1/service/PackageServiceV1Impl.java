package com.fkluh.freight.v1.service;

import com.fkluh.freight.v1.exception.PackageAlreadyExistsException;
import com.fkluh.freight.v1.model.dto.CustomPage;
import com.fkluh.freight.v1.model.dto.PackageDto;
import com.fkluh.freight.v1.model.dto.PostcodeByCountDto;
import com.fkluh.freight.v1.exception.ErrorMessages;
import com.fkluh.freight.v1.exception.PackageNotFoundException;
import com.fkluh.freight.v1.exception.PackageValidationException;
import com.fkluh.freight.v1.mapper.PackageMapper;
import com.fkluh.freight.v1.model.Package;
import com.fkluh.freight.v1.model.DeliveryStatusEnum;
import com.fkluh.freight.v1.repository.PackageRepositoryV1;
import com.fkluh.freight.v1.service.strategy.filter.FilterStrategyByDeliveryDate;
import com.fkluh.freight.v1.service.strategy.filter.FilterStrategy;
import com.fkluh.freight.v1.service.strategy.filter.FilterStrategyByPostcode;
import com.fkluh.freight.v1.service.strategy.filter.FilterStrategyByStatus;
import com.fkluh.freight.v1.service.strategy.track.TrackStrategyByAllParameters;
import com.fkluh.freight.v1.service.strategy.track.TrackStrategyByEmailAndPostcode;
import com.fkluh.freight.v1.service.strategy.track.TrackStrategyByEmail;
import com.fkluh.freight.v1.service.strategy.track.TrackStrategyByPostcode;
import com.fkluh.freight.v1.service.strategy.track.TrackStrategyByTrackingNumber;
import com.fkluh.freight.v1.service.strategy.track.TrackStrategy;
import com.fkluh.freight.v1.util.ValidationUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class PackageServiceV1Impl implements PackageServiceV1 {

    @Autowired
    private PackageRepositoryV1 repository;
    
    @Autowired
    private PackageMapper mapper;

    /**
     * Adds a new package to the repository.
     *
     * @param packageDto the package data transfer object containing package details
     * @return the added package as a data transfer object
     */
    @Override
    @Transactional
    public PackageDto addPackage(PackageDto packageDto) {
        ValidationUtil.validate(packageDto);
        Package pkg = mapper.packageDtoToEntity(packageDto);
        validateTrackingNumberExists(pkg.getTrackingNumber());
        log.info("Adding package with tracking number: {}", pkg.getTrackingNumber());
        if (pkg.getStatus() == null) {
            pkg.setStatus(DeliveryStatusEnum.IN_TRANSIT);
        }
        Package savedPackage = repository.save(pkg);
        return mapper.packageEntityToDto(savedPackage);
    }

    /**
     * Tracks packages based on the provided tracking number, email, and postcode.
     * - If all parameters (`trackingNumber`, `email`, and `postcode`) are provided, returns a single package.
     * - If only `email` and `postcode` are provided, returns all packages for that email and postcode.
     * - If only `email` is provided, returns all packages for that email.
     * - If only `postcode` is provided, returns all packages for that postcode.
     *
     * @param trackingNumber the tracking number of the package (optional)
     * @param email the email of the package recipient (optional)
     * @param postcode the postcode of the package recipient (optional)
     * @param page the page number for pagination
     * @param size the page size for pagination
     * @return a paginated list of package data transfer objects matching the search criteria
     */
    @Override
    public CustomPage<PackageDto> trackPackages(
        String trackingNumber,
        String email,
        String postcode,
        int page,
        int size
    ) {
        validateTrackPackagePayload(trackingNumber, email, postcode);
        Pageable pageable = sanitizePagingParameters(page, size);

        List<TrackStrategy> trackStrategyList = List.of(
            new TrackStrategyByAllParameters(repository, mapper),
            new TrackStrategyByEmailAndPostcode(repository, mapper),
            new TrackStrategyByEmail(repository, mapper),
            new TrackStrategyByPostcode(repository, mapper),
            new TrackStrategyByTrackingNumber(repository, mapper)
        );

        for (TrackStrategy trackStrategy : trackStrategyList) {
            if (trackStrategy.isApplicable(trackingNumber, email, postcode)) {
                return trackStrategy.apply(trackingNumber, email, postcode, pageable);
            }
        }

        log.error(ErrorMessages.TRACKING_NUMBER_OR_EMAIL_AND_POSTCODE_EMPTY);
        throw new PackageValidationException(ErrorMessages.TRACKING_NUMBER_OR_EMAIL_AND_POSTCODE_EMPTY);
    }

    /**
     * Updates the actual delivery date of a package and sets its status to DELIVERED.
     *
     * @param trackingNumber         the tracking number of the package to update
     * @param actualDeliveryDate  the actual delivery date as a string in the format YYYY-MM-DD
     * @return the updated package as a data transfer object
     */
    @Override
    @Transactional
    public PackageDto updatePackage(String trackingNumber, String actualDeliveryDate) {
        validateUpdatePackagePayload(trackingNumber, actualDeliveryDate);
        validateTrackingNumberNotExists(trackingNumber);

        Package pkg = repository.findById(trackingNumber)
            .orElseThrow(() -> new PackageNotFoundException(String.format("Package with tracking number %s not found.", trackingNumber)));

        validatePackageToUpdate(pkg, actualDeliveryDate);

        LocalDate parsedActualDeliveryDate = LocalDate.parse(actualDeliveryDate);
        pkg.setActualDeliveryDate(parsedActualDeliveryDate);
        pkg.setStatus(DeliveryStatusEnum.DELIVERED);

        return mapper.packageEntityToDto(repository.save(pkg));
    }

    /**
     * Removes a package from the repository by its tracking number.
     *
     * @param trackingNumber the tracking number of the package to remove
     */
    @Override
    public void removePackage(String trackingNumber) {
        repository.deleteById(trackingNumber);
    }

    /**
     * Filters packages based on the provided status, postcode, or delivery date.
     * - If 'status' is provided, it filters packages based on the status (delayed or on-time).
     * - If 'postcode' is provided, it filters packages based on the recipient's postcode.
     * - If 'deliveryDate' is provided, it filters packages based on the actual delivery date.
     *
     * @param status       the status of the package (delayed or on-time)
     * @param postcode     the postcode of the package recipient
     * @param deliveryDate the actual delivery date in the format YYYY-MM-DD
     * @param page         the page number for pagination
     * @param size         the page size for pagination
     * @return a paginated page of package data transfer objects matching the filter criteria
     */
    @Override
    public CustomPage<PackageDto> filterPackages(
            String status,
            String postcode,
            String deliveryDate,
            int page,
            int size
    ) {
        validateFilterPackagesPayload(status, postcode, deliveryDate);
        Pageable pageable = sanitizePagingParameters(page, size);

        List<FilterStrategy> filters = List.of(
            new FilterStrategyByStatus(repository, mapper),
            new FilterStrategyByPostcode(repository, mapper),
            new FilterStrategyByDeliveryDate(repository, mapper)
        );

        for (FilterStrategy filter : filters) {
            if (filter.isApplicable(status, postcode, deliveryDate)) {
                return filter.apply(status, postcode, deliveryDate, pageable);
            }
        }
        throw new IllegalArgumentException(ErrorMessages.FILTER_INPUT_INVALID);
    }

    /**
     * Filters postcode with the most delayed packages.
     * This method returns a paginated list of postcodes along with the count of delayed packages for each postcode.
     *
     * @param page the page number for pagination
     * @param size the page size for pagination
     * @return a paginated page of PostcodeByCountDto containing postcodes and their respective delayed package counts
     */
    @Override
    public CustomPage<PostcodeByCountDto> filterPostcodeByMostDelayedPackages(
        int page,
        int size
    ) {
        Pageable pageable = sanitizePagingParameters(page, size);
        return mapper.postcodeEntityPageToDtoPage(repository.findPostcodeWithMostDelayedPackages(pageable));
    }


    /**
     * Validates that the new package does not exist (by tracking number).
     * @param trackingNumber the package trackingNumber to validate
     */
    private void validateTrackingNumberExists(String trackingNumber) {
        if (repository.existsById(trackingNumber)) {
            log.error(ErrorMessages.PACKAGE_ALREADY_EXISTS);
            throw new PackageAlreadyExistsException(ErrorMessages.PACKAGE_ALREADY_EXISTS);
        }
    }

    private void validateUpdatePackagePayload(String trackingNumber, String actualDeliveryDate) {
        ValidationUtil.validateNotEmpty(trackingNumber, ErrorMessages.TRACKING_NUMBER_EMPTY);
        ValidationUtil.validateNotEmpty(actualDeliveryDate, ErrorMessages.ACTUAL_DELIVERY_DATE_EMPTY);
        ValidationUtil.validateDateFormat(actualDeliveryDate, ErrorMessages.DELIVERY_DATE_INVALID_FORMAT);
    }

    private void validateTrackingNumberNotExists(String trackingNumber) {
        if (!repository.existsById(trackingNumber)) {
            log.error(ErrorMessages.TRACKING_NUMBER_NOT_EXIST);
            throw new PackageNotFoundException(ErrorMessages.TRACKING_NUMBER_NOT_EXIST);
        }
    }

    /**
     * Validates the package to update its actual delivery date.
     * @param pkg the package to validate
     * @param actualDeliveryDateStr the actual delivery date as a string in the format YYYY-MM-DD
     */
    private void validatePackageToUpdate(Package pkg, String actualDeliveryDateStr) {
        if (actualDeliveryDateStr == null) {
            log.error(ErrorMessages.ACTUAL_DELIVERY_DATE_EMPTY);
            throw new PackageValidationException(ErrorMessages.ACTUAL_DELIVERY_DATE_EMPTY);
        }
        if (LocalDate.parse(actualDeliveryDateStr).isAfter(LocalDate.now())) {
            log.error(ErrorMessages.ACTUAL_DELIVERY_DATE_CANNOT_BE_FUTURE_DATE);
            throw new PackageValidationException(ErrorMessages.ACTUAL_DELIVERY_DATE_CANNOT_BE_FUTURE_DATE);
        }
        if (pkg.getStatus() != null && pkg.getStatus().name().equals(DeliveryStatusEnum.DELIVERED.name())) {
            log.error(ErrorMessages.PACKAGE_ALREADY_DELIVERED_CANNOT_UPDATE);
            throw new PackageValidationException(ErrorMessages.PACKAGE_ALREADY_DELIVERED_CANNOT_UPDATE );
        }
    }

    /**
     * Validates the payload for tracking packages.
     * @param trackingNumber the tracking number of the package
     * @param email the email of the package recipient
     * @param postcode the postcode of the package recipient
     */
    private void validateTrackPackagePayload(
        String trackingNumber,
        String email,
        String postcode
    ) {
        if ((trackingNumber == null || trackingNumber.isEmpty()) && (email == null || email.isEmpty()) && (postcode == null || postcode.isEmpty())) {
            log.error(ErrorMessages.TRACKING_NUMBER_OR_EMAIL_AND_POSTCODE_EMPTY);
            throw new PackageValidationException(ErrorMessages.TRACKING_NUMBER_OR_EMAIL_AND_POSTCODE_EMPTY);
        }
    }

    /**
     * Validates the payload for filtering packages.
     * @param status the status of the package (delayed or on-time)
     * @param postcode the postcode of the package recipient
     * @param deliveryDate the actual delivery date in the format YYYY-MM-DD
     */
    private void validateFilterPackagesPayload(
        String status,
        String postcode,
        String deliveryDate
    ) {
        if (status != null && !status.equalsIgnoreCase("delayed") && !status.equalsIgnoreCase("on-time")) {
            log.error(ErrorMessages.FILTER_STATUS_INVALID);
            throw new PackageValidationException(ErrorMessages.FILTER_STATUS_INVALID);
        }
        if (postcode != null && postcode.isEmpty()) {
            log.error(ErrorMessages.POSTCODE_EMPTY);
            throw new PackageValidationException(ErrorMessages.POSTCODE_EMPTY);
        }
        if (deliveryDate != null && !deliveryDate.isEmpty()) {
            try {
                LocalDate.parse(deliveryDate);
            } catch (Exception e) {
                log.error(ErrorMessages.DELIVERY_DATE_INVALID_FORMAT);
                throw new PackageValidationException(ErrorMessages.DELIVERY_DATE_INVALID_FORMAT);
            }
        }
    }

    private Pageable sanitizePagingParameters(int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 10;
        if (size > 100) size = 100;
        return PageRequest.of(page, size);
    }

}