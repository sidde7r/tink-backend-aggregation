package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Test;

public class EngagementResponseBodyTest {
    @Test
    public void shouldNotThrowWhenBodyNotMapped() throws IOException {
        String withBodyKey = "{\"Body\":[],\"HasDepots\":false,\"HasLoans\":false,\"HasInsurances\":false}";

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.readValue(withBodyKey, EngagementResponseBody.class);
    }
}