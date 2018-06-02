package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator;

import java.util.Optional;
import org.junit.Test;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSETestConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class HandelsbankenSEBankIdAuthenticatorTest {

    @Test
    public void canAuthenticate() throws Exception {
        Credentials credentials = new Credentials();
        credentials.setType(CredentialsTypes.MOBILE_BANKID);
        credentials.setField(Field.Key.USERNAME, HandelsbankenSETestConfig.HEDBERG);

        HandelsbankenSEConfiguration configuration = new HandelsbankenSEConfiguration();
        HandelsbankenPersistentStorage persistentStorage = new HandelsbankenPersistentStorage(new PersistentStorage());
        HandelsbankenSessionStorage sessionStorage = new HandelsbankenSessionStorage(new SessionStorage(),
                configuration);
        new BankIdAuthenticationController<>(
                mock(AgentContext.class),
                new HandelsbankenBankIdAuthenticator(new HandelsbankenSEApiClient(new TinkHttpClient(null, credentials),
                        configuration),
                        credentials, persistentStorage, sessionStorage)
        ).authenticate(credentials);

        assertThat(persistentStorage.getAuthorizeResponse(), not(Optional.empty()));
        assertThat(sessionStorage.applicationEntryPoint(), not(Optional.empty()));

    }
}
