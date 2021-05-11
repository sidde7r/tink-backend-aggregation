package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.monzo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.consent.MonzoConsentExpirationFilter;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.rpc.Response;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class MonzoConsentExpirationFilterTest {

    private MonzoConsentExpirationFilter consentExpirationFilter;

    @Before
    public void setUp() throws Exception {
        this.consentExpirationFilter =
                new MonzoConsentExpirationFilter(mock(PersistentStorage.class));
    }

    @Test
    public void shouldHandleExpiredConsentResponse() throws IOException {
        // given
        Response errorResponse =
                new ObjectMapper()
                        .readValue(
                                "{\"code\":\"forbidden.consent_sca_expired\",\"message\":\"Consent has reached it's SCA expiration\",\"params\":{\"consent_id\":\"**HASHED:HD**\"},\"retryable\":{}}",
                                Response.class);
        HttpResponse response = mock(HttpResponse.class);
        given(response.getStatus()).willReturn(403);
        given(response.getBody(Response.class)).willReturn(errorResponse);

        Filter nextFilter = mock(Filter.class);
        when(nextFilter.handle(any())).thenReturn(response);
        consentExpirationFilter.setNext(nextFilter);

        // expected
        Assertions.assertThatCode(() -> consentExpirationFilter.handle(null))
                .isInstanceOf(SessionException.class)
                .hasMessage("Consent expired. Expiring the session.");
    }
}
