package it.gov.pagopa.mypay2pu.extractor.controller;

import it.gov.pagopa.mypay2pu.extractor.service.SqlLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ConfigControllerTest {

  @Mock
  private SqlLoader sqlLoaderMock;

  private ConfigController configController;

  @BeforeEach
  void init() {
    configController = new ConfigController(sqlLoaderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(sqlLoaderMock);
  }

  @Test
  void whenGetSqlLocationsThenCallSqlLoader() {
    // When
    configController.getSqlLocations();

    // Then
    verify(sqlLoaderMock).getLoadedSqlLocations();
  }

  @Test
  void whenGetSqlContentThenCallSqlLoader() {
    // Given
    String location = "testLocation";

    // When
    configController.getSqlContent(location);

    // Then
    verify(sqlLoaderMock).getLoadedSqlContent(location);
  }
}
