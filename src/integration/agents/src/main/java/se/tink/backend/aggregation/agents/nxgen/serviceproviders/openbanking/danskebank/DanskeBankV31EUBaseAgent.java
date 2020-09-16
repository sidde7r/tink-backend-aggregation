package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank;

import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration.UkOpenBankingClientConfigurationAdapter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

import java.util.List;

public abstract class DanskeBankV31EUBaseAgent extends NextGenerationAgent
        implements RefreshTransferDestinationExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshIdentityDataExecutor {

    private final UkOpenBankingAisConfig aisConfig;
    private final UkOpenBankingBaseAgentImpl ukOpenBankingBaseAgent;

    public DanskeBankV31EUBaseAgent(
            AgentComponentProvider componentProvider,
            AgentsServiceConfiguration configuration,
            UkOpenBankingAisConfig aisConfig) {
        super(componentProvider);
        ukOpenBankingBaseAgent =
                new UkOpenBankingBaseAgentImpl(componentProvider, configuration, aisConfig, true);
        this.aisConfig = aisConfig;
    }

    @Override
    public final void setConfiguration(AgentsServiceConfiguration configuration) {
        ukOpenBankingBaseAgent.setConfiguration(configuration);
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return ukOpenBankingBaseAgent.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return ukOpenBankingBaseAgent.fetchCheckingTransactions();
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return ukOpenBankingBaseAgent.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return ukOpenBankingBaseAgent.fetchCreditCardTransactions();
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return ukOpenBankingBaseAgent.fetchIdentityData();
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return ukOpenBankingBaseAgent.fetchSavingsAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return ukOpenBankingBaseAgent.fetchSavingsTransactions();
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return ukOpenBankingBaseAgent.fetchTransferDestinations(accounts);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return ukOpenBankingBaseAgent.constructAuthenticator();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    private class UkOpenBankingBaseAgentImpl extends UkOpenBankingBaseAgent {
        UkOpenBankingBaseAgentImpl(
                AgentComponentProvider componentProvider,
                AgentsServiceConfiguration configuration,
                UkOpenBankingAisConfig agentConfig,
                boolean disableSslVerification) {
            super(
                    componentProvider,
                    createEidasJwtSigner(
                            configuration,
                            componentProvider.getContext(),
                            UkOpenBankingBaseAgentImpl.class),
                    agentConfig,
                    disableSslVerification);
        }

        @Override
        public AgentConfiguration<? extends UkOpenBankingClientConfigurationAdapter>
                getAgentConfiguration() {
            return getAgentConfigurationController()
                    .getAgentConfiguration(DanskebankEUConfiguration.class);
        }

        @Override
        protected Authenticator constructAuthenticator() {
            return super.constructAuthenticator(aisConfig);
        }

        @Override
        protected UkOpenBankingAis makeAis() {
            return new DanskeBankOpenBankingV31Ais(
                    aisConfig, persistentStorage, localDateTimeSource);
        }
    }
}
