package se.tink.backend.aggregation.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class TrimmingStringDeserializerTest {

    private ObjectMapper mapper;

    private static final String VALUE_1 = "  ";
    private static final String VALUE_2 = "hejsan på dejsan";
    private static final String VALUE_3 = "   hejsan på         dejsan     ";

    private static final String JSON_TEST_ENTITY_1 = "{\"a\":\""+ VALUE_1 +"\"}";
    private static final String JSON_TEST_ENTITY_2 = "{\"a\":\""+ VALUE_2 +"\"}";
    private static final String JSON_TEST_ENTITY_3 = "{\"a\":\""+ VALUE_3 +"\"}";

    private static final String JSON_TEST_ENTITY_MIXED_1 = "{\"a\":\""+ VALUE_1 +"\",\"b\":\""+ VALUE_1 +"\"}";
    private static final String JSON_TEST_ENTITY_MIXED_2 = "{\"a\":\""+ VALUE_2 +"\",\"b\":\""+ VALUE_2 +"\"}";
    private static final String JSON_TEST_ENTITY_MIXED_3 = "{\"a\":\""+ VALUE_3 +"\",\"b\":\""+ VALUE_3 +"\"}";


    private static final String JSON_TEST_ENTITY_EMPTY_1 = "{\"a\":\"\"}";
    private static final String JSON_TEST_ENTITY_EMPTY_2 = "{}";
    private static final String JSON_TEST_ENTITY_EMPTY_3 = "{\"a\":null}";

    private static final String JSON_TEST_ENTITY_NUMBER_1 = "{\"a\":5}";
    private static final String JSON_TEST_ENTITY_NUMBER_2 = "{\"a\":9.0}";
    private static final String JSON_TEST_ENTITY_NUMBER_3 = "{\"a\":NaN}";

    private static final String JSON_TEST_ENTITY_BOOLEAN_1 = "{\"a\":true}";
    private static final String JSON_TEST_ENTITY_BOOLEAN_2 = "{\"a\":false}";

    @Before
    public void setUp() {
        mapper = new ObjectMapper(new JsonFactory());
    }

    @Test
    public void shouldNotCrashEmpty1() throws IOException {
        TestEntity e = mapper.readValue(JSON_TEST_ENTITY_EMPTY_1, TestEntity.class);

        Assert.assertEquals("", e.getA());
    }
    @Test
    public void shouldNotCrashEmpty2() throws IOException {
        TestEntity e = mapper.readValue(JSON_TEST_ENTITY_EMPTY_2, TestEntity.class);

        Assert.assertEquals(null, e.getA());
    }

    @Test
    public void shouldNotCrashEmpty3() throws IOException {
        TestEntity e = mapper.readValue(JSON_TEST_ENTITY_EMPTY_3, TestEntity.class);

        Assert.assertEquals(null, e.getA());
    }

    @Test
    public void shouldNotCrashNumber4() throws IOException {
        TestEntity e = mapper.readValue(JSON_TEST_ENTITY_NUMBER_1, TestEntity.class);

        Assert.assertEquals("5", e.getA());
    }

    @Test
    public void shouldNotCrashNumber5() throws IOException {
        TestEntity e = mapper.readValue(JSON_TEST_ENTITY_NUMBER_2, TestEntity.class);

        Assert.assertEquals("9.0", e.getA());
    }

    @Test
    public void shouldNotCrashNumber6() throws IOException {
        mapper.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);
        TestEntity e = mapper.readValue(JSON_TEST_ENTITY_NUMBER_3, TestEntity.class);

        Assert.assertEquals("NaN", e.getA());
    }

    @Test
    public void shouldNotCrashBoolean7() throws IOException {
        TestEntity e = mapper.readValue(JSON_TEST_ENTITY_BOOLEAN_1, TestEntity.class);

        Assert.assertEquals("true", e.getA());
    }

    @Test
    public void shouldNotCrashBoolean8() throws IOException {
        TestEntity e = mapper.readValue(JSON_TEST_ENTITY_BOOLEAN_2, TestEntity.class);

        Assert.assertEquals("false", e.getA());
    }

    @Test
     public void deserialize1() throws IOException {
        TestEntity e = mapper.readValue(JSON_TEST_ENTITY_1, TestEntity.class);

        Assert.assertNotEquals(VALUE_1, e.getA());
        Assert.assertEquals(VALUE_1.trim(), e.getA());
    }

    @Test
    public void deserialize2() throws IOException {
        TestEntity e = mapper.readValue(JSON_TEST_ENTITY_2, TestEntity.class);

        Assert.assertEquals(VALUE_2, e.getA());
    }

    @Test
    public void deserialize3() throws IOException {
        TestEntity e = mapper.readValue(JSON_TEST_ENTITY_3, TestEntity.class);

        Assert.assertNotEquals(VALUE_3, e.getA());
        Assert.assertEquals(VALUE_3.trim(), e.getA());
    }

    @Test
    public void deserializeMixed1() throws IOException {
        TestEntityMixed e = mapper.readValue(JSON_TEST_ENTITY_MIXED_1, TestEntityMixed.class);

        Assert.assertEquals(VALUE_1, e.getA());
        Assert.assertNotEquals(VALUE_1.trim(), e.getA());
        Assert.assertEquals(VALUE_1.trim(), e.getB());
        Assert.assertNotEquals(VALUE_1, e.getB());
    }

    @Test
    public void deserializeMixed2() throws IOException {
        TestEntityMixed e = mapper.readValue(JSON_TEST_ENTITY_MIXED_2, TestEntityMixed.class);

        Assert.assertEquals(VALUE_2, e.getA());
        Assert.assertEquals(VALUE_2, e.getB());
    }

    @Test
    public void deserializeMixed3() throws IOException {
        TestEntityMixed e = mapper.readValue(JSON_TEST_ENTITY_MIXED_3, TestEntityMixed.class);

        Assert.assertEquals(VALUE_3, e.getA());
        Assert.assertNotEquals(VALUE_3.trim(), e.getA());
        Assert.assertEquals(VALUE_3.trim(), e.getB());
        Assert.assertNotEquals(VALUE_3, e.getB());
    }

    @Test
    public void deserializeWithoutCustomDeserializer1() throws IOException {
        TestEntityWithoutTrim e = mapper.readValue(JSON_TEST_ENTITY_1, TestEntityWithoutTrim.class);

        Assert.assertEquals(VALUE_1, e.getA());
        Assert.assertNotEquals(VALUE_1.trim(), e.getA());
    }

    @Test
    public void deserializeWithoutCustomDeserializer2() throws IOException {
        TestEntityWithoutTrim e = mapper.readValue(JSON_TEST_ENTITY_2, TestEntityWithoutTrim.class);

        Assert.assertEquals(VALUE_2, e.getA());
    }

    @Test
    public void deserializeWithoutCustomDeserializer3() throws IOException {
        TestEntityWithoutTrim e = mapper.readValue(JSON_TEST_ENTITY_3, TestEntityWithoutTrim.class);

        Assert.assertEquals(VALUE_3, e.getA());
        Assert.assertNotEquals(VALUE_3.trim(), e.getA());
    }

    public static class TestEntity {

        private String a;
        public TestEntity() {

        }

        public String getA() {
            return a;
        }

        @JsonDeserialize(using = TrimmingStringDeserializer.class)
        public void setA(String a) {
            this.a = a;
        }
    }

    public static class TestEntityMixed {

        private String a;
        private String b;
        public TestEntityMixed() {

        }

        public String getA() {
            return a;
        }

        public void setA(String a) {
            this.a = a;
        }

        public String getB() {
            return b;
        }

        @JsonDeserialize(using = TrimmingStringDeserializer.class)
        public void setB(String b) {
            this.b = b;
        }
    }

    public static class TestEntityWithoutTrim {

        private String a;

        public TestEntityWithoutTrim() {

        }
        public String getA() {
            return a;
        }

        public void setA(String a) {
            this.a = a;
        }
    }
}
