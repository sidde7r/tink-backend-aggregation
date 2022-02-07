package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.tesco;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.core.util.StringKeyIgnoreCaseMultivaluedMap;
import java.io.IOException;
import javax.ws.rs.core.MultivaluedMap;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.tesco.filter.TescoConsentExpirationFilter;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.tesco.rpc.Response;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class TescoConsentExpirationFilterTest {

    private TescoConsentExpirationFilter consentExpirationFilter;
    private HttpResponse response;
    private Filter nextFilter;

    @Before
    public void setUp() throws Exception {
        this.consentExpirationFilter =
                new TescoConsentExpirationFilter(mock(PersistentStorage.class));
        response = mock(HttpResponse.class);
        nextFilter = mock(Filter.class);
    }

    @Test
    public void shouldHandleExpiredConsentResponse() throws IOException {
        // given
        Response errorResponse =
                new ObjectMapper()
                        .readValue(
                                "{\n"
                                        + "    \"Code\": \"UK.OBIE.Reauthenticate\",\n"
                                        + "    \"Id\": \"id\",\n"
                                        + "    \"Message\": \"Reauthentication required for request\"\n"
                                        + "}",
                                Response.class);
        MultivaluedMap<String, String> responseHeader = new StringKeyIgnoreCaseMultivaluedMap<>();
        given(response.getHeaders()).willReturn(responseHeader);
        given(response.getStatus()).willReturn(403);
        given(response.getBody(Response.class)).willReturn(errorResponse);

        when(nextFilter.handle(any())).thenReturn(response);
        consentExpirationFilter.setNext(nextFilter);

        // expected
        Assertions.assertThatCode(() -> consentExpirationFilter.handle(null))
                .isInstanceOf(SessionException.class)
                .hasMessage("Consent expired. Expiring the session.");
    }

    @Test
    public void shouldNotThrowSessionExceptionWhenResponseCodeIsNotReauthenticate()
            throws IOException {
        // given
        Response errorResponse =
                new ObjectMapper()
                        .readValue(
                                "{\n"
                                        + "    \"Code\": \"other code\",\n"
                                        + "    \"Id\": \"id\",\n"
                                        + "    \"Message\": \"Reauthentication required for request\"\n"
                                        + "}",
                                Response.class);
        MultivaluedMap<String, String> responseHeader = new StringKeyIgnoreCaseMultivaluedMap<>();
        given(response.getHeaders()).willReturn(responseHeader);
        given(response.getStatus()).willReturn(403);
        given(response.getBody(Response.class)).willReturn(errorResponse);

        when(nextFilter.handle(any())).thenReturn(response);
        consentExpirationFilter.setNext(nextFilter);

        // expected
        assertThat(consentExpirationFilter.handle(null))
                .isNotExactlyInstanceOf(SessionException.class);
    }

    @Test
    public void shouldNotThrowSessionExceptionWhenResponseStatusIsNot403() throws IOException {
        // given
        Response errorResponse =
                new ObjectMapper()
                        .readValue(
                                "{\n"
                                        + "    \"Code\": \"UK.OBIE.Reauthenticate\",\n"
                                        + "    \"Id\": \"id\",\n"
                                        + "    \"Message\": \"Reauthentication required for request\"\n"
                                        + "}",
                                Response.class);
        MultivaluedMap<String, String> responseHeader = new StringKeyIgnoreCaseMultivaluedMap<>();
        given(response.getHeaders()).willReturn(responseHeader);
        given(response.getStatus()).willReturn(not(eq(403)));
        given(response.getBody(Response.class)).willReturn(errorResponse);

        when(nextFilter.handle(any())).thenReturn(response);
        consentExpirationFilter.setNext(nextFilter);

        // expected
        assertThat(consentExpirationFilter.handle(null))
                .isNotExactlyInstanceOf(SessionException.class);
    }
}
