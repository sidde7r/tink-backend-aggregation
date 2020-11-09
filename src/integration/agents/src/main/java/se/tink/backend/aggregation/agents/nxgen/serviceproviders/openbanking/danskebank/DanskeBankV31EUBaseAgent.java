package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Ais.defaultCreditCardAccountMapper;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Ais.defaultTransactionalAccountMapper;

import java.util.List;
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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.creditcards.CreditCardAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.transactionalaccounts.TransactionalAccountMapper;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

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
                new UkOpenBankingBaseAgentImpl(componentProvider, configuration, aisConfig);
        this.aisConfig = aisConfig;
    }

    public DanskeBankV31EUBaseAgent(
            AgentComponentProvider componentProvider,
            AgentsServiceConfiguration configuration,
            UkOpenBankingAisConfig aisConfig,
            CreditCardAccountMapper creditCardAccountMapper,
            TransactionalAccountMapper transactionalAccountMapper) {
        super(componentProvider);
        ukOpenBankingBaseAgent =
                new UkOpenBankingBaseAgentImpl(
                        componentProvider,
                        configuration,
                        aisConfig,
                        creditCardAccountMapper,
                        transactionalAccountMapper);
        this.aisConfig = aisConfig;
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return ukOpenBankingBaseAgent.constructAuthenticator();
    }

    @Override
    public final void setConfiguration(AgentsServiceConfiguration configuration) {
        ukOpenBankingBaseAgent.setConfiguration(configuration);
        // fixme make sure that cert is verified - add root ca to eidas proxy
        client.disableSslVerification();
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
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public void persistLoginSession() {
        ukOpenBankingBaseAgent.persistLoginSession();
    }

    @Override
    public void loadLoginSession() {
        ukOpenBankingBaseAgent.loadLoginSession();
    }

    @Override
    public void clearLoginSession() {
        ukOpenBankingBaseAgent.clearLoginSession();
    }

    private class UkOpenBankingBaseAgentImpl extends UkOpenBankingBaseAgent {
        private final CreditCardAccountMapper creditCardAccountMapper;
        private final TransactionalAccountMapper transactionalAccountMapper;

        UkOpenBankingBaseAgentImpl(
                AgentComponentProvider componentProvider,
                AgentsServiceConfiguration configuration,
                UkOpenBankingAisConfig agentConfig) {
            super(
                    componentProvider,
                    createEidasJwtSigner(
                            configuration,
                            componentProvider.getContext(),
                            UkOpenBankingBaseAgentImpl.class),
                    agentConfig);
            creditCardAccountMapper = defaultCreditCardAccountMapper();
            transactionalAccountMapper = defaultTransactionalAccountMapper();
        }

        UkOpenBankingBaseAgentImpl(
                AgentComponentProvider componentProvider,
                AgentsServiceConfiguration configuration,
                UkOpenBankingAisConfig agentConfig,
                CreditCardAccountMapper creditCardAccountMapper,
                TransactionalAccountMapper transactionalAccountMapper) {
            super(
                    componentProvider,
                    createEidasJwtSigner(
                            configuration,
                            componentProvider.getContext(),
                            UkOpenBankingBaseAgentImpl.class),
                    agentConfig);
            this.creditCardAccountMapper = creditCardAccountMapper;
            this.transactionalAccountMapper = transactionalAccountMapper;
        }

        @Override
        public AgentConfiguration<? extends UkOpenBankingClientConfigurationAdapter>
                getAgentConfiguration() {
            return getAgentConfigurationController()
                    .getAgentConfiguration(DanskebankEUConfiguration.class);
        }

        @Override
        protected UkOpenBankingAis makeAis() {
            return new DanskeBankOpenBankingV31Ais(
                    aisConfig,
                    persistentStorage,
                    localDateTimeSource,
                    creditCardAccountMapper,
                    transactionalAccountMapper);
        }
    }
}
