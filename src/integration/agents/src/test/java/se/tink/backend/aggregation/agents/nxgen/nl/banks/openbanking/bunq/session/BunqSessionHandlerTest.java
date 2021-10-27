package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.jackson.datatype.VavrModule;
import java.io.IOException;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.BunqApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.BunqClientAuthTokenHandler;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.entities.ErrorEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.fetchers.transactional.rpc.AccountsResponseWrapper;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

/**
 * @author Nazri Abdullah
 * @version 1.0
 * @date 2021-10-27 22:56
 */
public class BunqSessionHandlerTest {

    private static final String DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/nl/banks/openbanking/bunq/session/resources";

    private BunqApiClient apiClient;
    private BunqClientAuthTokenHandler clientAuthTokenHandler;
    private SessionStorage sessionStorage;
    private BunqSessionHandler sessionHandler;

    @Before
    public void setUp() {
        apiClient = mock(BunqApiClient.class);
        clientAuthTokenHandler = mock(BunqClientAuthTokenHandler.class);
        sessionStorage = mock(SessionStorage.class);
        sessionHandler = new BunqSessionHandler(apiClient, clientAuthTokenHandler, sessionStorage);
    }

    @Test
    public void shouldNotThrowIfSessionIsStillAlive() throws IOException {

        // given
        final String userId = "dummyUserId";
        final AccountsResponseWrapper accountsResponse =
                loadSampleData("accounts_response.json", AccountsResponseWrapper.class);
        when(sessionStorage.containsKey(BunqBaseConstants.StorageKeys.USER_ID)).thenReturn(true);
        when(apiClient.listAccounts(userId)).thenReturn(accountsResponse);

        // when
        Throwable throwable = catchThrowable(() -> sessionHandler.keepAlive());

        // then
        Assert.assertNull(throwable);
    }

    @Test
    public void shouldThrowExceptionIfNoLongerAlive() throws SessionException {
        // given
        final String responseBody =
                "{\n"
                        + "\t\"Error\": [\n"
                        + "\t\t{\n"
                        + "\t\t\t\"error_description\": \"Insufficient authorisation.\",\n"
                        + "\t\t\t\"error_description_translated\": \"Insufficient authorisation.\"\n"
                        + "\t\t}\n"
                        + "\t]\n"
                        + "}";

        HttpResponseException exception = mockResponse(401, responseBody);
        when(apiClient.listAccounts("dummyUserId")).thenThrow(exception);

        // when
        Throwable thrown = catchThrowable(() -> sessionHandler.keepAlive());

        // then
        assertThat(thrown).isExactlyInstanceOf(SessionException.class);
    }

    private HttpResponseException mockResponse(int status, String responseBody) {
        HttpResponse mocked = mock(HttpResponse.class);
        when(mocked.getStatus()).thenReturn(status);
        HttpResponseException exception = new HttpResponseException(null, mocked);

        when(exception.getResponse().getBody(ErrorEntity.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(responseBody, ErrorEntity.class));

        return exception;
    }

    private <T> T loadSampleData(String path, Class<T> cls) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new VavrModule());
        return objectMapper.readValue(Paths.get(DATA_PATH, path).toFile(), cls);
    }
}
