package se.tink.backend.aggregation.agents.banks.seb.mortgage.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class GetLoanStatusResponseTest {
    @Test
    public void deserialize() throws IOException {
        String serialized = "{\"status\":\"1\",\"description\":\"SOMEDESC\"}";

        ObjectMapper objectMapper = new ObjectMapper();
        GetLoanStatusResponse getLoanStatusResponse = objectMapper.readValue(serialized, GetLoanStatusResponse.class);

        assertThat(getLoanStatusResponse.getDescription()).isEqualTo("SOMEDESC");
        assertThat(getLoanStatusResponse.getStatus()).isEqualTo(MortgageStatus.SEB_WILL_CONTACT_CUSTOMER);
    }

    @Test
    public void serialize() throws IOException {
        GetLoanStatusResponse getLoanStatusResponse = new GetLoanStatusResponse();
        getLoanStatusResponse.setDescription("SOMEDESC");
        getLoanStatusResponse.setStatus(MortgageStatus.INCOMPLETE_APPLICATION);

        ObjectMapper objectMapper = new ObjectMapper();
        String serialized = objectMapper.writeValueAsString(getLoanStatusResponse);

        assertThat(serialized).contains("\"status\":\"2\"");
        assertThat(serialized).contains("\"description\":\"SOMEDESC\"");
    }
}
