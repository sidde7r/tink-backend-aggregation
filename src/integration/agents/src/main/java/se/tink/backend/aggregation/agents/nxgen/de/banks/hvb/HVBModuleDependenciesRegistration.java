package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb;

import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.AccessTokenCall;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.AuthorizationCall;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.DataEncoder;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.HVBAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.PreAuthorizationCall;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.RegistrationCall;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.AccountsCall;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.AccountsFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.AccountsMapper;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.TransactionsCall;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.TransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.TransactionsMapper;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.UserDataCall;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.UserDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.UserDataMapper;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.scaffold.ModuleDependenciesRegistration;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class HVBModuleDependenciesRegistration extends ModuleDependenciesRegistration {

    void registerExternalDependencies(
            TinkHttpClient tinkHttpClient,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage,
            AgentComponentProvider componentProvider) {

        registerExternalDependencies(tinkHttpClient, sessionStorage, persistentStorage);
        registerBean(RandomValueGenerator.class, componentProvider.getRandomValueGenerator());
        registerBean(LocalDateTimeSource.class, componentProvider.getLocalDateTimeSource());
    }

    @Override
    public void registerInternalModuleDependencies() {
        registerBean(DataEncoder.class, new DataEncoder());
        registerBean(new ConfigurationProvider(getBean(RandomValueGenerator.class)));
        registerBean(
                new HVBStorage(getBean(SessionStorage.class), getBean(PersistentStorage.class)));
        registerBean(
                new RegistrationCall(
                        getBean(TinkHttpClient.class),
                        getBean(ConfigurationProvider.class),
                        getBean(DataEncoder.class)));
        registerBean(
                new PreAuthorizationCall(
                        getBean(TinkHttpClient.class), getBean(ConfigurationProvider.class)));
        registerBean(
                new AuthorizationCall(
                        getBean(TinkHttpClient.class), getBean(ConfigurationProvider.class)));
        registerBean(
                new AccessTokenCall(
                        getBean(TinkHttpClient.class),
                        getBean(ConfigurationProvider.class),
                        getBean(DataEncoder.class)));

        registerBean(
                new HVBAuthenticator(
                        getBean(HVBStorage.class),
                        getBean(ConfigurationProvider.class),
                        getBean(DataEncoder.class),
                        getBean(LocalDateTimeSource.class),
                        getBean(RegistrationCall.class),
                        getBean(PreAuthorizationCall.class),
                        getBean(AuthorizationCall.class),
                        getBean(AccessTokenCall.class)));
        registerBean(
                new UserDataCall(
                        getBean(TinkHttpClient.class), getBean(ConfigurationProvider.class)));

        registerBean(
                new AccountsCall(
                        getBean(TinkHttpClient.class),
                        getBean(ConfigurationProvider.class),
                        getBean(HVBStorage.class)));

        registerBean(new AccountsMapper());

        registerBean(
                new AccountsFetcher(
                        getBean(HVBStorage.class),
                        getBean(AccountsCall.class),
                        getBean(AccountsMapper.class)));

        registerBean(
                new TransactionsCall(
                        getBean(TinkHttpClient.class),
                        getBean(ConfigurationProvider.class),
                        getBean(HVBStorage.class)));

        registerBean(new TransactionsMapper());

        registerBean(
                new TransactionsFetcher(
                        getBean(HVBStorage.class),
                        getBean(ConfigurationProvider.class),
                        getBean(LocalDateTimeSource.class),
                        getBean(TransactionsCall.class),
                        getBean(TransactionsMapper.class)));

        registerBean(new UserDataMapper());

        registerBean(
                new UserDataFetcher(
                        getBean(HVBStorage.class),
                        getBean(UserDataCall.class),
                        getBean(UserDataMapper.class)));
    }
}
