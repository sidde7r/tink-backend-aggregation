package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb;

import java.util.Map;
import se.tink.backend.agents.rpc.Credentials;
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
import se.tink.libraries.i18n.Catalog;

public class DkbModuleDependenciesRegistration extends ModuleDependenciesRegistration {

    void registerExternalDependencies(
            TinkHttpClient tinkHttpClient,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage,
            Map<Class<?>, Object> beans) {
        registerExternalDependencies(tinkHttpClient, sessionStorage, persistentStorage);
        beans.forEach(this::registerBean);
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

        registerBean(
                new DkbSupplementalDataProvider(
                        getBean(SupplementalInformationHelper.class), getBean(Catalog.class)));

        registerBean(
                new DkbAuthenticator(
                        getBean(DkbAuthApiClient.class),
                        getBean(DkbSupplementalDataProvider.class),
                        getBean(DkbStorage.class),
                        getBean(Credentials.class)));
    }
}
