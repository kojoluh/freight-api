package com.fkluh.freight.v1.controller;

import com.fkluh.freight.v1.exception.ErrorResponse;
import com.fkluh.freight.v1.model.DeliveryStatusEnum;
import com.fkluh.freight.v1.model.Package;
import com.fkluh.freight.v1.model.dto.CustomPage;
import com.fkluh.freight.v1.model.dto.PackageDto;
import com.fkluh.freight.v1.model.dto.PackageUpdateDto;
import com.fkluh.freight.v1.model.dto.PostcodeByCountDto;
import com.fkluh.freight.v1.repository.PackageRepositoryV1;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PackageControllerV1IntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PackageRepositoryV1 repository;

    private static final String BASE_URL = "http://localhost:";
    private static final String BASE_API_URL = "/api/v1/packages";

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    PackageDto getPackageDtoFixture() {
        PackageDto pkg = new PackageDto();
        pkg.setTrackingNumber("123456");
        pkg.setEmail("kojo@test.com");
        pkg.setRecipientPostcode("123456");
        pkg.setEstimatedDeliveryDate(LocalDate.now().plusDays(3));
        return pkg;
    }

    Package getPackageFixture(PackageDto packageDto) {
        Package pkg = new Package();
        pkg.setTrackingNumber(packageDto.getTrackingNumber());
        pkg.setEmail(packageDto.getEmail());
        pkg.setRecipientPostcode(packageDto.getRecipientPostcode());
        pkg.setEstimatedDeliveryDate(packageDto.getEstimatedDeliveryDate());
        pkg.setStatus(DeliveryStatusEnum.IN_TRANSIT);
        return pkg;
    }

    @Test
    void testAddPackage_happy_path() {
        PackageDto packageDto = getPackageDtoFixture();
        Package pkg = getPackageFixture(packageDto);

        ResponseEntity<PackageDto> response = restTemplate.postForEntity(
                BASE_URL + port + BASE_API_URL, packageDto, PackageDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTrackingNumber()).isEqualTo(pkg.getTrackingNumber());
    }

    @Test
    void testAddPackageWithMissingFields_rainy_path() {
        PackageDto packageDto = new PackageDto();
        packageDto.setTrackingNumber(null);

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                BASE_URL + port + BASE_API_URL, packageDto, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

    }

    @Test
    void testTrackPackage_happy_path() {
        PackageDto packageDto = getPackageDtoFixture();
        Package pkg = getPackageFixture(packageDto);
        repository.save(pkg);

        ResponseEntity<CustomPage<PackageDto>> response = restTemplate.exchange(
                BASE_URL + port + BASE_API_URL + "/track?trackingNumber=123456&email=kojo@test.com&recipientPostcode=123456",
                org.springframework.http.HttpMethod.GET,
                null,
                new org.springframework.core.ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent().size()).isEqualTo(1);
        assertThat(response.getBody().getContent().get(0).getTrackingNumber()).isEqualTo(pkg.getTrackingNumber());
    }

    @Test
    void testTrackPackageNotFound_rainy_path() {
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                BASE_URL + port + BASE_API_URL + "/track?trackingNumber=9876&email=test@kojo.com&recipientPostcode=123456",
                ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("PACKAGE_NOT_FOUND");
    }

    @Test
    void testUpdatePackage_happy_path() {
        String trackingNumber = UUID.randomUUID().toString();
        Package pkg = new Package();
        pkg.setTrackingNumber(trackingNumber);
        pkg.setEmail("test@kojo.com");
        pkg.setRecipientPostcode("123456");
        pkg.setEstimatedDeliveryDate(LocalDate.now().plusDays(3));
        pkg.setStatus(DeliveryStatusEnum.IN_TRANSIT);
        repository.save(pkg);

        PackageUpdateDto request = new PackageUpdateDto();
        request.setActualDeliveryDate(LocalDate.now().toString());

        restTemplate.put(BASE_URL + port + BASE_API_URL + "/" + trackingNumber, request);

        Package updatedPackage = repository.findById(trackingNumber).orElse(null);

        assertThat(updatedPackage).isNotNull();
        assertThat(updatedPackage.getStatus()).isEqualTo(DeliveryStatusEnum.DELIVERED);
    }

    @Test
    void testUpdatePackageNotFound_rainy_path() {
        PackageUpdateDto packageUpdateDto = new PackageUpdateDto();
        packageUpdateDto.setActualDeliveryDate(LocalDate.now().toString());

        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                BASE_URL + port + BASE_API_URL + "/9876",
                org.springframework.http.HttpMethod.PUT,
                new org.springframework.http.HttpEntity<>(packageUpdateDto),
                ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("PACKAGE_NOT_FOUND");
    }

    @Test
    void testRemovePackage_happy_path() {
        PackageDto packageDto = getPackageDtoFixture();
        Package pkg = getPackageFixture(packageDto);
        repository.save(pkg);

        restTemplate.delete(BASE_URL + port + BASE_API_URL + "/123456");

        assertThat(repository.findById("123456").isPresent()).isFalse();
    }

    @Test
    void testRemovePackageNotFound_rainy_path() {

        ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL + port + BASE_API_URL + "/9876",
                org.springframework.http.HttpMethod.DELETE,
                null,
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void testFilterPackages_byStatusDelayed_happy_path() {
        PackageDto packageDto = getPackageDtoFixture();
        Package pkg = getPackageFixture(packageDto);

        pkg.setActualDeliveryDate(pkg.getEstimatedDeliveryDate().plusDays(2));
        pkg.setStatus(DeliveryStatusEnum.DELIVERED);
        repository.save(pkg);

        ResponseEntity<CustomPage<PackageDto>> response = restTemplate.exchange(
                BASE_URL + port + BASE_API_URL + "/filter?status=delayed&page=0&size=10",
                org.springframework.http.HttpMethod.GET,
                null,
                new org.springframework.core.ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isNotNull();
        assertThat(response.getBody().getContent().size()).isEqualTo(1);
        assertThat(response.getBody().getContent().get(0).getTrackingNumber()).isEqualTo(pkg.getTrackingNumber());
    }

    @Test
    void testFilterPackages_byPostcode_happy_path() {
        PackageDto packageDto = getPackageDtoFixture();
        Package pkg = getPackageFixture(packageDto);
        pkg.setActualDeliveryDate(pkg.getEstimatedDeliveryDate().plusDays(1));
        pkg.setStatus(DeliveryStatusEnum.DELIVERED);
        repository.save(pkg);

        ResponseEntity<CustomPage<PackageDto>> response = restTemplate.exchange(
                BASE_URL + port + BASE_API_URL + "/filter?postcode=123456",
                org.springframework.http.HttpMethod.GET,
                null,
                new org.springframework.core.ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isNotNull();
        assertThat(response.getBody().getContent().size()).isEqualTo(1);
        assertThat(response.getBody().getContent().get(0).getTrackingNumber()).isEqualTo(pkg.getTrackingNumber());
    }

    @Test
    void testFilterPackages_byDeliveryDate_happy_path() {
        Package pkg = new Package();
        pkg.setTrackingNumber("123456");
        pkg.setEmail("test@kojo.com");
        pkg.setRecipientPostcode("123456");
        pkg.setEstimatedDeliveryDate(LocalDate.now().plusDays(3));
        pkg.setActualDeliveryDate(LocalDate.now());
        pkg.setStatus(DeliveryStatusEnum.DELIVERED);
        repository.save(pkg);

        ResponseEntity<CustomPage<PackageDto>> response = restTemplate.exchange(
                BASE_URL + port + BASE_API_URL + "/filter?deliveryDate=" + LocalDate.now(),
                org.springframework.http.HttpMethod.GET,
                null,
                new org.springframework.core.ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isNotNull();
        assertThat(response.getBody().getContent().size()).isEqualTo(1);
        assertThat(response.getBody().getContent().get(0).getTrackingNumber()).isEqualTo(pkg.getTrackingNumber());
    }

    @Test
    void testFilterPackages_findMostDelayedPostcode_happy_path() {
        PackageDto packageDto = getPackageDtoFixture();
        Package pkg = getPackageFixture(packageDto);
        pkg.setEstimatedDeliveryDate(LocalDate.now().minusDays(3));
        pkg.setActualDeliveryDate(LocalDate.now());
        pkg.setStatus(DeliveryStatusEnum.DELIVERED);
        repository.save(pkg);

        ResponseEntity<CustomPage<PostcodeByCountDto>> response = restTemplate.exchange(
                BASE_URL + port + BASE_API_URL + "/filter?findMostDelayedPostcode=true",
                org.springframework.http.HttpMethod.GET,
                null,
                new org.springframework.core.ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isNotNull();
        assertThat(response.getBody().getContent().size()).isEqualTo(1);
        assertThat(response.getBody().getContent().get(0).getRecipientPostcode()).isEqualTo(pkg.getRecipientPostcode());
    }

    @Test
    void testFilterPackages_invalidStatus_rainy_path() {
        ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL + port + BASE_API_URL + "/filter?status=invalid_status",
                org.springframework.http.HttpMethod.GET,
                null,
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testFilterPackages_conflictingParameters_rainy_path() {
        ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL + port + BASE_API_URL + "/filter?status=invalid_status&findMostDelayedPostcode=true",
                org.springframework.http.HttpMethod.GET,
                null,
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

}