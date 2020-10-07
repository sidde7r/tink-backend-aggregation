package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb;

import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.DkbAuthApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.DkbAuthRequestsFactory;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.DkbAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.DkbSupplementalDataProvider;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.configuration.DkbConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.scaffold.ModuleDependenciesRegistration;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class DkbModuleDependenciesRegistration extends ModuleDependenciesRegistration {

    void registerExternalDependencies(
            TinkHttpClient tinkHttpClient,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage,
            DkbConfiguration configuration,
            SupplementalInformationHelper supplementalInformationHelper,
            DkbUserIpInformation dkbUserIpInformation) {
        registerExternalDependencies(tinkHttpClient, sessionStorage, persistentStorage);
        registerBean(DkbConfiguration.class, configuration);
        registerBean(SupplementalInformationHelper.class, supplementalInformationHelper);
        registerBean(DkbUserIpInformation.class, dkbUserIpInformation);
    }

    @Override
    public void registerInternalModuleDependencies() {

        registerBean(new DkbStorage(getBean(PersistentStorage.class)));

        registerBean(
                new DkbApiClient(
                        getBean(TinkHttpClient.class),
                        getBean(DkbStorage.class),
                        getBean(DkbUserIpInformation.class)));

        registerBean(
                new DkbAuthRequestsFactory(
                        getBean(DkbConfiguration.class),
                        getBean(DkbStorage.class),
                        getBean(DkbUserIpInformation.class)));

        registerBean(
                new DkbAuthApiClient(
                        getBean(TinkHttpClient.class),
                        getBean(DkbAuthRequestsFactory.class),
                        getBean(DkbStorage.class)));

        registerBean(new DkbSupplementalDataProvider(getBean(SupplementalInformationHelper.class)));

        registerBean(
                new DkbAuthenticator(
                        getBean(DkbAuthApiClient.class),
                        getBean(DkbSupplementalDataProvider.class),
                        getBean(DkbStorage.class)));
    }
}
