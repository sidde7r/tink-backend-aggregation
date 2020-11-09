package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingV31Constants.HSBC_ORG_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingV31Constants.NATIONWIDE_ORG_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.HttpHeaders.X_FAPI_FINANCIAL_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.HttpHeaders.X_FAPI_INTERACTION_ID;

import com.sun.jersey.core.header.OutBoundHeaders;
import com.sun.jersey.core.util.StringKeyIgnoreCaseMultivaluedMap;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class UkOpenBankingAuthenticatedHttpFilterTest {

    private static final String EXAMPLE_FAPI_INTERACTION_ID =
            "93bac548-d2de-4546-b106-880a5018460d";
    private OAuth2Token accessToken = mock(OAuth2Token.class);
    private RandomValueGenerator randomValueGenerator = mock(RandomValueGenerator.class);
    private UkOpenBankingAuthenticatedHttpFilter sut =
            new UkOpenBankingAuthenticatedHttpFilter(accessToken, randomValueGenerator);
    private HttpRequest httpRequest = mock(HttpRequest.class);
    private HttpResponse httpResponse = mock(HttpResponse.class);

    @Test
    public void testNormalUkBankInteractionIdMatching() {
        MultivaluedMap<String, String> responseHeader = new StringKeyIgnoreCaseMultivaluedMap<>();
        MultivaluedMap<String, Object> requestHeader = new OutBoundHeaders();
        requestHeader.putSingle(X_FAPI_FINANCIAL_ID, HSBC_ORG_ID);
        requestHeader.putSingle(X_FAPI_INTERACTION_ID, EXAMPLE_FAPI_INTERACTION_ID);

        responseHeader.putSingle(X_FAPI_INTERACTION_ID, EXAMPLE_FAPI_INTERACTION_ID);
        when(httpRequest.getHeaders()).thenReturn(requestHeader);
        when(httpResponse.getHeaders()).thenReturn(responseHeader);
        sut.validateInteractionIdOrThrow(httpResponse, httpRequest);
    }

    @Test
    public void testNationwideInteractionIdMismatchExclusion() {
        MultivaluedMap<String, String> responseHeader = new StringKeyIgnoreCaseMultivaluedMap<>();
        MultivaluedMap<String, Object> requestHeader = new OutBoundHeaders();
        requestHeader.putSingle(X_FAPI_FINANCIAL_ID, NATIONWIDE_ORG_ID);
        requestHeader.putSingle(X_FAPI_INTERACTION_ID, EXAMPLE_FAPI_INTERACTION_ID);

        responseHeader.putSingle(X_FAPI_INTERACTION_ID, "MIS_MATCH_ID");
        when(httpRequest.getHeaders()).thenReturn(requestHeader);
        when(httpResponse.getHeaders()).thenReturn(responseHeader);
        sut.validateInteractionIdOrThrow(httpResponse, httpRequest);
    }

    @Test(expected = HttpResponseException.class)
    public void testNormalUkBankInteractionIdMismatchThrowException() {
        MultivaluedMap<String, String> responseHeader = new StringKeyIgnoreCaseMultivaluedMap<>();
        MultivaluedMap<String, Object> requestHeader = new OutBoundHeaders();
        requestHeader.putSingle(X_FAPI_FINANCIAL_ID, HSBC_ORG_ID);
        requestHeader.putSingle(X_FAPI_INTERACTION_ID, EXAMPLE_FAPI_INTERACTION_ID);

        responseHeader.putSingle(X_FAPI_INTERACTION_ID, "MIS_MATCH_ID");
        when(httpRequest.getHeaders()).thenReturn(requestHeader);
        when(httpResponse.getHeaders()).thenReturn(responseHeader);
        sut.validateInteractionIdOrThrow(httpResponse, httpRequest);
    }
}
