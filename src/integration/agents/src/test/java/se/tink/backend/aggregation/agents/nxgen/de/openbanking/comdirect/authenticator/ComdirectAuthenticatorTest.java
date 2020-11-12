package se.tink.backend.aggregation.agents.nxgen.de.openbanking.comdirect.authenticator;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.comdirect.ComdirectAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.Xs2aDevelopersAuthenticatorTest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersProviderConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ActualLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.MockRandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class ComdirectAuthenticatorTest {

    private final Xs2aDevelopersAuthenticatorTest xs2aDevelopersAuthenticatorTest =
            new Xs2aDevelopersAuthenticatorTest();

    @Test
    public void when_user_is_authenticated_then_session_expiry_date_is_set() {
        // given
        Date date =
                Date.from(
                        LocalDate.parse("2030-01-01")
                                .atStartOfDay()
                                .atZone(ZoneId.systemDefault())
                                .toInstant());

        TinkHttpClient tinkHttpClient = xs2aDevelopersAuthenticatorTest.mockHttpClient();
        PersistentStorage persistentStorage = new PersistentStorage();
        Credentials credentials = new Credentials();
        credentials.setType(CredentialsTypes.THIRD_PARTY_APP);

        ComdirectAuthenticator comdirectAuthenticator =
                createComdirectAuthenticator(tinkHttpClient, persistentStorage, credentials);

        AutoAuthenticationController autoAuthenticationController =
                xs2aDevelopersAuthenticatorTest.createAutoAuthenticationController(
                        comdirectAuthenticator, credentials, persistentStorage, true);

        // when
        autoAuthenticationController.authenticate(credentials);

        // then
        assertThat(credentials.getSessionExpiryDate()).isEqualTo(date);
    }

    private ComdirectAuthenticator createComdirectAuthenticator(
            TinkHttpClient tinkHttpClient,
            PersistentStorage persistentStorage,
            Credentials credentials) {
        Xs2aDevelopersProviderConfiguration xs2aDevelopersProviderConfiguration =
                new Xs2aDevelopersProviderConfiguration("clientId", "baseUrl", "redirectUrl");

        Xs2aDevelopersApiClient xs2aDevelopersApiClient =
                new Xs2aDevelopersApiClient(
                        tinkHttpClient,
                        persistentStorage,
                        xs2aDevelopersProviderConfiguration,
                        true,
                        "userIp",
                        new MockRandomValueGenerator());

        LocalDateTimeSource localDateTimeSource = new ActualLocalDateTimeSource();
        return new ComdirectAuthenticator(
                xs2aDevelopersApiClient,
                persistentStorage,
                xs2aDevelopersProviderConfiguration,
                localDateTimeSource,
                credentials);
    }
}
