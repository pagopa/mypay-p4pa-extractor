package it.gov.pagopa.mypay2pu.extractor.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import it.gov.pagopa.mypay2pu.extractor.service.SqlLoader;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SqlTestUtils {
  private SqlTestUtils() {}

  private static final Path readmeFilePath = Path.of("README.md");
  private static final int QUERY_CUSTOMIZATION_PARAGRAPH_LINE_NUMBER = getQueryCustomizationParagraphLineNumber();
  private static final Map<String, Integer> SOURCE2README_PARAGRAPH_LINE_NUMBER =
    Stream.of("mypay", "fesp", "mypivot")
      .collect(Collectors.toMap(
        source -> source,
        SqlTestUtils::getQueryCustomizationSourceParagraphLineNumber
      ));

  @SneakyThrows
  private static int getQueryCustomizationParagraphLineNumber() {
    try (Stream<String> readmeLines = Files.lines(readmeFilePath)) {
      int[] counter = {0};
      String queryCustomizationParagraph = "## Extraction queries customization";
      String found = readmeLines
        .peek(x -> counter[0]++)
        .filter(line -> line.equals(queryCustomizationParagraph))
        .findFirst().orElse(null);
      Assertions.assertNotNull(found, "Missing '%s' paragraph in README.md".formatted(queryCustomizationParagraph));
      return counter[0];
    }
  }

  @SneakyThrows
  private static int getQueryCustomizationSourceParagraphLineNumber(String source) {
    try (Stream<String> readmeLines = Files.lines(readmeFilePath)) {
      int[] counter = {0};
      String found = readmeLines.skip(QUERY_CUSTOMIZATION_PARAGRAPH_LINE_NUMBER)
        .peek(x -> counter[0]++)
        .filter(line -> line.startsWith("###"))
        .map(line -> line.substring(3).trim().toLowerCase())
        .filter(line -> line.equals(source))
        .findFirst()
        .orElse(null);
      Assertions.assertNotNull(found);
      return counter[0] + QUERY_CUSTOMIZATION_PARAGRAPH_LINE_NUMBER;
    }
  }

  /** It will check the presence of the parameters inside the query and its documentation inside the README */
  public static void assertQueryParameters(String sqlFilePath, List<String> parameters) {
    ((Logger) LoggerFactory.getLogger(SqlLoader.class)).setLevel(Level.ERROR);
    assertSqlContent(sqlFilePath, parameters);
    assertReadmeContent(sqlFilePath, parameters);
  }

  private static void assertSqlContent(String sqlFilePath, List<String> parameters) {
    String queryContent = new SqlLoader().load(sqlFilePath);
    parameters.forEach(param -> Assertions.assertTrue(queryContent.contains(":" + param), "Missing parameter in SQL query: " + param));

    Pattern parameterPattern = Pattern.compile(":[a-zA-Z0-9_]+");
    Set<String> foundParameters = parameterPattern.matcher(queryContent).results()
      .map(matchResult -> matchResult.group().substring(1))
      .collect(Collectors.toSet());
    Assertions.assertEquals(parameters.size(), foundParameters.size(), "Mismatch in number of parameters in SQL query: " + sqlFilePath);
  }

  @SneakyThrows
  private static void assertReadmeContent(String sqlFilePath, List<String> parameters) {
    String source = sqlFilePath.split("/")[0];
    Integer sourceParagraphLineNumber = SOURCE2README_PARAGRAPH_LINE_NUMBER.get(source);
    Assertions.assertNotNull(sourceParagraphLineNumber, "Unknown sql file root path: " + source);

    String foundReadmeLine;
    try (Stream<String> readmeLines = Files.lines(readmeFilePath)) {
      foundReadmeLine = readmeLines
        .skip(sourceParagraphLineNumber)
        .filter(line -> line.contains("`/db/" + sqlFilePath + "`") || line.startsWith("#"))
        .findFirst().orElse(null);
    }
    Assertions.assertNotNull(foundReadmeLine, "Missing SQL file reference in README.md: " + sqlFilePath);
    Assertions.assertFalse(foundReadmeLine.startsWith("#"), "Missing SQL file reference in README.md: " + sqlFilePath);

    String parametersInReadme = foundReadmeLine.split("\\|")[3];
    parameters.forEach(param ->
      Assertions.assertTrue(parametersInReadme.contains("<li>" + param + ":"),
        "Missing parameter in README.md for SQL file: " + sqlFilePath + ", parameter: " + param)
    );
    Assertions.assertEquals(parameters.size(), parametersInReadme.split("<li>").length - 1,
      "Mismatch in number of parameters in README.md for SQL file: " + sqlFilePath
    );
  }
}
