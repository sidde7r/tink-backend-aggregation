package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(Parameterized.class)
public class ConsentStatusRetryTest {

    private HttpRequest httpRequest;
    private ConsentStatusRetryFilter objectUnderTest;

    private final String consentStatus;
    private final boolean expectedRetry;

    @Before
    public void init() {
        httpRequest = mock(HttpRequest.class);
        objectUnderTest = new ConsentStatusRetryFilter();

        when(httpRequest.getUrl()).thenReturn(new URL("urlBase/consent/something/status"));
    }

    public ConsentStatusRetryTest(String consentStatus, boolean expectedRetry) {
        this.consentStatus = consentStatus;
        this.expectedRetry = expectedRetry;
    }

    @Parameters(name = "Test with {0}, result: {1}\"")
    public static Collection<Object[]> consentStatusRetry() {
        return Arrays.asList(
                new Object[] {"RCVD", true},
                new Object[] {"ACTC", false},
                new Object[] {"RJCT", false},
                new Object[] {"CANC", false});
    }

    @Test
    public void shouldRetryWhenConsentRCVD() {
        // given
        HttpResponse httpResponse = mockResponseWithStatus(consentStatus);

        // when
        boolean result = objectUnderTest.shouldRetry(httpResponse);

        // then
        assertThat(result).isEqualTo(expectedRetry);
    }

    private HttpResponse mockResponseWithStatus(String status) {
        ConsentStatusResponse consentStatusResponseMock =
                SerializationUtils.deserializeFromString(
                        "{\"transactionStatus\":\"" + status + "\"}", ConsentStatusResponse.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getRequest()).thenReturn(httpRequest);
        when(httpResponse.getBody(ConsentStatusResponse.class))
                .thenReturn(consentStatusResponseMock);
        when(httpResponse.hasBody()).thenReturn(true);
        return httpResponse;
    }
}
