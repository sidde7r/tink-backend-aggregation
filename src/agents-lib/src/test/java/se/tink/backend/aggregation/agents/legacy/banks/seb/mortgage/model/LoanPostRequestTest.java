package se.tink.backend.aggregation.agents.banks.seb.mortgage.model;

import org.junit.Test;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.ApiRequest;
import static org.assertj.core.api.Assertions.assertThat;

public class LoanPostRequestTest {
    @Test
    public void uriPath() {
        ApiRequest request = new LoanPostRequest();

        assertThat(
                request.getUriPath())
                .isEqualTo("/loans");
    }
}