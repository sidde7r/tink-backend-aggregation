package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.identity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Ignore;
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
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(MockitoJUnitRunner.class)
public class OpenbankIdentityDataFetcherTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/openbank/resources";

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

    @Ignore("WIP")
    @Test
    public void shouldFetchIdentity() {
        // given
        IdentityResponse response_correct_data =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "identity_correct_response.json").toFile(),
                        IdentityResponse.class);
        when(openbankApiClient.getUserIdentity()).thenReturn(response_correct_data);

        // when
        IdentityData response = dataFetcher.fetchIdentityData();

        // then
        assertNotNull(response);
        assertEquals(
                "failure - birth dates are not equal", "1949-10-23", response.getDateOfBirth());
        assertEquals("failure - full names are not equal", "JOHN DOE", response.getFullName());
        assertEquals("failure - birth dates are not equal", "JOHN DOE", response.getNameElements());
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
