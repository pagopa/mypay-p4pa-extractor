package it.gov.pagopa.mypay2pu.extractor.controller;

import it.gov.pagopa.mypay2pu.extractor.controller.generated.ConfigApi;
import it.gov.pagopa.mypay2pu.extractor.service.SqlLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class ConfigController implements ConfigApi {

  private final SqlLoader sqlLoader;

  public ConfigController(SqlLoader sqlLoader) {
    this.sqlLoader = sqlLoader;
  }

  @Override
  public ResponseEntity<List<String>> getSqlLocations() {
    log.info("Requested list of loaded sql locations");
    return ResponseEntity.ok(sqlLoader.getLoadedSqlLocations().stream().toList());
  }

  @Override
  public ResponseEntity<String> getSqlContent(String location) {
    log.info("Requested SQL content for location: {}", location);
    return ResponseEntity.ok(sqlLoader.getLoadedSqlContent(location));
  }
}
