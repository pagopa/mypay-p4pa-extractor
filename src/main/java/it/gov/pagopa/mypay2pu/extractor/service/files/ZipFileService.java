package it.gov.pagopa.mypay2pu.extractor.service.files;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ZipFileService {

  public File zipper(Path zipFilePath, List<Path> filesToZip) {
    try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilePath.toFile()))) {
      for (Path file : filesToZip) {
        ZipEntry zipEntry = new ZipEntry(file.getFileName().toString());
        zos.putNextEntry(zipEntry);
        Files.copy(file, zos);
        zos.closeEntry();
      }
      return zipFilePath.toFile();
    } catch (IOException e) {
      throw new IllegalStateException("Error while zipping: " + zipFilePath, e);
    }
  }
}
