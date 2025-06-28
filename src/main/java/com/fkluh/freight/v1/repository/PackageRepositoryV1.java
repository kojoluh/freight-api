package com.fkluh.freight.v1.repository;

import com.fkluh.freight.v1.model.dto.PostcodeByCountDto;
import com.fkluh.freight.v1.model.Package;
import com.fkluh.freight.v1.model.DeliveryStatusEnum;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface PackageRepositoryV1 extends JpaRepository<Package, String> {

    @Cacheable(value = "packagesByStatusAndActualDeliveryDateAfterEstimated", key = "#status + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Query("SELECT p FROM Package p WHERE p.status = :status AND p.actualDeliveryDate > p.estimatedDeliveryDate")
    Page<Package> findByStatusAndActualDeliveryDateAfterEstimatedDeliveryDate(DeliveryStatusEnum status, Pageable pageable);

    @Cacheable(value = "packagesByStatusAndActualDeliveryDateAfterEstimated", key = "#status + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Query("SELECT p FROM Package p WHERE p.status = :status AND (p.actualDeliveryDate <= p.estimatedDeliveryDate OR p.actualDeliveryDate IS NULL) ORDER BY p.createdAt DESC")
    Page<Package> findByStatusAndActualDeliveryDateOnOrBeforeEstimatedDeliveryDate(DeliveryStatusEnum status, Pageable pageable);

    @Cacheable(value = "packagesByTrackingNumberAndEmailAndRecipientPostcode", key = "#trackingNumber + '-' + #email + '-' + #postcode + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Query("SELECT p FROM Package p WHERE p.trackingNumber = :trackingNumber AND p.email = :email AND p.recipientPostcode = :postcode ORDER BY p.createdAt DESC")
    Package findByTrackingNumberAndEmailAndRecipientPostcode(String trackingNumber, String email, String postcode);

    @Cacheable(value = "packagesDeliveredByActualDeliveryDate", key = "#deliveryDate + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Query("SELECT p FROM Package p WHERE p.status = 'DELIVERED' AND p.actualDeliveryDate = :deliveryDate ORDER BY p.createdAt DESC")
    Page<Package> findDeliveredByActualDeliveryDate(LocalDate deliveryDate, Pageable pageable);

    @Cacheable(value = "packagesDeliveredByPostcode", key = "#postcode + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Query("SELECT p FROM Package p WHERE p.status = 'DELIVERED' AND p.recipientPostcode = :postcode ORDER BY p.createdAt DESC")
    Page<Package> findDeliveredByRecipientPostcode(String postcode, Pageable pageable);

    @Cacheable(value = "packagesByPostcode", key = "#postcode + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Query("SELECT p FROM Package p WHERE p.recipientPostcode = :postcode ORDER BY p.createdAt DESC")
    Page<Package> findByRecipientPostcode(String postcode, Pageable pageable);

    @Cacheable(value = "packagesByEmail", key = "#email + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Query(value = "SELECT p FROM Package p WHERE p.email = :email ORDER BY p.createdAt DESC")
    Page<Package> findByEmail(@Param("email") String email, Pageable pageable);

    @Cacheable(value = "packagesByEmailAndPostcode", key = "#email + '-' + #postcode + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Query("SELECT p FROM Package p WHERE p.email = :email AND p.recipientPostcode = :postcode")
    Page<Package> findByEmailAndRecipientPostcode(String email, String postcode, Pageable pageable);

    @Query("SELECT p.recipientPostcode, COUNT(p) AS delayCount FROM Package p WHERE p.status = 'DELIVERED' AND p.actualDeliveryDate > p.estimatedDeliveryDate GROUP BY p.recipientPostcode ORDER BY delayCount DESC")
    Page<PostcodeByCountDto> findPostcodeWithMostDelayedPackages(Pageable pageable);

}
