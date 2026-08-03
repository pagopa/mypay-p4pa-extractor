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
import it.gov.pagopa.mypay2pu.extractor.service.export.SplitByIpaCodeBaseExportProcessingService;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import it.gov.pagopa.mypay2pu.extractor.utils.DateTimeUtils;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentNotificationExportProcessingService
  extends SplitByIpaCodeBaseExportProcessingService<PaymentNotification, PuPaymentNotificationDTO> {

  private final PaymentNotificationDao paymentNotificationDao;
  private final PaymentNotificationMapper paymentNotificationMapper;

  protected PaymentNotificationExportProcessingService(
    PaymentNotificationDao paymentNotificationDao,
    PaymentNotificationMapper paymentNotificationMapper,
    CsvService csvService,
    CsvPartitionWriterService csvPartitionWriterService,
    FileArchiverService fileArchiverService,
    Validator validator,
    ExtractorExportProperties exportProperties
  ) {
    super(csvService, csvPartitionWriterService, fileArchiverService, validator, exportProperties);
    this.paymentNotificationDao = paymentNotificationDao;
    this.paymentNotificationMapper = paymentNotificationMapper;
  }

  @Override
  protected MigrationFileType getMigrationFileType() {
    return MigrationFileType.PAYMENT_NOTIFICATION;
  }

  @Override
  protected Class<PuPaymentNotificationDTO> getDtoClass() {
    return PuPaymentNotificationDTO.class;
  }

  @Override
  protected String getZipVersion() {
    return PuPaymentNotificationDTO.VERSION;
  }

  @Override
  protected PuPaymentNotificationDTO toExportableEntity(PaymentNotification model) {
    return paymentNotificationMapper.map(model);
  }

  @Override
  protected List<PaymentNotification> retrieveData(String ipaCode, ExtractionRequest request, int pageSize, int offset) {
    String iud = Optional.ofNullable(request.getFilters())
      .map(ExtractionFilters::getIud)
      .orElse(null);
    String iuv = Optional.ofNullable(request.getFilters())
      .map(ExtractionFilters::getIuv)
      .orElse(null);
    LocalDate createdFrom = Optional.ofNullable(request.getFilters())
      .map(ExtractionFilters::getCreatedFrom)
      .orElse(null);
    LocalDate createdTo = Optional.ofNullable(request.getFilters())
      .map(ExtractionFilters::getCreatedTo)
      .orElse(null);
    return paymentNotificationDao.findByFilters(
      ipaCode,
      iud,
      iuv,
      DateTimeUtils.toStartOfDay(createdFrom),
      DateTimeUtils.toStartOfNextDay(createdTo),
      pageSize,
      offset
    );
  }
}
