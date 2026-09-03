package it.gov.pagopa.mypay2pu.extractor.service.export.paymentnotification;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.PaymentNotificationDao;
import it.gov.pagopa.mypay2pu.extractor.dto.export.PuPaymentNotificationDTO;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.mapper.paymentnotification.PaymentNotificationMapper;
import it.gov.pagopa.mypay2pu.extractor.model.mpv4.PaymentNotification;
import it.gov.pagopa.mypay2pu.extractor.service.FileArchiverService;
import it.gov.pagopa.mypay2pu.extractor.service.export.CsvPartitionWriterService;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
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
  void retrieveDataShouldDelegateDateFiltersToDao() {
    PaymentNotificationExportProcessingService service = service();
    String ipaCode = "IPA1";
    OffsetDateTime dateFrom = OffsetDateTime.parse("2026-01-10T10:30:00Z");
    OffsetDateTime dateTo = OffsetDateTime.parse("2026-01-11T10:30:00Z");
    ExtractionFilters filters = new ExtractionFilters()
      .dateFrom(dateFrom)
      .dateTo(dateTo)
      .logicalKey("IUD-1,IUD-2|IUV-1,IUV-2");
    ExtractionRequest request = new ExtractionRequest(List.of(ipaCode), MigrationFileType.PAYMENT_NOTIFICATION, null, filters);
    List<PaymentNotification> expected = List.of();
    when(paymentNotificationDaoMock.findByFilters(
      ipaCode,
      List.of("IUD-1", "IUD-2"),
      List.of("IUV-1", "IUV-2"),
      dateFrom.toLocalDateTime(),
      dateTo.toLocalDateTime(),
      50,
      100
    ))
      .thenReturn(expected);

    List<PaymentNotification> result = service.retrieveData(ipaCode, request, 50, 100);

    assertEquals(expected, result);
    verify(paymentNotificationDaoMock).findByFilters(
      ipaCode,
      List.of("IUD-1", "IUD-2"),
      List.of("IUV-1", "IUV-2"),
      dateFrom.toLocalDateTime(),
      dateTo.toLocalDateTime(),
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
    when(paymentNotificationDaoMock.findByFilters(ipaCode, List.of(), List.of(), null, null, 50, 100))
      .thenReturn(expected);

    List<PaymentNotification> result = service.retrieveData(
      ipaCode,
      new ExtractionRequest(List.of(ipaCode), MigrationFileType.PAYMENT_NOTIFICATION, null, null),
      50,
      100
    );

    assertEquals(expected, result);
    verify(paymentNotificationDaoMock).findByFilters(ipaCode, List.of(), List.of(), null, null, 50, 100);
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
