package it.gov.pagopa.mypay2pu.extractor.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

  // ── MP4 (primary) ──────────────────────────────────────────────────────────

  @Bean("mp4DataSource")
  @Primary
  @ConfigurationProperties("app.datasource.mp4")
  public DataSource mp4DataSource() {
    return DataSourceBuilder.create().build();
  }

  @Bean("mp4JdbcTemplate")
  @Primary
  public JdbcTemplate mp4JdbcTemplate(@Qualifier("mp4DataSource") DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

  // ── FESP (secondary) ───────────────────────────────────────────────────────

  @Bean("fespDataSource")
  @ConfigurationProperties("app.datasource.fesp")
  public DataSource fespDataSource() {
    return DataSourceBuilder.create().build();
  }

  @Bean("fespJdbcTemplate")
  public JdbcTemplate fespJdbcTemplate(@Qualifier("fespDataSource") DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }
}
