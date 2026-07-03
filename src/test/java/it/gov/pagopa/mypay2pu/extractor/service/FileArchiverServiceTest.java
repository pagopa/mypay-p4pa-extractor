package it.gov.pagopa.mypay2pu.extractor.service;

import it.gov.pagopa.mypay2pu.extractor.config.CipherProperties;
import it.gov.pagopa.mypay2pu.extractor.service.files.ZipFileService;
import it.gov.pagopa.mypay2pu.extractor.utils.AESUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;

class FileArchiverServiceTest {

  private static final String PASSWORD = "test-password";

  @TempDir
  Path tempDir;

  private final ZipFileService zipFileService = new ZipFileService();

  @Test
  void givenSingleFileWhenCompressAndArchiveWithoutEncryptionThenCreateZipInTargetDirectory() throws IOException {
    FileArchiverService service = new FileArchiverService(new CipherProperties(false, PASSWORD), zipFileService);

    Path sourceFile = tempDir.resolve("source.txt");
    Files.writeString(sourceFile, "test content");
    Path targetDirectory = tempDir.resolve("archive");

    service.compressAndArchive(sourceFile, targetDirectory);

    Path archivedZip = targetDirectory.resolve("source.txt.zip");
    assertTrue(Files.exists(archivedZip), "Zip file should exist in target directory");
    assertFalse(Files.exists(sourceFile), "Source file should be deleted");
  }

  @Test
  void givenSingleFileWhenCompressAndArchiveWithEncryptionThenCreateEncryptedZipInTargetDirectory() throws IOException {
    FileArchiverService service = new FileArchiverService(new CipherProperties(true, PASSWORD), zipFileService);

    Path sourceFile = tempDir.resolve("source.txt");
    Files.writeString(sourceFile, "test content");
    Path targetDirectory = tempDir.resolve("archive");

    service.compressAndArchive(sourceFile, targetDirectory);

    Path archivedEncrypted = targetDirectory.resolve("source.txt.zip" + AESUtils.CIPHER_EXTENSION);
    assertTrue(Files.exists(archivedEncrypted), "Encrypted file should exist in target directory");
    assertFalse(Files.exists(sourceFile), "Source file should be deleted");
  }

  @Test
  void givenMultipleFilesWhenCompressAndArchiveWithoutEncryptionThenReturnZippedFileSize() throws IOException {
    FileArchiverService service = new FileArchiverService(new CipherProperties(false, PASSWORD), zipFileService);

    Path file1 = tempDir.resolve("file1.txt");
    Path file2 = tempDir.resolve("file2.txt");
    Files.writeString(file1, "content1");
    Files.writeString(file2, "content2");

    Path zipFilePath = tempDir.resolve("temp.zip");
    Path targetDirectory = tempDir.resolve("archive");

    Long zippedFileSize = service.compressAndArchive(List.of(file1, file2), zipFilePath, targetDirectory);

    assertTrue(zippedFileSize > 0, "Zipped file size should be greater than 0");
    assertTrue(Files.exists(targetDirectory.resolve("temp.zip")), "Archive should be in target directory");
    assertFalse(Files.exists(file1), "Source file 1 should be deleted");
    assertFalse(Files.exists(file2), "Source file 2 should be deleted");
  }

  @Test
  void givenMultipleFilesWhenCompressAndArchiveWithEncryptionThenReturnOriginalZippedFileSize() throws IOException {
    FileArchiverService service = new FileArchiverService(new CipherProperties(true, PASSWORD), zipFileService);

    Path file1 = tempDir.resolve("file1.txt");
    Path file2 = tempDir.resolve("file2.txt");
    Files.writeString(file1, "content1");
    Files.writeString(file2, "content2");

    Path zipFilePath = tempDir.resolve("temp.zip");
    Path targetDirectory = tempDir.resolve("archive");

    Long originalZipSize = service.compressAndArchive(List.of(file1, file2), zipFilePath, targetDirectory);

    assertTrue(originalZipSize > 0, "Original zipped file size should be greater than 0");
    assertTrue(Files.exists(targetDirectory.resolve("temp.zip" + AESUtils.CIPHER_EXTENSION)),
      "Encrypted archive should be in target directory");
    assertFalse(Files.exists(file1), "Source file 1 should be deleted");
    assertFalse(Files.exists(file2), "Source file 2 should be deleted");
  }

  @Test
  void givenZipFileWhenZipFileThenCreateZipArchive() throws IOException {
    FileArchiverService service = new FileArchiverService(new CipherProperties(false, PASSWORD), zipFileService);

    Path sourceFile = tempDir.resolve("source.txt");
    Files.writeString(sourceFile, "test content");
    Path zipFilePath = tempDir.resolve("archive").resolve("output.zip");

    Path createdZipPath = service.zipFile(sourceFile, zipFilePath);

    assertTrue(Files.exists(createdZipPath), "Zip file should be created");
    assertEquals(zipFilePath, createdZipPath);
    assertTrue(hasZipEntry(createdZipPath, "source.txt"));
  }

  @Test
  void givenFilesInSourceDirectoryWhenMoveFilesToTargetDirectoryThenCopyAndDeleteSourceFiles() throws IOException {
    FileArchiverService service = new FileArchiverService(new CipherProperties(false, PASSWORD), zipFileService);

    Path file1 = tempDir.resolve("file1.zip");
    Path file2 = tempDir.resolve("file2.zip");
    Files.writeString(file1, "zip content 1");
    Files.writeString(file2, "zip content 2");
    Path targetDirectory = tempDir.resolve("archive");

    service.moveFilesToTargetDirectory(List.of(file1, file2), targetDirectory);

    assertTrue(Files.exists(targetDirectory.resolve("file1.zip")), "File 1 should be in target directory");
    assertTrue(Files.exists(targetDirectory.resolve("file2.zip")), "File 2 should be in target directory");
    assertFalse(Files.exists(file1), "Source file 1 should be deleted");
    assertFalse(Files.exists(file2), "Source file 2 should be deleted");
  }

  @Test
  void givenNonExistentTargetDirectoryWhenMoveFilesToTargetDirectoryThenCreateDirectoryAndMoveFiles() throws IOException {
    FileArchiverService service = new FileArchiverService(new CipherProperties(false, PASSWORD), zipFileService);

    Path file1 = tempDir.resolve("file1.zip");
    Files.writeString(file1, "zip content");
    Path targetDirectory = tempDir.resolve("nested").resolve("archive");

    assertFalse(Files.exists(targetDirectory), "Target directory should not exist");
    service.moveFilesToTargetDirectory(List.of(file1), targetDirectory);

    assertTrue(Files.exists(targetDirectory), "Target directory should be created");
    assertTrue(Files.exists(targetDirectory.resolve("file1.zip")), "File should be in created directory");
  }

  private boolean hasZipEntry(Path zipPath, String entryName) throws IOException {
    try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        if (entry.getName().equals(entryName)) {
          return true;
        }
      }
    }
    return false;
  }
}
