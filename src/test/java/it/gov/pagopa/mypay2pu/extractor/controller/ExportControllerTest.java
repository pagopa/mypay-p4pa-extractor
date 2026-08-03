package it.gov.pagopa.mypay2pu.extractor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.mypay2pu.extractor.config.json.JsonConfig;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionStatus;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionStatusResponse;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.exception.ControllerExceptionHandler;
import it.gov.pagopa.mypay2pu.extractor.service.ExportFileHandlerService;
import it.gov.pagopa.mypay2pu.extractor.service.ExportFileStatusService;
import it.gov.pagopa.mypay2pu.extractor.validation.ExtractionRequestValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.Month;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ExportControllerTest {

  @Mock
  private ExportFileHandlerService exportFileHandlerServiceMock;
  @Mock
  private ExportFileStatusService exportFileStatusServiceMock;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new JsonConfig().objectMapper();
    mockMvc = MockMvcBuilders
      .standaloneSetup(new ExportController(
        exportFileHandlerServiceMock,
        exportFileStatusServiceMock,
        new ExtractionRequestValidator()))
      .setControllerAdvice(new ControllerExceptionHandler())
      .build();
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(exportFileHandlerServiceMock, exportFileStatusServiceMock);
  }

  @Test
  void givenGeneratedExtractionIdWhenExecuteExportThenReturnAccepted() throws Exception {
    ExtractionRequest request = new ExtractionRequest(
      List.of("IPA_CODE_TEST"),
      MigrationFileType.ORGANIZATIONS,
      new ExtractionFilters()
    );
    when(exportFileHandlerServiceMock.createExtraction(request)).thenReturn("generated-extraction-id");

    mockMvc.perform(post("/extract")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isAccepted())
      .andExpect(jsonPath("$.extractionId").value("generated-extraction-id"));

  }

  @Test
  void givenValidExtractionIdWhenGetExtractionStatusThenReturnStatus() throws Exception {
    OffsetDateTime now = OffsetDateTime.parse("2026-01-01T00:00:00Z");
    String extractionId = "123e4567-e89b-42d3-a456-426614174000";
    ExtractionStatusResponse statusResponse = new ExtractionStatusResponse(
      extractionId,
      List.of("IPA_CODE_TEST"),
      MigrationFileType.ORGANIZATIONS,
      ExtractionStatus.COMPLETED,
      now,
      now,
      null,
      List.of("organizations.csv")
    );
    when(exportFileStatusServiceMock.readStatus(extractionId)).thenReturn(statusResponse);

    mockMvc.perform(get("/extract/" + extractionId))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.extractionId").value(extractionId))
      .andExpect(jsonPath("$.status").value("COMPLETED"))
      .andExpect(jsonPath("$.files[0]").value("organizations.csv"));

  }

  @Test
  void givenReversedFilterDatesWhenExecuteExportThenReturnBadRequest() throws Exception {
    ExtractionRequest request = new ExtractionRequest(
      List.of("IPA_CODE_TEST"),
      MigrationFileType.ORGANIZATIONS,
      new ExtractionFilters(
        LocalDate.of(2026, Month.JANUARY, 2),
        LocalDate.of(2026, Month.JANUARY, 1),
        List.of()
      )
    );

    mockMvc.perform(post("/extract")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.category").value("BAD_REQUEST"))
      .andExpect(jsonPath("$.code").value("INVALID_EXTRACTION_FILTERS"))
      .andExpect(jsonPath("$.message").value("[INVALID_EXTRACTION_FILTERS] filters.modifiedFrom must be before or equal to filters.modifiedTo"));
  }

  @Test
  void givenInvalidExtractionIdWhenGetExtractionStatusThenReturnBadRequest() throws Exception {
    mockMvc.perform(get("/extract/not-a-uuid"))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.category").value("BAD_REQUEST"))
      .andExpect(jsonPath("$.code").value("INVALID_EXTRACTION_ID"))
      .andExpect(jsonPath("$.message").value("[INVALID_EXTRACTION_ID] extractionId must be a valid UUID"));
  }
}
