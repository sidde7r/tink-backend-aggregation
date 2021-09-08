package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class LansforsakringarSessionHandlerTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/lansforsakringar/resources";

    private LansforsakringarSessionHandler sessionHandler;
    private LansforsakringarStorageHelper storageHelper;
    private LansforsakringarApiClient apiClient;

    @Before
    public void setup() {
        storageHelper = mock(LansforsakringarStorageHelper.class);
        apiClient = mock(LansforsakringarApiClient.class);
        sessionHandler = new LansforsakringarSessionHandler(apiClient, storageHelper);
    }

    @Test
    public void shouldGetSampleAccountIdFromStorageAndKeepAlive() {
        // when
        when(storageHelper.getOAuth2Token()).thenReturn(Optional.of(getValidToken()));
        when(storageHelper.getStoredAccounts()).thenReturn(Optional.of(getAccountsResponse()));

        // then
        sessionHandler.keepAlive();
    }

    private OAuth2Token getValidToken() {
        return OAuth2Token.create(
                "Bearer",
                "123",
                "1233",
                "!23",
                200,
                LocalDateTime.now().toEpochSecond(OffsetDateTime.now().getOffset()));
    }

    @Test(expected = SessionException.class)
    public void shouldThrowSessionException() {
        // when
        when(storageHelper.getOAuth2Token()).thenReturn(Optional.of(getValidToken()));
        when(storageHelper.getStoredAccounts()).thenReturn(Optional.of(getNoResponse()));

        // then
        sessionHandler.keepAlive();
    }

    private OAuth2Token getNotValidToken() {
        return OAuth2Token.create(
                "Bearer",
                "123",
                null,
                "!23",
                0,
                LocalDateTime.now().toEpochSecond(OffsetDateTime.now().getOffset()));
    }

    private GetAccountsResponse getAccountsResponse() {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, "GetAccountsResponse.json").toFile(),
                GetAccountsResponse.class);
    }

    private GetAccountsResponse getEmptyAccountsResponse() {
        return SerializationUtils.deserializeFromString(
                "{\"accounts\":[]}", GetAccountsResponse.class);
    }

    private GetAccountsResponse getNoResponse() {
        return SerializationUtils.deserializeFromString("{}", GetAccountsResponse.class);
    }

    @Test(expected = SessionException.class)
    public void shouldNotGetAccountResponseFromStorageAndThrowSessionExceptionError() {
        // when
        when(storageHelper.getOAuth2Token()).thenReturn(Optional.of(getValidToken()));
        // then
        sessionHandler.keepAlive();
    }

    @Test(expected = SessionException.class)
    public void shouldGetEmptySampleAccountIdFromStorageAndThrowSessionExceptionError() {
        // when
        when(storageHelper.getOAuth2Token()).thenReturn(Optional.of(getValidToken()));
        when(storageHelper.getStoredAccounts()).thenReturn(Optional.of(getEmptyAccountsResponse()));

        // then
        sessionHandler.keepAlive();
    }

    @Test(expected = SessionException.class)
    public void
            shouldGetSampleAccountIdFromStorageAndThrowSessionExceptionErrorCausedByInvalidToken() {
        // when
        when(storageHelper.getStoredAccounts()).thenReturn(Optional.of(getAccountsResponse()));
        when(storageHelper.getOAuth2Token()).thenReturn(Optional.of(getNotValidToken()));
        when(apiClient.isConsentValid()).thenReturn(true);
        // then
        sessionHandler.keepAlive();
    }

    @Test(expected = SessionException.class)
    public void
            shouldddGetSampleAccountIdFromStorageAndThrowSessionExceptionErrorCausedByInvalidToken() {
        // when
        when(apiClient.isConsentValid()).thenReturn(true);
        when(apiClient.refreshToken(anyString())).thenReturn(getValidToken());

        // then
        sessionHandler.keepAlive();
    }

    @Test(expected = SessionException.class)
    public void shouldThrowSessionExceptionErrorCausedByLackOfToken() {
        sessionHandler.keepAlive();
    }
}
