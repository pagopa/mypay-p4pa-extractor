package it.gov.pagopa.mypay2pu.extractor.service.files;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import com.opencsv.bean.CsvIgnore;
import it.gov.pagopa.mypay2pu.extractor.dto.export.CsvExportDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class TestCsv implements CsvExportDto {
    @Override
    public String getZipVersion() {
        return "1_0";
    }

    @CsvBindByName(column = "Column1", required = true)
    private String column1;
    @CsvBindByName(column = "Column2", required = true)
    private String column2;
    @CsvBindByName(column = "Column3")
    @CsvDate(value = "yyyy-MM-dd")
    private LocalDate column3;
    @CsvIgnore
    private Long lineNumber;
}
