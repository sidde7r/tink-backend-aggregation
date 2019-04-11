package se.tink.backend.aggregation.agents.banks.seb.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class ServiceInputTest {
    @Test
    public void serializesStringObjectToJsonString() throws JsonProcessingException {
        ServiceInput stringInput = new ServiceInput("name", "string value");

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(stringInput);

        assertThat(json).contains("\"string value\"");
    }

    @Test
    public void serializesIntegerObjectToJsonInteger() throws JsonProcessingException {
        ServiceInput stringInput = new ServiceInput("name", 123);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(stringInput);

        assertThat(json).contains("123");
        assertThat(json).doesNotContain("\"123\"");
    }
}
