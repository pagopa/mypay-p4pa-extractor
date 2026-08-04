package it.gov.pagopa.mypay2pu.extractor.service.export.debtpositionstypeorgoperators;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.DebtPositionsTypeOrgOperatorsDao;
import it.gov.pagopa.mypay2pu.extractor.dto.export.PuDebtPositionsTypeOrgOperatorsDTO;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.mapper.debtpositionstypeorgoperators.DebtPositionsTypeOrgOperatorsMapper;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPositionsTypeOrgOperators;
import it.gov.pagopa.mypay2pu.extractor.service.FileArchiverService;
import it.gov.pagopa.mypay2pu.extractor.service.export.BaseExportProcessingService;
import it.gov.pagopa.mypay2pu.extractor.service.export.CsvPartitionWriterService;
import it.gov.pagopa.mypay2pu.extractor.service.export.SplitByIpaCodeBaseExportProcessingService;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DebtPositionsTypeOrgOperatorsExportProcessingService extends SplitByIpaCodeBaseExportProcessingService<DebtPositionsTypeOrgOperators, PuDebtPositionsTypeOrgOperatorsDTO> {

  private final DebtPositionsTypeOrgOperatorsDao debtPositionsTypeOrgOperatorsDao;
  private final DebtPositionsTypeOrgOperatorsMapper debtPositionsTypeOrgOperatorsMapper;

  public DebtPositionsTypeOrgOperatorsExportProcessingService(DebtPositionsTypeOrgOperatorsDao debtPositionsTypeOrgOperatorsDao,
                                                              DebtPositionsTypeOrgOperatorsMapper debtPositionsTypeOrgOperatorsMapper,
                                                              CsvService csvService,
                                                              CsvPartitionWriterService csvPartitionWriterService,
                                                              FileArchiverService fileArchiverService,
                                                              Validator validator,
                                                              ExtractorExportProperties exportProperties) {
    super(csvService, csvPartitionWriterService, fileArchiverService, validator, exportProperties);
    this.debtPositionsTypeOrgOperatorsDao = debtPositionsTypeOrgOperatorsDao;
    this.debtPositionsTypeOrgOperatorsMapper = debtPositionsTypeOrgOperatorsMapper;
  }

  @Override
  protected MigrationFileType getMigrationFileType() {
    return MigrationFileType.DEBT_POSITIONS_TYPE_ORG_OPERATORS;
  }

  @Override
  protected Class<PuDebtPositionsTypeOrgOperatorsDTO> getDtoClass() {
    return PuDebtPositionsTypeOrgOperatorsDTO.class;
  }

  @Override
  protected String getZipVersion() {
    return PuDebtPositionsTypeOrgOperatorsDTO.VERSION;
  }

  @Override
  protected PuDebtPositionsTypeOrgOperatorsDTO toExportableEntity(DebtPositionsTypeOrgOperators debtPositionsTypeOrgOperators) {
    return debtPositionsTypeOrgOperatorsMapper.map(debtPositionsTypeOrgOperators);
  }

  @Override
  protected List<DebtPositionsTypeOrgOperators> retrieveData(String ipaCode, ExtractionRequest request, int pageSize, int offset) {
    return debtPositionsTypeOrgOperatorsDao.findByFilters(
      ipaCode,
      null,
      null,
      pageSize,
      offset
    );
  }
}
