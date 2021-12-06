package se.tink.backend.aggregation.agents.nxgen.pl.openbanking.pekao;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.agents.nxgen.pl.openbanking.pekao.PekaoConstants.ACCOUNT_TYPE_MAPPER;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.util.List;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.PolishApiAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.concreteagents.PolishApiLogicFlowConfigurator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants;
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
public class PekaoAgent extends PolishApiAgent {

    @Inject
    public PekaoAgent(AgentComponentProvider agentComponentProvider, QsealcSigner qsealcSigner) {
        super(agentComponentProvider, qsealcSigner);
    }

    @Override
    public PolishAccountsApiUrlFactory getAccountApiUrlFactory() {
        return new PolishPostAccountsApiUrlFactory(
                new URL(PekaoConstants.Urls.BASE_URL), PekaoConstants.Urls.VERSION);
    }

    @Override
    public PolishAuthorizeApiUrlFactory getAuthorizeApiUrlFactory() {
        return new PolishPostAuthorizeApiUrlFactory(
                new URL(PekaoConstants.Urls.BASE_URL), PekaoConstants.Urls.VERSION);
    }

    @Override
    public PolishTransactionsApiUrlFactory getTransactionsApiUrlFactory() {
        return new PolishPostTransactionsApiUrlFactory(
                new URL(PekaoConstants.Urls.BASE_URL), PekaoConstants.Urls.VERSION);
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
    public List<PolishApiConstants.Transactions.TransactionTypeRequest>
            getSupportedTransactionTypes() {
        return ImmutableList.of(PolishApiConstants.Transactions.TransactionTypeRequest.DONE);
    }

    @Override
    public PolishApiLogicFlowConfigurator getLogicFlowConfigurator() {
        return PolishApiLogicFlowConfigurator.builder()
                .shouldSentSingleScopeLimitInAisAccounts(true)
                .shouldSentAuthorizationCodeInUpperCaseField(false)
                .shouldSentScopeAndScopeDetailsInFirstTokenRequest(false)
                .shouldSentScopeInRefreshTokenRequest(false)
                .shouldSentTokenInRefreshAndExchangeToken(false)
                .shouldGenerateNewConsentIdInExchangeToken(true)
                .build();
    }
}
