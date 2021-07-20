package se.tink.backend.aggregation.agents.nxgen.pl.openbanking.pko;

import static se.tink.backend.aggregation.agents.nxgen.pl.openbanking.pko.PkoConstants.ACCOUNT_TYPE_MAPPER;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.PolishApiAgent;
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
public class PkoAgent extends PolishApiAgent {

    @Inject
    public PkoAgent(AgentComponentProvider agentComponentProvider, QsealcSigner qsealcSigner) {
        super(agentComponentProvider, qsealcSigner);
    }

    @Override
    public PolishAccountsApiUrlFactory getAccountApiUrlFactory() {
        return new PolishPostAccountsApiUrlFactory(
                new URL(PkoConstants.Urls.BASE_URL), PkoConstants.Urls.VERSION);
    }

    @Override
    public PolishAuthorizeApiUrlFactory getAuthorizeApiUrlFactory() {
        return new PolishPostAuthorizeApiUrlFactory(
                new URL(PkoConstants.Urls.BASE_URL), PkoConstants.Urls.VERSION);
    }

    @Override
    public PolishTransactionsApiUrlFactory getTransactionsApiUrlFactory() {
        return new PolishPostTransactionsApiUrlFactory(
                new URL(PkoConstants.Urls.BASE_URL), PkoConstants.Urls.VERSION);
    }

    @Override
    public int getMaxDaysToFetch() {
        return 365;
    }

    @Override
    public boolean shouldGetAccountListFromTokenResponse() {
        return false;
    }

    @Override
    public boolean doesSupportExchangeToken() {
        return false;
    }

    @Override
    public AccountTypeMapper getAccountTypeMapper() {
        return ACCOUNT_TYPE_MAPPER;
    }
}
