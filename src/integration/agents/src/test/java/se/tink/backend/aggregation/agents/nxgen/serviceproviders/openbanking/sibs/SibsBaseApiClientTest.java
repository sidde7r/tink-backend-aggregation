package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.dictionary.SibsPaymentType;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class SibsBaseApiClientTest {

    private SibsBaseApiClient sibsBaseApiClient;
    private TinkHttpClient tinkHttpClient = mock(TinkHttpClient.class);
    private String ANY_STRING = "thisstringdoesntmatter";

    @Before
    public void setUp() {
        SibsBaseApiClient apiSpy =
                new SibsBaseApiClient(
                        tinkHttpClient, mock(SibsUserState.class), ANY_STRING, false, "0.0.0.0");
        sibsBaseApiClient = spy(apiSpy);
    }

    @Test
    public void getPaymentStatus() {
        URL url = new URL(ANY_STRING);
        doReturn(url).when(sibsBaseApiClient).createUrl(anyString());
        doThrow(HttpResponseException.class)
                .when(sibsBaseApiClient)
                .getSibsGetPaymentStatusResponse(
                        ANY_STRING, SibsPaymentType.SEPA_CREDIT_TRANSFERS, url);

        try {
            sibsBaseApiClient.getPaymentStatus(ANY_STRING, SibsPaymentType.SEPA_CREDIT_TRANSFERS);
        } catch (TransferExecutionException e) {
            assertEquals(e.getUserMessage(), "This type of payment is unavailable for you");
        }
    }
}
