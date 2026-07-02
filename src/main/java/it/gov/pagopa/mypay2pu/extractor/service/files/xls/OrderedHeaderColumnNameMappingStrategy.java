package it.gov.pagopa.mypay2pu.extractor.service.files.xls;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvIgnore;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class OrderedHeaderColumnNameMappingStrategy<T> extends HeaderColumnNameMappingStrategy<T> {

    private String currentProfile;

    @Override
    public void setProfile(String profile) {
        super.setProfile(profile);
        this.currentProfile = profile;
    }

    @Override
    public String[] generateHeader(T bean) throws CsvRequiredFieldEmptyException {
        if (bean == null) {
            return super.generateHeader(null);
        }

        super.generateHeader(bean);

        List<Field> declaredFields = Arrays.asList(bean.getClass().getDeclaredFields());

        List<String> orderedHeaders = declaredFields.stream()
                .filter(field -> field.isAnnotationPresent(CsvBindByName.class))
                .filter(field -> !shouldIgnoreField(field))
                .map(field -> {
                    CsvBindByName ann = field.getAnnotation(CsvBindByName.class);
                    return (ann.column() != null && !ann.column().isEmpty())
                            ? ann.column()
                            : field.getName();
                })
                .toList();

        setColumnOrderOnWrite(Comparator.comparingInt(header -> {
            int idx = orderedHeaders.indexOf(header);
            return (idx == -1) ? Integer.MAX_VALUE : idx;
        }));

        headerIndex.initializeHeaderIndex(orderedHeaders.toArray(new String[0]));
        return orderedHeaders.toArray(new String[0]);
    }

    private boolean shouldIgnoreField(Field field) {
        if (!field.isAnnotationPresent(CsvIgnore.class)) {
            return false;
        }

        CsvIgnore ignoreAnnotation = field.getAnnotation(CsvIgnore.class);
        String[] profiles = ignoreAnnotation.profiles();

        if (profiles == null || profiles.length == 0) {
            return true;
        }

        if (currentProfile == null || currentProfile.isEmpty()) {
            return false;
        }

        return Arrays.asList(profiles).contains(currentProfile);
    }
}
