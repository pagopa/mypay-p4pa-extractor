package it.gov.pagopa.mypay2pu.extractor.service.export.debtpositionpaid;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.DebtPositionPaidDao;
import it.gov.pagopa.mypay2pu.extractor.dto.export.PuDebtPositionPaidDTO;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.mapper.debtpositionpaid.DebtPositionPaidMapper;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPositionPaid;
import it.gov.pagopa.mypay2pu.extractor.service.FileArchiverService;
import it.gov.pagopa.mypay2pu.extractor.service.export.CsvPartitionWriterService;
import it.gov.pagopa.mypay2pu.extractor.service.export.SplitByIpaCodeBaseExportProcessingService;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import it.gov.pagopa.mypay2pu.extractor.validation.LogicalKeyPair;
import it.gov.pagopa.mypay2pu.extractor.validation.PairedLogicalKeyValidator;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class DebtPositionPaidExportProcessingService
  extends SplitByIpaCodeBaseExportProcessingService<DebtPositionPaid, PuDebtPositionPaidDTO> {

  private final DebtPositionPaidDao debtPositionPaidDao;
  private final DebtPositionPaidMapper debtPositionPaidMapper;

  public DebtPositionPaidExportProcessingService(
    DebtPositionPaidDao debtPositionPaidDao,
    DebtPositionPaidMapper debtPositionPaidMapper,
    CsvService csvService,
    CsvPartitionWriterService csvPartitionWriterService,
    FileArchiverService fileArchiverService,
    Validator validator,
    ExtractorExportProperties exportProperties
  ) {
    super(csvService, csvPartitionWriterService, fileArchiverService, validator, exportProperties);
    this.debtPositionPaidDao = debtPositionPaidDao;
    this.debtPositionPaidMapper = debtPositionPaidMapper;
  }

  @Override
  protected MigrationFileType getMigrationFileType() {
    return MigrationFileType.DEBT_POSITIONS_PAID;
  }

  @Override
  protected Class<PuDebtPositionPaidDTO> getDtoClass() {
    return PuDebtPositionPaidDTO.class;
  }

  @Override
  protected String getZipVersion() {
    return PuDebtPositionPaidDTO.VERSION;
  }

  @Override
  protected PuDebtPositionPaidDTO toExportableEntity(DebtPositionPaid model) {
    return debtPositionPaidMapper.map(model);
  }

  @Override
  protected List<DebtPositionPaid> retrieveData(String ipaCode, ExtractionRequest request, int pageSize, int offset) {
    ExtractionFilters filters = request.getFilters();
    LogicalKeyPair logicalKeyPair = PairedLogicalKeyValidator.parseLogicalKey(
      filters != null ? filters.getLogicalKey() : null
    );
    OffsetDateTime createdFrom = filters != null ? filters.getDateFrom() : null;
    OffsetDateTime createdTo = filters != null ? filters.getDateTo() : null;

    return debtPositionPaidDao.findByFilters(
      ipaCode,
      logicalKeyPair.left(),
      logicalKeyPair.right(),
      createdFrom,
      createdTo,
      pageSize,
      offset
    );
  }
}
