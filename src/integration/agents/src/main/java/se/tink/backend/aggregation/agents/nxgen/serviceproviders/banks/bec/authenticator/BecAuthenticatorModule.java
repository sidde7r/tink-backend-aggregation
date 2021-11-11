package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecStorage;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.credentials.service.UserAvailability;
import se.tink.libraries.i18n.Catalog;

@RequiredArgsConstructor
public class BecAuthenticatorModule extends AbstractModule {

    private final BecApiClient apiClient;
    private final Credentials credentials;
    private final BecStorage storage;
    private final UserAvailability userAvailability;
    private final Catalog catalog;
    private final SupplementalInformationController supplementalInformationController;

    @Override
    public void configure() {
        bind(BecApiClient.class).toInstance(apiClient);
        bind(Credentials.class).toInstance(credentials);
        bind(BecStorage.class).toInstance(storage);
        bind(UserAvailability.class).toInstance(userAvailability);
        bind(Catalog.class).toInstance(catalog);
        bind(SupplementalInformationController.class).toInstance(supplementalInformationController);
    }

    public BecAuthenticator createAuthenticator() {
        Injector injector = Guice.createInjector(this);
        return injector.getInstance(BecAuthenticator.class);
    }
}
