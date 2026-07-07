package it.gov.pagopa.mypay2pu.extractor.service;

import it.gov.pagopa.mypay2pu.extractor.service.files.ZipFileService;
import it.gov.pagopa.mypay2pu.extractor.utils.AESUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Lazy
@Service
public class FileArchiverService {

  private final boolean fileEncryptEnabled;
  private final String dataCipherPsw;
  private final ZipFileService zipFileService;

  public FileArchiverService(
    @Value("${cipher.file-encrypt-enabled}") boolean fileEncryptEnabled,
    @Value("${cipher.file-encrypt-psw}") String dataCipherPsw,
    ZipFileService zipFileService
  ) {
    this.fileEncryptEnabled = fileEncryptEnabled;
    this.dataCipherPsw = dataCipherPsw;
    this.zipFileService = zipFileService;
  }

  public Path zipFile(Path sourceFile, Path zipFilePath) throws IOException {
    Files.createDirectories(zipFilePath.getParent());
    return zipFileService.zipper(zipFilePath, List.of(sourceFile)).toPath();
  }

  public void compressAndArchive(Path sourceFile, Path targetDirectory) throws IOException {
    Path tmpZipFilePath = sourceFile
      .getParent()
      .resolve(sourceFile.getFileName() + ".zip");
    compressAndArchive(List.of(sourceFile), tmpZipFilePath, targetDirectory);
  }

  public Long compressAndArchive(List<Path> files2Archive, Path file2Zip, Path targetPath) throws IOException {
    Files.createDirectories(file2Zip.getParent());
    File zipped = zipFileService.zipper(file2Zip, files2Archive);
    Long zippedFileSize = zipped.length();
    Path resultFile = zipped.toPath();
    if (fileEncryptEnabled) {
      File encrypted = AESUtils.encrypt(dataCipherPsw, zipped);
      Files.deleteIfExists(zipped.toPath());
      resultFile = encrypted.toPath();
    }
    for (Path path : files2Archive) {
      Files.deleteIfExists(path);
    }

    moveFilesToTargetDirectory(List.of(resultFile), targetPath);
    return zippedFileSize;
  }

  public void moveFilesToTargetDirectory(List<Path> files2Archive, Path targetPath) {
    try {
      Files.createDirectories(targetPath);
      for (Path file : files2Archive) {
        Files.copy(file, targetPath.resolve(file.getFileName()), REPLACE_EXISTING);
        Files.deleteIfExists(file);
      }
    } catch (IOException e) {
      throw new IllegalStateException("Cannot archive files: " + files2Archive + " into destination: " + targetPath, e);
    }
  }

}
