package it.gov.pagopa.mypay2pu.extractor.service.export.paymentnotification;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.PaymentNotificationDao;
import it.gov.pagopa.mypay2pu.extractor.dto.export.PuPaymentNotificationDTO;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.mapper.paymentnotification.PaymentNotificationMapper;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.PaymentNotification;
import it.gov.pagopa.mypay2pu.extractor.service.FileArchiverService;
import it.gov.pagopa.mypay2pu.extractor.service.export.CsvPartitionWriterService;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentNotificationExportProcessingServiceTest {

  @Mock
  private PaymentNotificationDao paymentNotificationDaoMock;
  @Mock
  private PaymentNotificationMapper paymentNotificationMapperMock;
  @Mock
  private CsvService csvServiceMock;
  @Mock
  private CsvPartitionWriterService csvPartitionWriterServiceMock;
  @Mock
  private FileArchiverService fileArchiverServiceMock;
  @Mock
  private Validator validatorMock;
  @Mock
  private ExtractorExportProperties exportPropertiesMock;

  @AfterEach
  void verifyMocks() {
    verifyNoMoreInteractions(
      paymentNotificationDaoMock,
      paymentNotificationMapperMock,
      csvServiceMock,
      csvPartitionWriterServiceMock,
      fileArchiverServiceMock,
      validatorMock,
      exportPropertiesMock
    );
  }

  @Test
  void retrieveDataShouldDelegateMultiOrganizationFiltersToDao() {
    PaymentNotificationExportProcessingService service = service();
    String ipaCode = "IPA1";
    String iud = "IUD-1";
    String iuv = "IUV-1";
    LocalDate modifiedFrom = LocalDate.of(2026, Month.JANUARY, 10);
    LocalDate modifiedTo = LocalDate.of(2026, Month.JANUARY, 11);
    ExtractionFilters filters = new ExtractionFilters()
      .iud(iud)
      .iuv(iuv)
      .modifiedFrom(modifiedFrom)
      .modifiedTo(modifiedTo);
    ExtractionRequest request = new ExtractionRequest(List.of(ipaCode), MigrationFileType.PAYMENT_NOTIFICATION, null, filters);
    List<PaymentNotification> expected = List.of();
    when(paymentNotificationDaoMock.findByFilters(
      ipaCode,
      iud,
      iuv,
      modifiedFrom.atStartOfDay(),
      modifiedTo.plusDays(1).atStartOfDay(),
      50,
      100
    ))
      .thenReturn(expected);

    List<PaymentNotification> result = service.retrieveData(ipaCode, request, 50, 100);

    assertEquals(expected, result);
    verify(paymentNotificationDaoMock).findByFilters(
      ipaCode,
      iud,
      iuv,
      modifiedFrom.atStartOfDay(),
      modifiedTo.plusDays(1).atStartOfDay(),
      50,
      100
    );
  }

  @Test
  void serviceShouldExposePaymentNotificationMetadata() {
    PaymentNotificationExportProcessingService service = service();

    assertEquals(MigrationFileType.PAYMENT_NOTIFICATION, service.getMigrationFileType());
    assertEquals(PuPaymentNotificationDTO.class, service.getDtoClass());
    assertEquals(PuPaymentNotificationDTO.VERSION, service.getZipVersion());
  }

  @Test
  void retrieveDataShouldDelegateNullFiltersToDao() {
    PaymentNotificationExportProcessingService service = service();
    String ipaCode = "IPA1";
    List<PaymentNotification> expected = List.of();
    when(paymentNotificationDaoMock.findByFilters(ipaCode, null, null, null, null, 50, 100))
      .thenReturn(expected);

    List<PaymentNotification> result = service.retrieveData(
      ipaCode,
      new ExtractionRequest(List.of(ipaCode), MigrationFileType.PAYMENT_NOTIFICATION, null, null),
      50,
      100
    );

    assertEquals(expected, result);
    verify(paymentNotificationDaoMock).findByFilters(ipaCode, null, null, null, null, 50, 100);
  }

  private PaymentNotificationExportProcessingService service() {
    return new PaymentNotificationExportProcessingService(
      paymentNotificationDaoMock,
      paymentNotificationMapperMock,
      csvServiceMock,
      csvPartitionWriterServiceMock,
      fileArchiverServiceMock,
      validatorMock,
      exportPropertiesMock
    );
  }
}
