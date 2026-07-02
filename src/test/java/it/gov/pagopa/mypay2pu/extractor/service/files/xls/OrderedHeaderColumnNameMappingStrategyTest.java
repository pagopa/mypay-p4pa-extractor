package it.gov.pagopa.mypay2pu.extractor.service.files.xls;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvIgnore;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderedHeaderColumnNameMappingStrategyTest {

    static class PersonDto {
        @CsvBindByName(column = "first_name")
        private String firstName;

        @CsvBindByName(column = "last_name")
        private String lastName;

        @CsvBindByName(column = "age")
        private int age;
    }

    @Test
    void testGenerateHeaderWithBean() throws Exception {
        OrderedHeaderColumnNameMappingStrategy<PersonDto> strategy = new OrderedHeaderColumnNameMappingStrategy<>();
        strategy.setType(PersonDto.class);

        String[] headers = strategy.generateHeader(new PersonDto());

        assertNotNull(headers);
        assertEquals(3, headers.length);
        assertEquals(List.of("first_name", "last_name", "age"), List.of(headers));
    }

    @Test
    void testGenerateHeaderWithNullBean() {
        OrderedHeaderColumnNameMappingStrategy<PersonDto> strategy = new OrderedHeaderColumnNameMappingStrategy<>();
        strategy.setType(PersonDto.class);

        assertDoesNotThrow(() -> strategy.generateHeader(null));
    }

    @Test
    void testGenerateHeaderWithMissingAnnotationAndComparator() throws Exception {
        class PartialDto {
            @CsvBindByName(column = "")
            private String field1;

            private String field2;
        }

        OrderedHeaderColumnNameMappingStrategy<PartialDto> strategy = new OrderedHeaderColumnNameMappingStrategy<>();
        strategy.setType(PartialDto.class);

        String[] headers = strategy.generateHeader(new PartialDto());

        assertNotNull(headers);
        assertEquals(1, headers.length);
        assertEquals("field1", headers[0]);

        Comparator<String> comparator = Comparator.comparingInt(header -> {
            int idx = Arrays.asList(headers).indexOf(header);
            return (idx == -1) ? Integer.MAX_VALUE : idx;
        });

        assertEquals(0, comparator.compare("field1", "field1"));
        assertTrue(comparator.compare("unknown", "field1") > 0);
    }

    @Test
    void testShouldIgnoreFieldBehaviorReflection() throws Exception {
        class TestDto {
            @CsvBindByName(column = "f1")
            @CsvIgnore(profiles = {"A", "B"})
            private String f1;
        }

        OrderedHeaderColumnNameMappingStrategy<TestDto> strategy = new OrderedHeaderColumnNameMappingStrategy<>();
        Field field = TestDto.class.getDeclaredField("f1");

        strategy.setProfile(null);
        assertFalse(invokeShouldIgnoreField(strategy, field));

        strategy.setProfile("C");
        assertFalse(invokeShouldIgnoreField(strategy, field));

        strategy.setProfile("A");
        assertTrue(invokeShouldIgnoreField(strategy, field));
    }

    private boolean invokeShouldIgnoreField(OrderedHeaderColumnNameMappingStrategy<?> strategy, Field field)
            throws Exception {
        var method = OrderedHeaderColumnNameMappingStrategy.class.getDeclaredMethod("shouldIgnoreField", Field.class);
        method.setAccessible(true);
        return (boolean) method.invoke(strategy, field);
    }

    @Test
    void testSetProfileOverridesSuper() {
        OrderedHeaderColumnNameMappingStrategy<PersonDto> strategy = new OrderedHeaderColumnNameMappingStrategy<>();
        strategy.setProfile("TEST_PROFILE");

        try {
            var field = OrderedHeaderColumnNameMappingStrategy.class.getDeclaredField("currentProfile");
            field.setAccessible(true);
            assertEquals("TEST_PROFILE", field.get(strategy));
        } catch (Exception e) {
            fail("Field currentProfile not found or access failed");
        }
    }

    @Test
    void testGenerateHeaderNoCsvBindByNameFields() throws Exception {
        class EmptyDto {
            private String noAnnotation;
        }

        OrderedHeaderColumnNameMappingStrategy<EmptyDto> strategy = new OrderedHeaderColumnNameMappingStrategy<>();
        strategy.setType(EmptyDto.class);

        String[] headers = strategy.generateHeader(new EmptyDto());
        assertNotNull(headers);
        assertEquals(0, headers.length);
    }
}
