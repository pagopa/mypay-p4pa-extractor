package it.gov.pagopa.mypay2pu.extractor.service.export.assessmentsregistry;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.AssessmentsRegistryDao;
import it.gov.pagopa.mypay2pu.extractor.dto.export.PuAssessmentsRegistryDTO;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.mapper.assessmentsregistry.AssessmentsRegistryMapper;
import it.gov.pagopa.mypay2pu.extractor.model.mpv4.AssessmentsRegistry;
import it.gov.pagopa.mypay2pu.extractor.service.FileArchiverService;
import it.gov.pagopa.mypay2pu.extractor.service.export.CsvPartitionWriterService;
import it.gov.pagopa.mypay2pu.extractor.service.export.SplitByIpaCodeBaseExportProcessingService;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import it.gov.pagopa.mypay2pu.extractor.validation.ValueLogicalKeyValidator;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssessmentsRegistryExportProcessingService extends SplitByIpaCodeBaseExportProcessingService<AssessmentsRegistry, PuAssessmentsRegistryDTO> {

  private final AssessmentsRegistryDao assessmentsRegistryDao;
  private final AssessmentsRegistryMapper assessmentsRegistryMapper;

  protected AssessmentsRegistryExportProcessingService(AssessmentsRegistryDao assessmentsRegistryDao,
                                                       AssessmentsRegistryMapper assessmentsRegistryMapper,
                                                       CsvService csvService,
                                                       CsvPartitionWriterService csvPartitionWriterService,
                                                       FileArchiverService fileArchiverService,
                                                       Validator validator,
                                                       ExtractorExportProperties exportProperties) {
    super(csvService, csvPartitionWriterService, fileArchiverService, validator, exportProperties);
    this.assessmentsRegistryDao = assessmentsRegistryDao;
    this.assessmentsRegistryMapper = assessmentsRegistryMapper;
  }

  @Override
  protected MigrationFileType getMigrationFileType() {
    return MigrationFileType.ASSESSMENTS_REGISTRY;
  }

  @Override
  protected Class<PuAssessmentsRegistryDTO> getDtoClass() {
    return PuAssessmentsRegistryDTO.class;
  }

  @Override
  protected String getZipVersion() {
    return PuAssessmentsRegistryDTO.VERSION;
  }

  @Override
  protected PuAssessmentsRegistryDTO toExportableEntity(AssessmentsRegistry model) {
    return assessmentsRegistryMapper.map(model);
  }

  @Override
  protected List<AssessmentsRegistry> retrieveData(String ipaCode, ExtractionRequest request, int pageSize, int offset) {
    return assessmentsRegistryDao.findByFilters(
      ipaCode,
      request.getLastExtractionDate(),
      ValueLogicalKeyValidator.parseLogicalKey(request.getFilters().getLogicalKey()),
      request.getFilters().getDateFrom(),
      request.getFilters().getDateTo(),
      pageSize,
      offset
    );
  }
}
