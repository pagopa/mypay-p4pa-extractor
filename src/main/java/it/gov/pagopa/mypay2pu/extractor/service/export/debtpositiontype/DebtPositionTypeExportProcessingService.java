package it.gov.pagopa.mypay2pu.extractor.service.export.debtpositiontype;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.DebtPositionTypeDao;
import it.gov.pagopa.mypay2pu.extractor.dto.export.PuDebtPositionTypeDTO;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.mapper.debtpositiontype.DebtPositionTypeMapper;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPositionType;
import it.gov.pagopa.mypay2pu.extractor.service.FileArchiverService;
import it.gov.pagopa.mypay2pu.extractor.service.export.BaseExportProcessingService;
import it.gov.pagopa.mypay2pu.extractor.service.export.CsvPartitionWriterService;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DebtPositionTypeExportProcessingService extends BaseExportProcessingService<DebtPositionType, PuDebtPositionTypeDTO> {

  private final DebtPositionTypeDao debtPositionTypeDao;
  private final DebtPositionTypeMapper debtPositionTypeMapper;

  public DebtPositionTypeExportProcessingService(DebtPositionTypeDao debtPositionTypeDao,
                                                 DebtPositionTypeMapper debtPositionTypeMapper,
                                                 CsvService csvService,
                                                 CsvPartitionWriterService csvPartitionWriterService,
                                                 FileArchiverService fileArchiverService,
                                                 Validator validator,
                                                 ExtractorExportProperties exportProperties) {
    super(csvService, csvPartitionWriterService, fileArchiverService, validator, exportProperties);
    this.debtPositionTypeDao = debtPositionTypeDao;
    this.debtPositionTypeMapper = debtPositionTypeMapper;
  }

  @Override
  protected MigrationFileType getMigrationFileType() {
    return MigrationFileType.DEBT_POSITIONS_TYPE;
  }

  @Override
  protected Class<PuDebtPositionTypeDTO> getDtoClass() {
    return PuDebtPositionTypeDTO.class;
  }

  @Override
  protected String getZipVersion() {
    return PuDebtPositionTypeDTO.VERSION;
  }

  @Override
  protected PuDebtPositionTypeDTO toExportableEntity(DebtPositionType debtPositionType) {
    return debtPositionTypeMapper.map(debtPositionType);
  }

  @Override
  protected boolean isAggregatedExport() {
    return true;
  }

  @Override
  protected List<DebtPositionType> retrieveData(ExtractionRequest request, int pageSize, int offset) {
    return debtPositionTypeDao.findByFilters(request.getFilters(), pageSize, offset);
  }
}
