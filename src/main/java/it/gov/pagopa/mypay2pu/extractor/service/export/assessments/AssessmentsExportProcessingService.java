package it.gov.pagopa.mypay2pu.extractor.service.export.assessments;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.AssessmentsDao;
import it.gov.pagopa.mypay2pu.extractor.dto.export.PuAssessmentsDTO;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.mapper.assessments.AssessmentsMapper;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.Assessments;
import it.gov.pagopa.mypay2pu.extractor.service.FileArchiverService;
import it.gov.pagopa.mypay2pu.extractor.service.export.CsvPartitionWriterService;
import it.gov.pagopa.mypay2pu.extractor.service.export.SplitByIpaCodeBaseExportProcessingService;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssessmentsExportProcessingService extends SplitByIpaCodeBaseExportProcessingService<Assessments, PuAssessmentsDTO> {

  private final AssessmentsDao assessmentsDao;
  private final AssessmentsMapper assessmentsMapper;

  protected AssessmentsExportProcessingService(AssessmentsDao assessmentsDao,
                                               AssessmentsMapper assessmentsMapper,
                                               CsvService csvService,
                                               CsvPartitionWriterService csvPartitionWriterService,
                                               FileArchiverService fileArchiverService,
                                               Validator validator,
                                               ExtractorExportProperties exportProperties) {
    super(csvService, csvPartitionWriterService, fileArchiverService, validator, exportProperties);
    this.assessmentsDao = assessmentsDao;
    this.assessmentsMapper = assessmentsMapper;
  }

  @Override
  protected MigrationFileType getMigrationFileType() {
    return MigrationFileType.ASSESSMENTS;
  }

  @Override
  protected Class<PuAssessmentsDTO> getDtoClass() {
    return PuAssessmentsDTO.class;
  }

  @Override
  protected String getZipVersion() {
    return PuAssessmentsDTO.VERSION;
  }

  @Override
  protected PuAssessmentsDTO toExportableEntity(Assessments model) {
    return null;
  }

  @Override
  protected List<Assessments> retrieveData(String ipaCode, ExtractionRequest request, int pageSize, int offset) {
    return List.of();
  }
}
