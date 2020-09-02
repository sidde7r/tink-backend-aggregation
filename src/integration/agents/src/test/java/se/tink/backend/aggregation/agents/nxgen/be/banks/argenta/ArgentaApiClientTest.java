package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc.ArgentaErrorResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class ArgentaApiClientTest {

    private TinkHttpClient client;
    private ArgentaSessionStorage sessionStorage;
    private ArgentaApiClient objectUnderTest;

    @Before
    public void init() {
        client = Mockito.mock(TinkHttpClient.class);
        sessionStorage = Mockito.mock(ArgentaSessionStorage.class);
        objectUnderTest = new ArgentaApiClient(client, sessionStorage);
    }

    @Test(expected = BankServiceException.class)
    public void shouldThrowBankServiceExceptionForSomethingWrongBankResponse()
            throws LoginException, AuthorizationException {
        // given
        HttpResponseException httpResponseException = Mockito.mock(HttpResponseException.class);
        ArgentaErrorResponse argentaErrorResponse = Mockito.mock(ArgentaErrorResponse.class);
        Mockito.when(argentaErrorResponse.getMessage()).thenReturn("Oeps, er ging iets mis.");
        Mockito.when(argentaErrorResponse.getCode()).thenReturn("error.sbb");
        // when
        objectUnderTest.handleKnownErrorResponses(argentaErrorResponse, httpResponseException);
    }
}
