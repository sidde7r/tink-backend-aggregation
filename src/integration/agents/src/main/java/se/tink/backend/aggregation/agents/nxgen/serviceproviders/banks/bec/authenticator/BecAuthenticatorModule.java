package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.RequiredArgsConstructor;
import se.tink.agent.sdk.operation.User;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecStorage;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n_aggregation.Catalog;

@RequiredArgsConstructor
public class BecAuthenticatorModule extends AbstractModule {

    private final BecApiClient apiClient;
    private final Credentials credentials;
    private final BecStorage storage;
    private final User user;
    private final Catalog catalog;
    private final SupplementalInformationController supplementalInformationController;
    private final RandomValueGenerator randomValueGenerator;

    @Override
    public void configure() {
        bind(BecApiClient.class).toInstance(apiClient);
        bind(Credentials.class).toInstance(credentials);
        bind(BecStorage.class).toInstance(storage);
        bind(User.class).toInstance(user);
        bind(Catalog.class).toInstance(catalog);
        bind(SupplementalInformationController.class).toInstance(supplementalInformationController);
        bind(RandomValueGenerator.class).toInstance(randomValueGenerator);
    }

    public BecAuthenticator createAuthenticator() {
        Injector injector = Guice.createInjector(this);
        return injector.getInstance(BecAuthenticator.class);
    }
}
