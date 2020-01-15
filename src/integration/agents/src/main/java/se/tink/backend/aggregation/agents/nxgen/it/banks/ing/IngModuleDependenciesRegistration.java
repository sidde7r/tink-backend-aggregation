package se.tink.backend.aggregation.agents.nxgen.it.banks.ing;

import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.login.LoginProcess;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.CommonDataProvider;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.Login1ExternalApiCall;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.Login2ExternalApiCall;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.LoginStep;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.OtmlParser;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.PinKeyboardMapper;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.RegistrationProcess;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.scaffold.ModuleDependenciesRegistration;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class IngModuleDependenciesRegistration extends ModuleDependenciesRegistration {

    @Override
    public void registerInternalModuleDependencies() {
        registerBean(RandomDataProvider.class, randomDataProvider());
        registerBean(ConfigurationProvider.class, configurationProvider());
        registerBean(
                new Cryptor(
                        getBean(RandomDataProvider.class), getBean(ConfigurationProvider.class)));
        registerBean(
                new CommonDataProvider(
                        getBean(Cryptor.class), getBean(ConfigurationProvider.class)));
        registerBean(new PinKeyboardMapper());
        registerBean(new OtmlParser());
        registerBean(
                new Login1ExternalApiCall(
                        getBean(TinkHttpClient.class),
                        getBean(ConfigurationProvider.class),
                        getBean(CommonDataProvider.class),
                        getBean(PinKeyboardMapper.class),
                        getBean(OtmlParser.class)));
        registerBean(
                new Login2ExternalApiCall(
                        getBean(TinkHttpClient.class),
                        getBean(ConfigurationProvider.class),
                        getBean(CommonDataProvider.class)));
        registerBean(
                new LoginStep(
                        getBean(SessionStorage.class),
                        getBean(RandomDataProvider.class),
                        getBean(Login1ExternalApiCall.class),
                        getBean(Login2ExternalApiCall.class)));

        registerBean(new RegistrationProcess(getBean(LoginStep.class)));

        registerBean(new LoginProcess());
        registerBean(new IngAuthenticator());
    }

    protected RandomDataProvider randomDataProvider() {
        return new RandomDataProvider();
    }

    protected ConfigurationProvider configurationProvider() {
        return new ConfigurationProvider();
    }
}
