package it.gov.pagopa.mypay2pu.extractor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.mypay2pu.extractor.config.json.JsonConfig;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionStatusResponse;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.exception.ControllerExceptionHandler;
import it.gov.pagopa.mypay2pu.extractor.service.ExportFileHandlerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ExportControllerTest {

  @Mock
  private ExportFileHandlerService exportFileHandlerService;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new JsonConfig().objectMapper();
    mockMvc = MockMvcBuilders
      .standaloneSetup(new ExportController(exportFileHandlerService))
      .setControllerAdvice(new ControllerExceptionHandler())
      .build();
  }

  @Test
  void createExtractionShouldReturnAcceptedUsingServiceExtractionId() throws Exception {
    ExtractionRequest request = new ExtractionRequest(
      "IPA_CODE_TEST",
      MigrationFileType.ORGANIZATIONS,
      new ExtractionFilters()
    );
    String extractionId = "0d527f1b-cb46-47ae-a58e-49949d1e8cb9";

    when(exportFileHandlerService.executeExport(anyString(), eq(request)))
      .thenReturn(new ExtractionStatusResponse().extractionId(extractionId));

    mockMvc.perform(post("/extract")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isAccepted())
      .andExpect(jsonPath("$.extractionId").value(extractionId));

    verify(exportFileHandlerService).executeExport(anyString(), eq(request));
    verifyNoMoreInteractions(exportFileHandlerService);
  }

  @Test
  void createExtractionShouldGenerateUuidBeforeDelegatingToService() throws Exception {
    ExtractionRequest request = new ExtractionRequest(
      "IPA_CODE_TEST",
      MigrationFileType.ORGANIZATIONS,
      new ExtractionFilters()
    );

    when(exportFileHandlerService.executeExport(anyString(), eq(request)))
      .thenAnswer(invocation -> new ExtractionStatusResponse().extractionId(invocation.getArgument(0, String.class)));

    mockMvc.perform(post("/extract")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isAccepted())
      .andExpect(jsonPath("$.extractionId").isNotEmpty());

    ArgumentCaptor<String> extractionIdCaptor = ArgumentCaptor.forClass(String.class);
    verify(exportFileHandlerService).executeExport(extractionIdCaptor.capture(), eq(request));
    assertDoesNotThrow(() -> UUID.fromString(extractionIdCaptor.getValue()));
    verifyNoMoreInteractions(exportFileHandlerService);
  }

  @Test
  void createExtractionShouldRejectUnsupportedFileType() throws Exception {
    ExtractionRequest request = new ExtractionRequest(
      "IPA_CODE_TEST",
      MigrationFileType.DEBT_POSITIONS,
      null
    );

    mockMvc.perform(post("/extract")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value("UNSUPPORTED_FILE_TYPE"))
      .andExpect(jsonPath("$.message").value("[UNSUPPORTED_FILE_TYPE] Current POC supports only ORGANIZATIONS"));

    verifyNoInteractions(exportFileHandlerService);
  }

  @Test
  void getExtractionStatusShouldReturnOkWithEmptyBody() throws Exception {
    mockMvc.perform(get("/extract/extraction-id"))
      .andExpect(status().isOk())
      .andExpect(content().string(""));

    verifyNoInteractions(exportFileHandlerService);
  }
}
