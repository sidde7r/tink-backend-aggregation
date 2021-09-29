package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RunWith(JUnitParamsRunner.class)
public class LansforsakringarApiClientTest {

    private LansforsakringarStorageHelper storageHelper;
    private LansforsakringarApiClient apiClient;

    @Before
    public void setup() {
        storageHelper = mock(LansforsakringarStorageHelper.class);
        LansforsakringarUserIpInformation userIpInformation =
                mock(LansforsakringarUserIpInformation.class);
        TinkHttpClient client = mock(TinkHttpClient.class);
        Credentials credentials = mock(Credentials.class);
        apiClient =
                new LansforsakringarApiClient(
                        client, credentials, storageHelper, userIpInformation);
    }

    @Test
    public void shouldReturnThatConsentIsNotValidWhenIdIsNull() {
        // when
        when(storageHelper.getConsentId()).thenReturn(null);
        // then
        assertFalse(apiClient.isConsentValid());
    }

    @Test
    @Parameters(method = "getExceptions")
    public void shouldReturnSessionErrorCausedByException(Exception exception) {
        // when
        when(apiClient.getConsentStatus()).thenThrow(exception);
        // then
        assertThatThrownBy(() -> apiClient.isConsentValid())
                .isInstanceOf(SessionError.SESSION_EXPIRED.exception().getClass());
    }

    @Test
    public void shouldReturnThatConsentIsNotValidWhenThereIsNoConsentResponse() {
        assertFalse(apiClient.isConsentValid());
    }

    private RuntimeException getRuntime() {
        return mock(RuntimeException.class);
    }

    private HttpResponseException getHttp() {
        return mock(HttpResponseException.class);
    }

    private Object[] getExceptions() {
        return new Object[] {
            getRuntime(), getHttp(),
        };
    }
}
