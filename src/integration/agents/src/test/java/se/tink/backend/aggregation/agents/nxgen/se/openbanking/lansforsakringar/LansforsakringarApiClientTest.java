package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.agent.sdk.operation.User;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class LansforsakringarApiClientTest {

    private LansforsakringarStorageHelper storageHelper;
    private LansforsakringarApiClient apiClient;

    @Before
    public void setup() {
        storageHelper = mock(LansforsakringarStorageHelper.class);
        User user = mock(User.class);
        TinkHttpClient client = mock(TinkHttpClient.class);
        Credentials credentials = mock(Credentials.class);
        apiClient = new LansforsakringarApiClient(client, credentials, storageHelper, user);
    }

    @Test
    public void shouldReturnFalseWhenConsentIdIsNull() {
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
    public void shouldThrowConsentInvalidIfBankRejectsConsent() {
        LansforsakringarApiClient spyClient = spy(apiClient);
        doReturn(getConsentStatus("\"REJECTED\"")).when(spyClient).getConsentStatus();

        assertThatThrownBy(spyClient::isConsentValid)
                .isInstanceOf(LoginError.NOT_CUSTOMER.exception().getClass());
    }

    @Test
    public void shouldReturnFalseIfConsentStatusIsRandomString() {
        LansforsakringarApiClient spyClient = spy(apiClient);
        doReturn(getConsentStatus("\"random string\"")).when(spyClient).getConsentStatus();

        boolean result = spyClient.isConsentValid();

        assertFalse(result);
    }

    @Test
    public void shouldReturnTureIfConsentStatusIsValidButRandomlyCapitalized() {
        LansforsakringarApiClient spyClient = spy(apiClient);
        doReturn(getConsentStatus("\"VaLiD\"")).when(spyClient).getConsentStatus();

        boolean result = spyClient.isConsentValid();

        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseIfConsentStatusIsNull() {
        LansforsakringarApiClient spyClient = spy(apiClient);
        doReturn(null).when(spyClient).getConsentStatus();

        boolean result = spyClient.isConsentValid();

        assertFalse(result);
    }

    ConsentStatusResponse getConsentStatus(String status) {
        return SerializationUtils.deserializeFromString(
                "{\"consentStatus\": " + status + "}", ConsentStatusResponse.class);
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
