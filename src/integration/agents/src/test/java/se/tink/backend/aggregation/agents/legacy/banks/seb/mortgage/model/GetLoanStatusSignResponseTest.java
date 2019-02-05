package se.tink.backend.aggregation.agents.banks.seb.mortgage.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class GetLoanStatusSignResponseTest {
    @Test
    public void deserializesEnumCorrectly() throws IOException {
        String json = "{\"status\":\"COMPLETE\"}";

        GetLoanStatusSignResponse response = new ObjectMapper().readValue(json, GetLoanStatusSignResponse.class);

        assertThat(response.getStatus())
                .isNotNull()
                .isEqualTo(GetLoanStatusSignResponse.BankIdStatus.COMPLETE);
    }

    @Test
    public void deserializesErrorDescription() throws IOException {
        String json = "{\"status\":\"COMPLETE\",\"errors_description\":\"Description of error\"}";

        GetLoanStatusSignResponse response = new ObjectMapper().readValue(json, GetLoanStatusSignResponse.class);

        assertThat(response.getErrorsDescription()).isEqualTo("Description of error");
    }
}