package it.gov.pagopa.mypay2pu.extractor.service.export.debtpositiontypeorg;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.DebtPositionTypeOrgDao;
import it.gov.pagopa.mypay2pu.extractor.dto.export.PuDebtPositionTypeOrgDTO;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPositionTypeOrg;
import it.gov.pagopa.mypay2pu.extractor.service.FileArchiverService;
import it.gov.pagopa.mypay2pu.extractor.service.export.BaseExportProcessingService;
import it.gov.pagopa.mypay2pu.extractor.service.export.CsvPartitionWriterService;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DebtPositionTypeOrgExportProcessingService extends BaseExportProcessingService<DebtPositionTypeOrg, PuDebtPositionTypeOrgDTO> {

  private final DebtPositionTypeOrgDao debtPositionTypeOrgDao;
  //TODO add mapper

  protected DebtPositionTypeOrgExportProcessingService(DebtPositionTypeOrgDao debtPositionTypeOrgDao,
                                                       CsvService csvService,
                                                       CsvPartitionWriterService csvPartitionWriterService,
                                                       FileArchiverService fileArchiverService,
                                                       Validator validator,
                                                       ExtractorExportProperties exportProperties) {
    super(csvService, csvPartitionWriterService, fileArchiverService, validator, exportProperties);
    this.debtPositionTypeOrgDao = debtPositionTypeOrgDao;
  }

  @Override
  protected MigrationFileType getMigrationFileType() {
    return MigrationFileType.DEBT_POSITIONS_TYPE_ORG;
  }

  @Override
  protected Class<PuDebtPositionTypeOrgDTO> getDtoClass() {
    return PuDebtPositionTypeOrgDTO.class;
  }

  @Override
  protected String getZipVersion() { return PuDebtPositionTypeOrgDTO.VERSION; }

  @Override
  protected PuDebtPositionTypeOrgDTO toExportableEntity(DebtPositionTypeOrg model) {
    return null; //TODO add mapper P4ADEV-4887
  }

  @Override
  protected List<DebtPositionTypeOrg> retrieveData(ExtractionRequest request, int pageSize, int offset) {
    return debtPositionTypeOrgDao.findByFilters(
      request.getIpaCode(),
      request.getFilters() != null ? request.getFilters().getDebtPositionTypeOrgCodes() : null,
      pageSize,
      offset
    );
  }
}
