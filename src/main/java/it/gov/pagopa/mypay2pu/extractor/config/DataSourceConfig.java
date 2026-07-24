package it.gov.pagopa.mypay2pu.extractor.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

  // ── MP4 (primary) ──────────────────────────────────────────────────────────

  @Bean("mp4DataSource")
  @Primary
  @ConfigurationProperties("datasource.mp4")
  public DataSource mp4DataSource() {
    return DataSourceBuilder.create().build();
  }

  @Bean("mp4JdbcTemplate")
  @Primary
  public JdbcTemplate mp4JdbcTemplate(@Qualifier("mp4DataSource") DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

  @Bean("mp4NamedParameterJdbcTemplate")
  @Primary
  public NamedParameterJdbcTemplate mp4NamedParameterJdbcTemplate(@Qualifier("mp4DataSource") DataSource dataSource) {
    return new NamedParameterJdbcTemplate(dataSource);
  }

  // ── FESP (secondary) ───────────────────────────────────────────────────────

  @Bean("fespDataSource")
  @ConfigurationProperties("datasource.fesp")
  public DataSource fespDataSource() {
    return DataSourceBuilder.create().build();
  }

  @Bean("fespJdbcTemplate")
  public JdbcTemplate fespJdbcTemplate(@Qualifier("fespDataSource") DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

  @Bean("fespNamedParameterJdbcTemplate")
  public NamedParameterJdbcTemplate fespNamedParameterJdbcTemplate(@Qualifier("fespDataSource") DataSource dataSource) {
    return new NamedParameterJdbcTemplate(dataSource);
  }


  @Bean("mpv4DataSource")
  @ConditionalOnExpression(
    "'${MPV4_DB_HOST:}' != '' and '${MPV4_DB_USER:}' != '' and '${MPV4_DB_PASSWORD:}' != '' and '${MPV4_DB_NAME:}' != ''"
  )
  @ConfigurationProperties("datasource.mpv4")
  public DataSource mpv4DataSource() {
    return DataSourceBuilder.create().build();
  }

  @Bean("mpv4JdbcTemplate")
  public JdbcTemplate mpv4JdbcTemplate(@Qualifier("mpv4DataSource") DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }


  @Bean("mpv4NamedParameterJdbcTemplate")
  @ConditionalOnBean(name = "mpv4DataSource")
  public NamedParameterJdbcTemplate mpv4NamedParameterJdbcTemplate(@Qualifier("mpv4DataSource") DataSource dataSource) {
    return new NamedParameterJdbcTemplate(dataSource);
  }
}
