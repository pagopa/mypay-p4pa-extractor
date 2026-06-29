package it.gov.pagopa.mypay2pu.extractor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.mypay2pu.extractor.config.json.JsonConfig;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.exception.ControllerExceptionHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(value = ExportController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {ExportController.class, ControllerExceptionHandler.class, JsonConfig.class})
class ExportControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void createExtractionShouldReturnAccepted() throws Exception {
    ExtractionRequest request = new ExtractionRequest(
      "00493410240",
      List.of(it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType.ORGANIZATIONS),
      new ExtractionFilters()
    );

    mockMvc.perform(post("/extract")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isAccepted())
      .andExpect(jsonPath("$.extractionId").isNotEmpty());
  }

  @Test
  void createExtractionShouldRejectMultipleFileTypes() throws Exception {
    ExtractionRequest request = new ExtractionRequest(
      "00493410240",
      List.of(
        it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType.ORGANIZATIONS,
        it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType.DEBT_POSITIONS
      ),
      null
    );

    mockMvc.perform(post("/extract")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.message").value("[BAD_REQUEST] Invalid request content. fileTypes: size must be between 1 and 1"));
  }

  @Test
  void createExtractionShouldRejectUnsupportedFileType() throws Exception {
    ExtractionRequest request = new ExtractionRequest(
      "00493410240",
      List.of(it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType.DEBT_POSITIONS),
      null
    );

    mockMvc.perform(post("/extract")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isInternalServerError())
      .andExpect(jsonPath("$.code").value("UNSUPPORTED_FILE_TYPE"))
      .andExpect(jsonPath("$.message").value("[UNSUPPORTED_FILE_TYPE] Current POC supports only ORGANIZATIONS"));
  }

  @Test
  void getExtractionStatusShouldReturnOkWithEmptyBody() throws Exception {
    mockMvc.perform(get("/extract/extraction-id"))
      .andExpect(status().isOk())
      .andExpect(content().string(""));
  }
}
