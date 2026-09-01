package it.gov.pagopa.mypay2pu.extractor.service.export.debtpositiontypeorg;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.DebtPositionTypeOrgDao;
import it.gov.pagopa.mypay2pu.extractor.dto.export.PuDebtPositionTypeOrgDTO;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.mapper.debtpositiontypeorg.DebtPositionTypeOrgMapper;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPositionTypeOrg;
import it.gov.pagopa.mypay2pu.extractor.service.FileArchiverService;
import it.gov.pagopa.mypay2pu.extractor.service.export.SplitByIpaCodeBaseExportProcessingService;
import it.gov.pagopa.mypay2pu.extractor.service.export.CsvPartitionWriterService;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import it.gov.pagopa.mypay2pu.extractor.validation.ValueLogicalKeyValidator;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DebtPositionTypeOrgExportProcessingService extends SplitByIpaCodeBaseExportProcessingService<DebtPositionTypeOrg, PuDebtPositionTypeOrgDTO> {

  private final DebtPositionTypeOrgDao debtPositionTypeOrgDao;
  private final DebtPositionTypeOrgMapper debtPositionTypeOrgMapper;

  protected DebtPositionTypeOrgExportProcessingService(DebtPositionTypeOrgDao debtPositionTypeOrgDao,
                                                       DebtPositionTypeOrgMapper debtPositionTypeOrgMapper,
                                                       CsvService csvService,
                                                       CsvPartitionWriterService csvPartitionWriterService,
                                                       FileArchiverService fileArchiverService,
                                                       Validator validator,
                                                       ExtractorExportProperties exportProperties) {
    super(csvService, csvPartitionWriterService, fileArchiverService, validator, exportProperties);
    this.debtPositionTypeOrgDao = debtPositionTypeOrgDao;
    this.debtPositionTypeOrgMapper = debtPositionTypeOrgMapper;
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
    return debtPositionTypeOrgMapper.map(model);
  }

  @Override
  protected List<DebtPositionTypeOrg> retrieveData(String ipaCode, ExtractionRequest request, int pageSize, int offset) {
    return debtPositionTypeOrgDao.findByFilters(
      ipaCode,
      ValueLogicalKeyValidator.parseLogicalKey(request.getFilters().getLogicalKey()),
      pageSize,
      offset
    );
  }
}
