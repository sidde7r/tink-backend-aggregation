package se.tink.backend.aggregation.utils.json.deserializer;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class IdentifierMapDeserializerTest {

    private static final String VALID_JSON = "{\"TestEntities\":[{\"name\":\"adam\",\"data\":\"oranges\"},{\"name\":\"steve\",\"data\":\"thoughts\"}]}";
    private static final String MISSING_NAME_ATTR = "{\"TestEntities\":[{\"data\":\"oranges\"},{\"data\":\"thoughts\"}]}";
    private static final String NOT_AN_ARRAY = "{\"TestEntities\":{\"data\":\"oranges\"}}";
    private static final String NAME_IS_NOT_STRING = "{\"TestEntities\":[{\"name\":{\"value\":\"adam\"},\"data\":\"oranges\"}]}";

    private ImmutableMap<String, TestResultEntity> expectedResult;

    @Before
    public void setup() {
        expectedResult = ImmutableMap.<String, TestResultEntity>builder()
                .put("adam", new TestResultEntity("adam", "oranges"))
                .put("steve", new TestResultEntity("steve", "thoughts"))
                .build();
    }

    @Test
    public void testValidInput() {

        try {
            Map<String, TestResultEntity> resultMap = deserialize(VALID_JSON).getEntities();
            Assert.assertEquals(resultMap, expectedResult);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(expected = JsonMappingException.class)
    public void testMissingName() throws IOException {
        deserialize(MISSING_NAME_ATTR);
    }

    @Test(expected = JsonMappingException.class)
    public void testNotArray() throws IOException {
        deserialize(NOT_AN_ARRAY);
    }

    @Test(expected = JsonMappingException.class)
    public void testNameNotString() throws IOException {
        deserialize(NAME_IS_NOT_STRING);
    }

    private TestRootEntity deserialize(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, TestRootEntity.class);
    }
}
