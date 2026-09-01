package it.gov.pagopa.mypay2pu.extractor.service.export.debtposition;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.DebtPositionDao;
import it.gov.pagopa.mypay2pu.extractor.dto.export.PuDebtPositionDTO;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.mapper.debtposition.DebtPositionMapper;
import it.gov.pagopa.mypay2pu.extractor.model.ExportModel;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPosition;
import it.gov.pagopa.mypay2pu.extractor.service.FileArchiverService;
import it.gov.pagopa.mypay2pu.extractor.service.export.CsvPartitionWriterService;
import it.gov.pagopa.mypay2pu.extractor.service.export.SplitByIpaCodeBaseExportProcessingService;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import it.gov.pagopa.mypay2pu.extractor.validation.CsvLogicalKeyValidator;
import it.gov.pagopa.pu.debtposition.dto.generated.Action;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class DebtPositionExportProcessingService extends SplitByIpaCodeBaseExportProcessingService<DebtPositionExportProcessingService.DebtPositionWithAction, PuDebtPositionDTO> {

  private static final Action FIRST_EXTRACTION_ACTION = Action.I;

  private final DebtPositionDao debtPositionDao;
  private final DebtPositionMapper debtPositionMapper;

  public DebtPositionExportProcessingService(DebtPositionDao debtPositionDao,
                                             DebtPositionMapper debtPositionMapper,
                                             CsvService csvService,
                                             CsvPartitionWriterService csvPartitionWriterService,
                                             FileArchiverService fileArchiverService,
                                             Validator validator,
                                             ExtractorExportProperties exportProperties) {
    super(csvService, csvPartitionWriterService, fileArchiverService, validator, exportProperties);
    this.debtPositionDao = debtPositionDao;
    this.debtPositionMapper = debtPositionMapper;
  }

  @Override
  protected MigrationFileType getMigrationFileType() {
    return MigrationFileType.DEBT_POSITIONS;
  }

  @Override
  protected Class<PuDebtPositionDTO> getDtoClass() {
    return PuDebtPositionDTO.class;
  }

  @Override
  protected String getZipVersion() {
    return PuDebtPositionDTO.VERSION;
  }

  @Override
  protected PuDebtPositionDTO toExportableEntity(DebtPositionWithAction model) {
    return debtPositionMapper.map(model.debtPosition(), model.action());
  }

  @Override
  protected List<DebtPositionWithAction> retrieveData(String ipaCode, ExtractionRequest request, int pageSize, int offset) {
    ExtractionFilters filters = request.getFilters();
    List<String> iuvs = CsvLogicalKeyValidator.parseLogicalKey(filters != null ? filters.getLogicalKey() : null);
    OffsetDateTime dateFrom = filters != null ? filters.getDateFrom() : null;
    OffsetDateTime dateTo = filters != null ? filters.getDateTo() : null;
    List<DebtPosition> debtPositions = debtPositionDao.findDebtPositions(
      ipaCode,
      iuvs,
      dateFrom,
      dateTo,
      pageSize,
      offset
    );

    OffsetDateTime lastExtractionDate = request.getLastExtractionDate();
    if (lastExtractionDate == null) {
      return debtPositions.stream()
        .map(debtPosition -> new DebtPositionWithAction(debtPosition, FIRST_EXTRACTION_ACTION))
        .toList();
    }

    LocalDateTime lastExtractionDateTime = lastExtractionDate.toLocalDateTime();
    List<DebtPosition> cancelledDebtPositions = debtPositionDao.findCancelledDebtPositions(
      ipaCode,
      iuvs,
      dateFrom,
      dateTo,
      pageSize,
      offset
    );

    return Stream.concat(
      debtPositions.stream().map(debtPosition -> new DebtPositionWithAction(
        debtPosition,
        resolveOpenDebtPositionAction(debtPosition, ipaCode, lastExtractionDateTime)
      )),
      cancelledDebtPositions.stream().map(debtPosition -> new DebtPositionWithAction(debtPosition, Action.A))
    ).toList();
  }

  private Action resolveOpenDebtPositionAction(DebtPosition debtPosition, String ipaCode, LocalDateTime lastExtractionDateTime) {
    LocalDateTime lastChangeDateTime = ObjectUtils.firstNonNull(
      debtPosition.dtUltimaModifica(),
      debtPosition.dtCreazione()
    );
    if (lastChangeDateTime != null) {
      return lastChangeDateTime.isAfter(lastExtractionDateTime) ? Action.M : Action.I;
    }

    log.warn(
      "Cannot resolve action for debt position iupd={} iud={} of organization={}. Falling back to Action.I",
      debtPosition.iupd(),
      debtPosition.iud(),
      ipaCode
    );
    return Action.I;
  }

  record DebtPositionWithAction(DebtPosition debtPosition, Action action) implements ExportModel {
  }
}
