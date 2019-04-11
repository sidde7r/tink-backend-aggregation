package se.tink.backend.aggregation.agents.banks.seb.mortgage.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.ApiRequest;

public class LoanPostRequestTest {
    @Test
    public void uriPath() {
        ApiRequest request = new LoanPostRequest();

        assertThat(request.getUriPath()).isEqualTo("/loans");
    }
}
