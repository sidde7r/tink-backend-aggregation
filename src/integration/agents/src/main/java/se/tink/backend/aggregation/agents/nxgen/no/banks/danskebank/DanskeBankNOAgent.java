package se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.MORTGAGE_AGGREGATION;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import java.util.Collections;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank.authenticator.DanskeBankNOAuthInitializer;
import se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank.authenticator.DanskeBankNOAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank.authenticator.DanskeBankNOManualAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.mapper.AccountEntityMarketMapper;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.integration.webdriver.WebDriverHelper;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS, LOANS, MORTGAGE_AGGREGATION})
public final class DanskeBankNOAgent extends DanskeBankAgent<DanskeBankNOApiClient> {
    @Inject
    public DanskeBankNOAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, new AccountEntityMarketMapper("NO"));
    }

    @Override
    protected DanskeBankConfiguration createConfiguration() {
        return new DanskeBankNOConfiguration();
    }

    @Override
    protected DanskeBankNOApiClient createApiClient(
            TinkHttpClient client, DanskeBankConfiguration configuration) {
        return new DanskeBankNOApiClient(
                client, (DanskeBankNOConfiguration) configuration, credentials, catalog);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        DanskeBankNOAuthInitializer authInitializer =
                new DanskeBankNOAuthInitializer(
                        apiClient, deviceId, configuration, new WebDriverHelper());
        DanskeBankNOManualAuthenticator manualAuthenticator =
                new DanskeBankNOManualAuthenticator(
                        apiClient,
                        persistentStorage,
                        new WebDriverHelper(),
                        supplementalInformationController,
                        catalog,
                        authInitializer);
        DanskeBankNOAutoAuthenticator autoAuthenticator =
                new DanskeBankNOAutoAuthenticator(
                        apiClient,
                        persistentStorage,
                        credentials,
                        new WebDriverHelper(),
                        authInitializer);
        return new AutoAuthenticationController(
                request, systemUpdater, manualAuthenticator, autoAuthenticator);
    }

    //    Investments are temporarly disabled for Norwegian Agents ITE-1676,
    @Override
    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        return new FetchInvestmentAccountsResponse(Collections.emptyMap());
    }

    @Override
    public FetchTransactionsResponse fetchInvestmentTransactions() {
        return new FetchTransactionsResponse(Collections.emptyMap());
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
