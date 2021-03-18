package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.agentplatform.authentication.ObjectMapperFactory;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarDataAccessorFactory;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.MockRandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RunWith(JUnitParamsRunner.class)
public class FetcherApiClientTest {

    @Test
    @Parameters(method = "errorsParams")
    public void shouldThrowBankSideErrorOrOriginal(int status, RuntimeException expectedException) {
        // given
        TinkHttpClient client = mock(TinkHttpClient.class);
        FetcherApiClient apiClient =
                new FetcherApiClient(
                        client,
                        new PersistentStorage(),
                        new LunarDataAccessorFactory(new ObjectMapperFactory().getInstance()),
                        new MockRandomValueGenerator(),
                        "testCode");
        HttpResponse httpResponse = mock(HttpResponse.class);

        // and
        when(client.request(any(URL.class)))
                .thenThrow(new HttpResponseException(null, httpResponse));
        when(httpResponse.getStatus()).thenReturn(status);

        // when
        Throwable result = catchThrowable(apiClient::fetchInvestments);

        // then
        assertThat(result).isInstanceOf(expectedException.getClass());
        assertThat(result).hasMessage(expectedException.getMessage());
    }

    private Object[] errorsParams() {
        return new Object[] {
            new Object[] {500, BankServiceError.BANK_SIDE_FAILURE.exception("Http status: " + 500)},
            new Object[] {502, BankServiceError.NO_BANK_SERVICE.exception("Http status: " + 502)},
            new Object[] {503, BankServiceError.NO_BANK_SERVICE.exception("Http status: " + 503)},
            new Object[] {504, BankServiceError.NO_BANK_SERVICE.exception("Http status: " + 504)},
            new Object[] {404, new HttpResponseException(null, null)},
        };
    }
}
