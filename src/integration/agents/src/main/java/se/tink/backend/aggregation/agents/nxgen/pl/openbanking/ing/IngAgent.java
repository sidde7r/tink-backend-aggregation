package se.tink.backend.aggregation.agents.nxgen.pl.openbanking.ing;

import static se.tink.backend.aggregation.agents.nxgen.pl.openbanking.ing.IngConstants.ACCOUNT_TYPE_MAPPER;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.PolishApiAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.concreteagents.PolishApiLogicFlowConfigurator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishAccountsApiUrlFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishAuthorizeApiUrlFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishPostAccountsApiUrlFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishPostAuthorizeApiUrlFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishPostTransactionsApiUrlFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishTransactionsApiUrlFactory;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS})
public class IngAgent extends PolishApiAgent {

    @Inject
    public IngAgent(AgentComponentProvider agentComponentProvider, QsealcSigner qsealcSigner) {
        super(agentComponentProvider, qsealcSigner);
    }

    @Override
    public PolishAccountsApiUrlFactory getAccountApiUrlFactory() {
        return new PolishPostAccountsApiUrlFactory(
                new URL(IngConstants.Urls.BASE_URL), IngConstants.Urls.VERSION);
    }

    @Override
    public PolishAuthorizeApiUrlFactory getAuthorizeApiUrlFactory() {
        return new PolishPostAuthorizeApiUrlFactory(
                new URL(IngConstants.Urls.BASE_URL), IngConstants.Urls.VERSION);
    }

    @Override
    public PolishTransactionsApiUrlFactory getTransactionsApiUrlFactory() {
        return new PolishPostTransactionsApiUrlFactory(
                new URL(IngConstants.Urls.BASE_URL), IngConstants.Urls.VERSION);
    }

    @Override
    public int getMaxDaysToFetch() {
        return 1460;
    }

    @Override
    public AccountTypeMapper getAccountTypeMapper() {
        return ACCOUNT_TYPE_MAPPER;
    }

    @Override
    public PolishApiLogicFlowConfigurator getLogicFlowConfigurator() {
        return PolishApiLogicFlowConfigurator.builder()
                .shouldSentSingleScopeLimitInAisAccounts(true)
                .shouldSentScopeAndScopeDetailsInFirstTokenRequest(false)
                .build();
    }
}
