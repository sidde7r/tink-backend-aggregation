package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.identity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.OpenbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.rpc.IdentityResponse;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.identitydata.IdentityData;

@RunWith(MockitoJUnitRunner.class)
public class OpenbankIdentityDataFetcherTest {

    private OpenbankApiClient openbankApiClient;
    private OpenbankIdentityDataFetcher dataFetcher;
    private HttpResponseException httpResponseException;

    @Before
    public void setup() throws IOException {
        openbankApiClient = mock(OpenbankApiClient.class);
        dataFetcher = new OpenbankIdentityDataFetcher(openbankApiClient);

        HttpRequest request = mock(HttpRequestImpl.class);
        HttpResponse response = mock(HttpResponse.class);
        httpResponseException = new HttpResponseException(request, response);
    }

    @Test
    public void shouldFetchIdentity() {
        // given
        IdentityResponse response_correct_data =
                OpenbankIdentityDataFixtures.DUMMY_IDENTITY.json(IdentityResponse.class);
        when(openbankApiClient.getUserIdentity()).thenReturn(response_correct_data);

        // when
        IdentityData response = dataFetcher.fetchIdentityData();

        // then
        assertNotNull(response);
        assertEquals(
                "failure - birth dates are not equal",
                "1949-10-23",
                response.getDateOfBirth().toString());
        assertEquals("failure - full names are not equal", "JOHN DOE", response.getFullName());
        assertEquals("failure - ssn are not equal", "12345678V", response.getSsn());
    }

    @Test(expected = BankServiceException.class)
    public void throwBankServiceException() {
        // given
        when(openbankApiClient.getUserIdentity()).thenThrow(httpResponseException);
        when(httpResponseException.getResponse().getStatus()).thenReturn(500);

        // when
        dataFetcher.fetchIdentityData();
    }

    @Test(expected = HttpResponseException.class)
    public void throwUnhandledException() {
        // given
        when(openbankApiClient.getUserIdentity()).thenThrow(httpResponseException);
        when(httpResponseException.getResponse().getStatus()).thenReturn(400);

        // when
        dataFetcher.fetchIdentityData();
    }
}
