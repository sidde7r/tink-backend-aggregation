package se.tink.backend.aggregation.agents.banks.seb.mortgage.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.ApiRequest;

public class GetLoanStatusRequestTest {
    @Test
    public void uriPath() {
        ApiRequest request = new GetLoanStatusRequest("application-id-123");

        assertThat(request.getUriPath()).isEqualTo("/loans/application-id-123/status");
    }

    @Test
    public void uriIsEncoded() {
        ApiRequest request = new GetLoanStatusRequest("application id åäö");

        assertThat(request.getUriPath())
                .doesNotContain("application id åäö")
                .contains("application%20id%20%C3%A5%C3%A4%C3%B6");
    }
}
