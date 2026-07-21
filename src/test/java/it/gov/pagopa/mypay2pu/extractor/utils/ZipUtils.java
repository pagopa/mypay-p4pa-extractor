package it.gov.pagopa.mypay2pu.extractor.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;

public class ZipUtils {

  public static List<String> readZipEntries(Path archivePath) throws Exception {
    List<String> archiveEntries = new ArrayList<>();
    try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(archivePath))) {
      java.util.zip.ZipEntry zipEntry;
      while ((zipEntry = zipInputStream.getNextEntry()) != null) {
        archiveEntries.add(zipEntry.getName());
      }
    }
    return archiveEntries;
  }
}
