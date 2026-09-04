package it.gov.pagopa.mypay2pu.extractor.service.export.treasurycsvcomplete;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.TreasuryCsvCompleteDao;
import it.gov.pagopa.mypay2pu.extractor.dto.export.PuTreasuryCsvCompleteDTO;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.mapper.treasurycsvcomplete.TreasuryCsvCompleteMapper;
import it.gov.pagopa.mypay2pu.extractor.model.mpv4.TreasuryCsvComplete;
import it.gov.pagopa.mypay2pu.extractor.service.FileArchiverService;
import it.gov.pagopa.mypay2pu.extractor.service.export.CsvPartitionWriterService;
import it.gov.pagopa.mypay2pu.extractor.service.export.SplitByIpaCodeBaseExportProcessingService;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TreasuryCsvCompleteExportProcessingService
  extends SplitByIpaCodeBaseExportProcessingService<TreasuryCsvComplete, PuTreasuryCsvCompleteDTO> {

  private final TreasuryCsvCompleteDao treasuryCsvCompleteDao;
  private final TreasuryCsvCompleteMapper treasuryCsvCompleteMapper;

  public TreasuryCsvCompleteExportProcessingService(
    TreasuryCsvCompleteDao treasuryCsvCompleteDao,
    TreasuryCsvCompleteMapper treasuryCsvCompleteMapper,
    CsvService csvService,
    CsvPartitionWriterService csvPartitionWriterService,
    FileArchiverService fileArchiverService,
    Validator validator,
    ExtractorExportProperties exportProperties
  ) {
    super(csvService, csvPartitionWriterService, fileArchiverService, validator, exportProperties);
    this.treasuryCsvCompleteDao = treasuryCsvCompleteDao;
    this.treasuryCsvCompleteMapper = treasuryCsvCompleteMapper;
  }

  @Override
  protected MigrationFileType getMigrationFileType() {
    return MigrationFileType.TREASURY_CSV_COMPLETE;
  }

  @Override
  protected Class<PuTreasuryCsvCompleteDTO> getDtoClass() {
    return PuTreasuryCsvCompleteDTO.class;
  }

  @Override
  protected String getZipVersion() {
    return PuTreasuryCsvCompleteDTO.VERSION;
  }

  @Override
  protected PuTreasuryCsvCompleteDTO toExportableEntity(TreasuryCsvComplete model) {
    return treasuryCsvCompleteMapper.map(model);
  }

  @Override
  protected List<TreasuryCsvComplete> retrieveData(String ipaCode, ExtractionRequest request, int pageSize, int offset) {
    ExtractionFilters filters = request.getFilters();
    return treasuryCsvCompleteDao.findByFilters(
      ipaCode,
      new TreasuryCsvCompleteDao.TreasuryCsvCompleteFilters(
        filters != null ? filters.getLogicalKey() : null,
        filters != null ? filters.getDateFrom() : null,
        filters != null ? filters.getDateTo() : null
      ),
      pageSize,
      offset
    );
  }
}
