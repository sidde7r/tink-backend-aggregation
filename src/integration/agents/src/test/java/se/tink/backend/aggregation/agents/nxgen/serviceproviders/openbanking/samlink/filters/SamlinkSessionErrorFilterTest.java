package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import javax.ws.rs.core.MediaType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.filter.SamlinkSessionErrorFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RunWith(MockitoJUnitRunner.class)
public class SamlinkSessionErrorFilterTest {

    private SamlinkSessionErrorFilter sessionErrorFilter;

    @Before
    public void setUp() {
        sessionErrorFilter = new SamlinkSessionErrorFilter();
    }

    @Test
    public void shouldThrowSessionErrorWhenConsentExpired() {

        // given
        final String responseBody =
                "{\n"
                        + "\t\"tppMessages\": [\n"
                        + "\t\t{\n"
                        + "\t\t\t\"category\": \"ERROR\",\n"
                        + "\t\t\t\"code\": \"CONSENT_EXPIRED\",\n"
                        + "\t\t\t\"path\": \"Consent-ID\",\n"
                        + "\t\t\t\"text\": \"Consent is not valid\"\n"
                        + "\t\t}\n"
                        + "\t]\n"
                        + "}";

        HttpResponse mockedResponse = mockResponse(responseBody);
        Filter nextFilter = mock(Filter.class);
        when(nextFilter.handle(any())).thenThrow(new HttpResponseException(null, mockedResponse));

        // when
        sessionErrorFilter.setNext(nextFilter);
        Throwable t = catchThrowable(() -> sessionErrorFilter.handle(null));

        // then
        assertThat(t).isInstanceOf(SessionException.class).hasMessage("Cause: Consent is expired");
    }

    static HttpResponse mockResponse(String responseBody) {
        ErrorResponse errorResponse = new Gson().fromJson(responseBody, ErrorResponse.class);
        HttpResponse mocked = mock(HttpResponse.class);
        when(mocked.getBody(ErrorResponse.class)).thenReturn(errorResponse);
        when(mocked.getType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);
        return mocked;
    }
}
