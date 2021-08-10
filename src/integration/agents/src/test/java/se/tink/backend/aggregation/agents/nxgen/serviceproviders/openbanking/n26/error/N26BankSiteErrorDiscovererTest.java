package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.error;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;

@RunWith(JUnitParamsRunner.class)
public class N26BankSiteErrorDiscovererTest {

    private HttpClientException httpClientException;

    private N26BankSiteErrorDiscoverer objectUnderTest = new N26BankSiteErrorDiscoverer();

    @Before
    public void init() {
        httpClientException = Mockito.mock(HttpClientException.class);
    }

    @Test
    @Parameters({
        "upstream request timeout",
        "MIME media type text/plain was not found",
        "upstream connect error"
    })
    public void shouldReturnTrue(String message) {
        // given
        Mockito.when(httpClientException.getMessage()).thenReturn(message);

        // when
        boolean result = objectUnderTest.isBankSiteError(httpClientException);

        // then
        Assertions.assertThat(result).isTrue();
    }
}
