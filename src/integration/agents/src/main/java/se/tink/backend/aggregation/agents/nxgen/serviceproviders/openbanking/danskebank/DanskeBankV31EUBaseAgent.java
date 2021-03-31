package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingV31Ais.defaultTransactionalAccountMapper;

import java.util.List;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.authenticator.DanskebankAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.configuration.DanskebankEUConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.mapper.DanskeCreditCardBalanceMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.OpenIdAisAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingClientConfigurationAdapter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards.CreditCardAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards.DefaultCreditCardBalanceMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.transactionalaccounts.TransactionalAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticationValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

public abstract class DanskeBankV31EUBaseAgent extends NextGenerationAgent
        implements RefreshTransferDestinationExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {

    private final UkOpenBankingAisConfig aisConfig;
    private final UkOpenBankingBaseAgentImpl ukOpenBankingBaseAgent;

    public DanskeBankV31EUBaseAgent(
            AgentComponentProvider componentProvider,
            JwtSigner jwtSigner,
            UkOpenBankingAisConfig aisConfig,
            CreditCardAccountMapper creditCardAccountMapper) {
        super(componentProvider);
        ukOpenBankingBaseAgent =
                new UkOpenBankingBaseAgentImpl(
                        componentProvider, jwtSigner, aisConfig, creditCardAccountMapper);
        this.aisConfig = aisConfig;
    }

    public DanskeBankV31EUBaseAgent(
            AgentComponentProvider componentProvider,
            JwtSigner jwtSigner,
            UkOpenBankingAisConfig aisConfig,
            CreditCardAccountMapper creditCardAccountMapper,
            TransactionalAccountMapper transactionalAccountMapper) {
        super(componentProvider);
        ukOpenBankingBaseAgent =
                new UkOpenBankingBaseAgentImpl(
                        componentProvider,
                        jwtSigner,
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

    protected static DanskeCreditCardBalanceMapper getCreditCardBalanceMapper(
            PrioritizedValueExtractor valueExtractor) {
        return new DanskeCreditCardBalanceMapper(
                new DefaultCreditCardBalanceMapper(valueExtractor));
    }

    private class UkOpenBankingBaseAgentImpl extends UkOpenBankingBaseAgent {
        private final CreditCardAccountMapper creditCardAccountMapper;
        private final TransactionalAccountMapper transactionalAccountMapper;

        UkOpenBankingBaseAgentImpl(
                AgentComponentProvider componentProvider,
                JwtSigner jwtSigner,
                UkOpenBankingAisConfig agentConfig,
                CreditCardAccountMapper creditCardAccountMapper) {
            super(componentProvider, jwtSigner, agentConfig);
            this.creditCardAccountMapper = creditCardAccountMapper;
            this.transactionalAccountMapper = defaultTransactionalAccountMapper();
        }

        UkOpenBankingBaseAgentImpl(
                AgentComponentProvider componentProvider,
                JwtSigner jwtSigner,
                UkOpenBankingAisConfig agentConfig,
                CreditCardAccountMapper creditCardAccountMapper,
                TransactionalAccountMapper transactionalAccountMapper) {
            super(componentProvider, jwtSigner, agentConfig);
            this.creditCardAccountMapper = creditCardAccountMapper;
            this.transactionalAccountMapper = transactionalAccountMapper;
        }

        @Override
        public Authenticator constructAuthenticator() {
            final OpenIdAuthenticationController openIdAuthenticationController =
                    new DanskebankAuthenticationController(
                            this.persistentStorage,
                            this.supplementalInformationHelper,
                            this.apiClient,
                            new OpenIdAisAuthenticator(this.apiClient),
                            this.credentials,
                            this.strongAuthenticationState,
                            this.request.getCallbackUri(),
                            this.randomValueGenerator,
                            new OpenIdAuthenticationValidator(this.apiClient));

            return new AutoAuthenticationController(
                    this.request,
                    this.systemUpdater,
                    new ThirdPartyAppAuthenticationController<>(
                            openIdAuthenticationController, this.supplementalInformationHelper),
                    openIdAuthenticationController);
        }

        protected Class<? extends UkOpenBankingClientConfigurationAdapter>
                getClientConfigurationFormat() {
            return DanskebankEUConfiguration.class;
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
