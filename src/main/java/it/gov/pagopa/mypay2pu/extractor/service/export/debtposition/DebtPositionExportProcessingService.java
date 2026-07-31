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
import it.gov.pagopa.mypay2pu.extractor.service.export.BaseExportProcessingService;
import it.gov.pagopa.mypay2pu.extractor.service.export.CsvPartitionWriterService;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import it.gov.pagopa.pu.debtposition.dto.generated.Action;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class DebtPositionExportProcessingService extends BaseExportProcessingService<DebtPositionExportProcessingService.DebtPositionWithAction, PuDebtPositionDTO> {

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
  protected List<DebtPositionWithAction> retrieveData(ExtractionRequest request, int pageSize, int offset) {
    ExtractionFilters filters = request.getFilters();
    List<DebtPosition> debtPositions = debtPositionDao.findDebtPositions(
      request.getIpaCode(),
      null,
      null,
      filters,
      pageSize,
      offset
    );

    LocalDate lastExtractionDate = request.getLastExtractionDate();
    if (lastExtractionDate == null) {
      return debtPositions.stream()
        .map(debtPosition -> new DebtPositionWithAction(debtPosition, FIRST_EXTRACTION_ACTION))
        .toList();
    }

    LocalDateTime lastExtractionDateTime = lastExtractionDate.atStartOfDay();
    List<DebtPosition> cancelledDebtPositions = debtPositionDao.findCancelledDebtPositions(
      request.getIpaCode(),
      null,
      null,
      filters,
      pageSize,
      offset
    );

    return Stream.concat(
      debtPositions.stream().map(debtPosition -> new DebtPositionWithAction(
        debtPosition,
        resolveOpenDebtPositionAction(debtPosition, lastExtractionDateTime)
      )),
      cancelledDebtPositions.stream().map(debtPosition -> new DebtPositionWithAction(debtPosition, Action.A))
    ).toList();
  }

  private Action resolveOpenDebtPositionAction(DebtPosition debtPosition, LocalDateTime lastExtractionDateTime) {
    LocalDateTime dtCreazione = debtPosition.dtCreazione();
    if (dtCreazione != null && dtCreazione.isAfter(lastExtractionDateTime)) {
      return Action.I;
    }

    LocalDateTime dtUltimaModifica = debtPosition.dtUltimaModifica();
    if (dtCreazione != null
      && !dtCreazione.isAfter(lastExtractionDateTime)
      && dtUltimaModifica != null
      && dtUltimaModifica.isAfter(lastExtractionDateTime)) {
      return Action.M;
    }

    log.warn(
      "Cannot resolve action for debt position iupd={} iud={}. Falling back to Action.I",
      debtPosition.iupd(),
      debtPosition.iud()
    );
    return Action.I;
  }

  record DebtPositionWithAction(DebtPosition debtPosition, Action action) implements ExportModel {
  }
}
