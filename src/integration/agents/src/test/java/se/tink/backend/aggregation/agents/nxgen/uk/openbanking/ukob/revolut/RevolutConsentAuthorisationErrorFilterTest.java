package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.revolut;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.revolut.common.filter.RevolutConsentAuthorisationErrorFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class RevolutConsentAuthorisationErrorFilterTest {

    private RevolutConsentAuthorisationErrorFilter authorisationErrorFilter;
    private Filter nextFilter = mock(Filter.class);

    @Before
    public void setUp() throws Exception {
        this.authorisationErrorFilter =
                new RevolutConsentAuthorisationErrorFilter(mock(PersistentStorage.class));
    }

    @Test
    public void shouldCheckConsentResponseUkRevolutWhereConsentIsUnauthorized() throws Exception {
        // given
        HttpResponse response = mock(HttpResponse.class);
        ErrorResponse errorResponse = createErrorResponse();
        given(response.getStatus()).willReturn(401);
        given(response.getBody(ErrorResponse.class)).willReturn(errorResponse);
        given(nextFilter.handle(any())).willReturn(response);

        // when
        authorisationErrorFilter.setNext(nextFilter);

        // then
        Assertions.assertThatCode(() -> authorisationErrorFilter.handle(null))
                .isInstanceOf(SessionException.class)
                .hasMessage(
                        "[RevolutConsentAuthorisationErrorFilter] Consent has been revoked by the bank with message: Consent is not authorized");
    }

    private ErrorResponse createErrorResponse() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(
                "{"
                        + "\"Code\":\"401 Unauthorized\","
                        + "\"Message\":\"Consent is not authorized\","
                        + "\"Id\":\"13802IH1FATEB\","
                        + "\"Errors\":[{"
                        + "\"ErrorCode\":\"UK.OBIE.Resource.Unauthorized\","
                        + "\"Message\":\"Consent is not authorized\""
                        + "}]}",
                ErrorResponse.class);
    }
}
