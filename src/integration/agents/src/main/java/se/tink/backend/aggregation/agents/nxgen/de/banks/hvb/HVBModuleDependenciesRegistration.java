package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb;

import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.AccessTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.AuthorizationRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.ConfigurationProvider;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.DataEncoder;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.HVBAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.PreAuthorizationRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.RegistrationRequest;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.scaffold.ModuleDependenciesRegistration;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class HVBModuleDependenciesRegistration extends ModuleDependenciesRegistration {

    @Override
    public void registerInternalModuleDependencies() {
        registerBean(DataEncoder.class, new DataEncoder());
        registerBean(ConfigurationProvider.class, configurationProvider());
        registerBean(
                new HVBStorage(getBean(SessionStorage.class), getBean(PersistentStorage.class)));
        registerBean(
                new RegistrationRequest(
                        getBean(TinkHttpClient.class),
                        getBean(ConfigurationProvider.class),
                        getBean(DataEncoder.class)));
        registerBean(
                new PreAuthorizationRequest(
                        getBean(TinkHttpClient.class), getBean(ConfigurationProvider.class)));
        registerBean(
                new AuthorizationRequest(
                        getBean(TinkHttpClient.class), getBean(ConfigurationProvider.class)));
        registerBean(
                new AccessTokenRequest(
                        getBean(TinkHttpClient.class),
                        getBean(ConfigurationProvider.class),
                        getBean(DataEncoder.class)));

        registerBean(
                new HVBAuthenticator(
                        getBean(HVBStorage.class),
                        getBean(ConfigurationProvider.class),
                        getBean(DataEncoder.class),
                        getBean(RegistrationRequest.class),
                        getBean(PreAuthorizationRequest.class),
                        getBean(AuthorizationRequest.class),
                        getBean(AccessTokenRequest.class)));
    }

    protected ConfigurationProvider configurationProvider() {
        return new ConfigurationProvider();
    }
}
