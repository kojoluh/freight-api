package com.fkluh.freight.v1.service;

import com.fkluh.freight.v1.model.dto.CustomPage;
import com.fkluh.freight.v1.model.dto.PackageDto;
import com.fkluh.freight.v1.model.dto.PackageUpdateDto;
import com.fkluh.freight.v1.model.dto.PostcodeByCountDto;
import com.fkluh.freight.v1.exception.PackageAlreadyExistsException;
import com.fkluh.freight.v1.exception.PackageNotFoundException;
import com.fkluh.freight.v1.exception.PackageValidationException;
import com.fkluh.freight.v1.mapper.PackageMapper;
import com.fkluh.freight.v1.model.Package;
import com.fkluh.freight.v1.model.DeliveryStatusEnum;
import com.fkluh.freight.v1.repository.PackageRepositoryV1;
import com.fkluh.freight.v1.service.strategy.filter.FilterStrategy;
import com.fkluh.freight.v1.service.strategy.track.TrackStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

class PackageServiceV1ImplTest {

    @Mock
    private PackageRepositoryV1 repository;

    @Mock
    private PackageMapper mapper;

    @Mock
    private List<FilterStrategy> filterStrategies;

    @Mock
    private List<TrackStrategy> trackStrategies;

    @InjectMocks
    private PackageServiceV1Impl service;

    private Package testPackage;
    private PackageDto testPackageDto;

    private CustomPage<Package> testCustomPage;
    private Page<Package> testPage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testPackage = new Package();
        testPackage.setTrackingNumber("123456");
        testPackage.setEmail("test@kojo.com");
        testPackage.setRecipientPostcode("123456");
        testPackage.setEstimatedDeliveryDate(LocalDate.now().plusDays(1));
        testPackage.setStatus(DeliveryStatusEnum.IN_TRANSIT);

        testPackageDto = new PackageDto();
        testPackageDto.setTrackingNumber("123456");
        testPackageDto.setEmail("test@kojo.com");
        testPackageDto.setRecipientPostcode("123456");
        testPackageDto.setEstimatedDeliveryDate(LocalDate.now().plusDays(1));
        testPackageDto.setStatus(DeliveryStatusEnum.IN_TRANSIT.name());

        testPage = new PageImpl<>(List.of(testPackage));
        testCustomPage = new CustomPage<>(List.of(testPackage), 0, 10);
    }

    @Test
    void testAddPackage_happy_path() {
        when(mapper.packageDtoToEntity(testPackageDto)).thenReturn(testPackage);
        when(repository.save(any())).thenReturn(testPackage);
        when(mapper.packageEntityToDto(testPackage)).thenReturn(testPackageDto);

        PackageDto result = service.addPackage(testPackageDto);

        assertThat(result).isNotNull();
        assertThat(result.getTrackingNumber()).isEqualTo("123456");
        verify(repository, times(1)).save(any());
    }

    @Test
    void testAddPackage_defaultStatusSet_happy_path() {
        testPackageDto.setStatus(null);
        when(mapper.packageDtoToEntity(testPackageDto)).thenReturn(testPackage);
        when(repository.save(any())).thenReturn(testPackage);
        when(mapper.packageEntityToDto(testPackage)).thenReturn(testPackageDto);

        PackageDto result = service.addPackage(testPackageDto);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(DeliveryStatusEnum.IN_TRANSIT.name());
        verify(repository, times(1)).save(any());
    }


    @Test
    void testAddPackage_trackingNumber_exists_rainy_path() {
        when(mapper.packageDtoToEntity(testPackageDto)).thenReturn(testPackage);
        when(repository.existsById(testPackage.getTrackingNumber())).thenReturn(true);

        assertThrows(PackageAlreadyExistsException.class, () -> service.addPackage(testPackageDto));

        verify(repository, never()).save(any());
    }

    @Test
    void testTrackPackages_by_trackingNumber_happy_path() {
        when(repository.findById("123456")).thenReturn(Optional.of(testPackage));
        when(mapper.packageEntityToDto(testPackage)).thenReturn(testPackageDto);

        CustomPage<PackageDto> result = service.trackPackages("123456", null, null, 0, 10);

        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getTrackingNumber()).isEqualTo("123456");
        verify(repository, times(1)).findById("123456");
    }

    @Test
    void testTrackPackages_by_trackingNumber_email_postCode_happy_path() {
        when(repository.findByTrackingNumberAndEmailAndRecipientPostcode(anyString(), anyString(), anyString())).thenReturn(testPackage);
        when(mapper.packageEntityToDto(testPackage)).thenReturn(testPackageDto);

        CustomPage<PackageDto> result = service.trackPackages(testPackageDto.getTrackingNumber(), testPackageDto.getEmail(), testPackageDto.getRecipientPostcode(), 0, 10);
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getTrackingNumber()).isEqualTo("123456");
        verify(repository, times(1)).findByTrackingNumberAndEmailAndRecipientPostcode(anyString(), anyString(), anyString());
    }


    @Test
    void testTrackPackages_by_email_postCode_happy_path() {
        when(repository.findByEmailAndRecipientPostcode(anyString(), anyString(), any())).thenReturn(testPage);
        when(mapper.packageEntityToDto(testPackage)).thenReturn(testPackageDto);

        CustomPage<PackageDto> result = service.trackPackages(null, "test@kojo.com", "123456", 0, 10);

        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getTrackingNumber()).isEqualTo("123456");
        verify(repository, times(1)).findByEmailAndRecipientPostcode(anyString(), anyString(), any());
    }

    @Test
    void testTrackPackages_notFound_rainy_path() {
        when(repository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(PackageNotFoundException.class, () -> service.trackPackages("123456", null, null, 0, 10));
        verify(repository, times(1)).findById("123456");
    }

    @Test
    void testTrackPackages_byEmail_happy_path() {
        when(repository.findByEmail(anyString(), any())).thenReturn(testPage);
        when(mapper.packageEntityToDto(testPackage)).thenReturn(testPackageDto);

        CustomPage<PackageDto> result = service.trackPackages(null, "test@kojo.com", null, 0, 10);

        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getTrackingNumber()).isEqualTo("123456");
        verify(repository, times(1)).findByEmail(anyString(), any());
    }

    @Test
    void testTrackPackages_byEmail_notFound() {
        when(repository.findByEmail(anyString(), any())).thenReturn(Page.empty());

        assertThrows(PackageNotFoundException.class, () -> service.trackPackages(null, "test@kojo.com", null, 0, 10));
        verify(repository, times(1)).findByEmail(anyString(), any());
    }

    @Test
    void testTrackPackages_byPostcode_happy_path() {
        when(repository.findByRecipientPostcode(anyString(), any())).thenReturn(testPage);
        when(mapper.packageEntityToDto(testPackage)).thenReturn(testPackageDto);

        CustomPage<PackageDto> result = service.trackPackages(null, null, "12345", 0, 10);

        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getTrackingNumber()).isEqualTo("123456");
        verify(repository, times(1)).findByRecipientPostcode(anyString(), any());
    }

    @Test
    void testTrackPackages_byPostcode_notFound() {
        when(repository.findByRecipientPostcode(anyString(), any())).thenReturn(Page.empty());

        assertThrows(PackageNotFoundException.class, () -> service.trackPackages(null, null, "12345", 0, 10));
        verify(repository, times(1)).findByRecipientPostcode(anyString(), any());
    }

    @Test
    void testTrackPackages_missingParameters_rainy_path() {
        assertThrows(PackageValidationException.class, () -> service.trackPackages(null, null, null, 0, 10));
    }

    @Test
    void testUpdatePackage_happy_path() {
        PackageUpdateDto packageUpdateDto = new PackageUpdateDto();
        packageUpdateDto.setActualDeliveryDate(LocalDate.now().minusDays(2).toString());
        when(repository.findById(anyString())).thenReturn(Optional.of(testPackage));

        Package testUpdatedPackage = getFixtureTestUpdatedPackage(packageUpdateDto);

        PackageDto testUpdatedPackageDto = getFixtureTestUpdatedPackageDto(testUpdatedPackage);


        when(repository.save(any())).thenReturn(testUpdatedPackage);
        when(mapper.packageEntityToDto(testUpdatedPackage)).thenReturn(testUpdatedPackageDto);

        PackageDto result = service.updatePackage(testPackage.getTrackingNumber(), packageUpdateDto.getActualDeliveryDate());

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(DeliveryStatusEnum.DELIVERED.name());
        verify(repository, times(1)).findById(anyString());
        verify(repository, times(1)).save(any(Package.class));
    }

    private PackageDto getFixtureTestUpdatedPackageDto(Package testUpdatedPackage) {
        PackageDto testUpdatedPackageDto = new PackageDto();
        testUpdatedPackageDto.setTrackingNumber(testUpdatedPackage.getTrackingNumber());
        testUpdatedPackageDto.setEmail(testUpdatedPackage.getEmail());
        testUpdatedPackageDto.setRecipientPostcode(testUpdatedPackage.getRecipientPostcode());
        testUpdatedPackageDto.setEstimatedDeliveryDate(testUpdatedPackage.getEstimatedDeliveryDate());
        testUpdatedPackageDto.setActualDeliveryDate(testUpdatedPackage.getActualDeliveryDate());
        testUpdatedPackageDto.setStatus(testUpdatedPackage.getStatus().name());
        return testUpdatedPackageDto;
    }

    private Package getFixtureTestUpdatedPackage(PackageUpdateDto packageUpdateDto) {
        Package testUpdatedPackage = new Package();
        testUpdatedPackage.setTrackingNumber(testPackage.getTrackingNumber());
        testUpdatedPackage.setEmail(testPackage.getEmail());
        testUpdatedPackage.setRecipientPostcode(testPackage.getRecipientPostcode());
        testUpdatedPackage.setEstimatedDeliveryDate(testPackage.getEstimatedDeliveryDate());
        testUpdatedPackage.setActualDeliveryDate(LocalDate.parse(packageUpdateDto.getActualDeliveryDate()));
        testUpdatedPackage.setStatus(DeliveryStatusEnum.DELIVERED);
        return testUpdatedPackage;
    }

    @Test
    void testUpdatePackage_notFound_rainy_path() {
        when(repository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(PackageNotFoundException.class, () -> service.updatePackage("123456", LocalDate.now().toString()));
        verify(repository, never()).save(any(Package.class));
    }

    @Test
    void testFilterPackagesByStatus_happy_path() {
        when(repository.findByStatusAndActualDeliveryDateAfterEstimatedDeliveryDate(any(), any()))
                .thenReturn(testPage);
        when(mapper.packageEntityToDto(testPackage)).thenReturn(testPackageDto);

        CustomPage<PackageDto> result = service.filterPackages("delayed", null, null, 0, 10);

        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getTrackingNumber()).isEqualTo("123456");
        verify(repository, times(1)).findByStatusAndActualDeliveryDateAfterEstimatedDeliveryDate(any(), any());
    }

    @Test
    void testFilterPackages_invalidStatus() {
        assertThrows(PackageValidationException.class, () -> service.filterPackages("invalid_status", null, null, 0, 10));
        verify(repository, never()).findByStatusAndActualDeliveryDateAfterEstimatedDeliveryDate(any(), any());
    }

    @Test
    void testRemovePackage_happy_path() {
        doNothing().when(repository).deleteById(anyString());

        service.removePackage("123456");

        verify(repository, times(1)).deleteById(anyString());
    }

    @Test
    void testRemovePackage_notFound() {
        doThrow(new PackageNotFoundException("Package not found")).when(repository).deleteById(anyString());

        assertThrows(PackageNotFoundException.class, () -> service.removePackage("123456"));
        verify(repository, times(1)).deleteById(anyString());
    }

    @Test
    void testFilterPackages_byStatusDelayed_happy_path() {
        when(repository.findByStatusAndActualDeliveryDateAfterEstimatedDeliveryDate(any(), any()))
                .thenReturn(testPage);
        when(mapper.packageEntityToDto(testPackage)).thenReturn(testPackageDto);

        CustomPage<PackageDto> result = service.filterPackages("delayed", null, null, 0, 10);

        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getTrackingNumber()).isEqualTo("123456");
        verify(repository, times(1)).findByStatusAndActualDeliveryDateAfterEstimatedDeliveryDate(any(), any());
    }

    @Test
    void testFilterPackages_byStatusOnTime_happy_path() {
        when(repository.findByStatusAndActualDeliveryDateOnOrBeforeEstimatedDeliveryDate(any(), any()))
                .thenReturn(testPage);
        when(mapper.packageEntityToDto(testPackage)).thenReturn(testPackageDto);

        CustomPage<PackageDto> result = service.filterPackages("on-time", null, null, 0, 10);

        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getTrackingNumber()).isEqualTo("123456");
        verify(repository, times(1)).findByStatusAndActualDeliveryDateOnOrBeforeEstimatedDeliveryDate(any(), any());
    }

    @Test
    void testFilterPackages_byPostcode_happy_path() {
        when(repository.findByRecipientPostcode(anyString(), any())).thenReturn(testPage);
        when(mapper.packageEntityToDto(testPackage)).thenReturn(testPackageDto);

        CustomPage<PackageDto> result = service.filterPackages(null, "123456", null, 0, 10);

        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getTrackingNumber()).isEqualTo("123456");
        verify(repository, times(1)).findByRecipientPostcode(anyString(), any());
    }

    @Test
    void testFilterPackages_byDeliveryDate_happy_path() {
        LocalDate deliveryDate = LocalDate.now();
        when(repository.findDeliveredByActualDeliveryDate(any(), any())).thenReturn(testPage);
        when(mapper.packageEntityToDto(testPackage)).thenReturn(testPackageDto);

        CustomPage<PackageDto> result = service.filterPackages(null, null, deliveryDate.toString(), 0, 10);

        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getTrackingNumber()).isEqualTo("123456");
        verify(repository, times(1)).findDeliveredByActualDeliveryDate(any(), any());
    }

    @Test
    void testFilterPackages_findMostDelayedPostcode_happy_path() {
        PostcodeByCountDto testPostcodeByCountDto = new PostcodeByCountDto();
        testPostcodeByCountDto.setRecipientPostcode("12345");
        testPostcodeByCountDto.setDelayCount(5);

        when(repository.findPostcodeWithMostDelayedPackages(any())).thenReturn(new PageImpl<>(List.of(testPostcodeByCountDto)));

        CustomPage<PostcodeByCountDto> result = service.filterPostcodeByMostDelayedPackages(0, 10);

        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getRecipientPostcode()).isEqualTo(testPostcodeByCountDto.getRecipientPostcode());
        verify(repository, times(1)).findPostcodeWithMostDelayedPackages(any());
    }

    @Test
    void testFilterPackages_invalidStatus_rainy_path() {
        assertThrows(PackageValidationException.class, () -> service.filterPackages("invalid_status", null, null, 0, 10));
        verify(repository, never()).findByStatusAndActualDeliveryDateAfterEstimatedDeliveryDate(any(), any());
    }

    @Test
    void testFilterPackages_invalidDeliveryDate_rainy_path() {
        assertThrows(PackageValidationException.class, () -> service.filterPackages(null, null, "invalid_date", 0, 10));
        verify(repository, never()).findDeliveredByActualDeliveryDate(any(), any());
    }

    @Test
    void testFilterPackages_conflictingParameters_rainy_path() {
        assertThrows(PackageValidationException.class, () -> service.filterPackages("delayed", "123456", null, 0, 10));
        verify(repository, never()).findDeliveredByActualDeliveryDate(any(), any());
        verify(repository, never()).findByStatusAndActualDeliveryDateAfterEstimatedDeliveryDate(any(), any());
        verify(repository, never()).findByStatusAndActualDeliveryDateOnOrBeforeEstimatedDeliveryDate(any(), any());
        verify(repository, never()).findDeliveredByRecipientPostcode(any(), any());
        verify(repository, never()).findPostcodeWithMostDelayedPackages(any());
    }

}