package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ImaginBankPasswordAuthenticatorTest {
    private ImaginBankPasswordAuthenticator authenticator;
    private ImaginBankApiClient apiClient;
    private SupplementalInformationHelper supplementalInformationHelper;
    private static final String RESOURCE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/imaginbank/resources/";
    private static final String USERNAME = "dummyUsername";
    private static final String PASSWORD = "dummyPassword";

    @Before
    public void init() {
        apiClient = mock(ImaginBankApiClient.class);
        supplementalInformationHelper = mock(SupplementalInformationHelper.class);
        authenticator =
                new ImaginBankPasswordAuthenticator(
                        apiClient,
                        new ImaginBankSessionStorage(new SessionStorage()),
                        supplementalInformationHelper);
    }

    @Test
    public void authenticateShouldThrowNotSupportedExceptionIfFlowNotSupported() {
        // given
        when(apiClient.initializeSession(USERNAME))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(RESOURCE_PATH + "session_response_not_supported.json")
                                        .toFile(),
                                SessionResponse.class));
        Credentials credentials = new Credentials();
        credentials.setField(Field.Key.USERNAME, USERNAME);
        credentials.setField(Field.Key.PASSWORD, PASSWORD);

        // when
        Throwable exception = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(exception).isInstanceOf(LoginException.class);
    }
}
