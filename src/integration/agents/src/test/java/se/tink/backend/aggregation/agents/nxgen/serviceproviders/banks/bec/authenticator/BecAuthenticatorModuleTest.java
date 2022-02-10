package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import se.tink.agent.sdk.operation.User;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecStorage;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n_aggregation.Catalog;

public class BecAuthenticatorModuleTest {

    private BecApiClient apiClient;
    private Credentials credentials;
    private BecStorage storage;
    private User user;
    private Catalog catalog;
    private SupplementalInformationController supplementalInformationController;
    private RandomValueGenerator randomValueGenerator;

    @Before
    public void setup() {
        apiClient = mock(BecApiClient.class);
        credentials = mock(Credentials.class);
        storage = mock(BecStorage.class);
        user = mock(User.class);
        catalog = mock(Catalog.class);
        supplementalInformationController = mock(SupplementalInformationController.class);
        randomValueGenerator = mock(RandomValueGenerator.class);
    }

    @Test
    public void should_initialize_bec_authenticator() {
        // given
        BecAuthenticatorModule authenticatorModule =
                new BecAuthenticatorModule(
                        apiClient,
                        credentials,
                        storage,
                        user,
                        catalog,
                        supplementalInformationController,
                        randomValueGenerator);

        // when
        BecAuthenticator becAuthenticator = authenticatorModule.createAuthenticator();

        // then
        assertThat(becAuthenticator).isNotNull();
    }
}
