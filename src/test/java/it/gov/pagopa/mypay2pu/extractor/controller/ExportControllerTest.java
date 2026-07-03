package it.gov.pagopa.mypay2pu.extractor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.mypay2pu.extractor.config.json.JsonConfig;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.exception.ControllerExceptionHandler;
import it.gov.pagopa.mypay2pu.extractor.service.ExportFileHandlerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ExportControllerTest {

  @Mock
  private ExportFileHandlerService exportFileHandlerServiceMock;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new JsonConfig().objectMapper();
    mockMvc = MockMvcBuilders
      .standaloneSetup(new ExportController(exportFileHandlerServiceMock))
      .setControllerAdvice(new ControllerExceptionHandler())
      .build();
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(exportFileHandlerServiceMock);
  }

  @Test
  void givenGeneratedExtractionIdWhenExecuteExportThenReturnAccepted() throws Exception {
    ExtractionRequest request = new ExtractionRequest(
      "IPA_CODE_TEST",
      MigrationFileType.ORGANIZATIONS,
      new ExtractionFilters()
    );
    doNothing().when(exportFileHandlerServiceMock).executeExport(anyString(), eq(request));

    mockMvc.perform(post("/extract")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isAccepted())
      .andExpect(jsonPath("$.extractionId").isNotEmpty());

  }

  @Test
  void givenInvalidExtractionIdWhenGetExtractionStatusThenReturnNotImplemented() throws Exception {
    mockMvc.perform(get("/extract/extraction-id"))
      .andExpect(status().isNotImplemented());

  }
}
