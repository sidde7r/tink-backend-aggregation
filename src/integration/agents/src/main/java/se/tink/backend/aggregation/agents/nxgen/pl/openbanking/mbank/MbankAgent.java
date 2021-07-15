package se.tink.backend.aggregation.agents.nxgen.pl.openbanking.mbank;

import static se.tink.backend.aggregation.agents.nxgen.pl.openbanking.mbank.MbankConstants.ACCOUNT_TYPE_MAPPER;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.PolishApiAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishAccountsApiUrlFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishAuthorizeApiUrlFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishGetAccountsApiUrlFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishGetAuthorizeApiUrlFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishGetTransactionsApiUrlFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishTransactionsApiUrlFactory;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
@AgentCapabilities({CHECKING_ACCOUNTS, CREDIT_CARDS})
public class MbankAgent extends PolishApiAgent {

    @Inject
    public MbankAgent(AgentComponentProvider agentComponentProvider, QsealcSigner qsealcSigner) {
        super(agentComponentProvider, qsealcSigner);
    }

    @Override
    public PolishApiConfiguration getApiConfiguration() {
        return new PolishApiConfiguration();
    }

    @Override
    public PolishAccountsApiUrlFactory getAccountApiUrlFactory() {
        return new PolishGetAccountsApiUrlFactory(
                new URL(MbankConstants.Urls.BASE_URL),
                MbankConstants.Urls.API_TYPE,
                MbankConstants.Urls.VERSION);
    }

    @Override
    public PolishAuthorizeApiUrlFactory getAuthorizeApiUrlFactory() {
        return new PolishGetAuthorizeApiUrlFactory(
                new URL(MbankConstants.Urls.BASE_URL),
                MbankConstants.Urls.API_TYPE,
                MbankConstants.Urls.VERSION);
    }

    @Override
    public PolishTransactionsApiUrlFactory getTransactionsApiUrlFactory() {
        return new PolishGetTransactionsApiUrlFactory(
                new URL(MbankConstants.Urls.BASE_URL),
                MbankConstants.Urls.API_TYPE,
                MbankConstants.Urls.VERSION);
    }

    @Override
    public int getMaxDaysToFetch() {
        return 1460;
    }

    @Override
    public boolean shouldAttachHeadersAndUriInJws() {
        return true;
    }

    @Override
    public boolean shouldGetAccountListFromTokenResponse() {
        return true;
    }

    @Override
    public boolean doesSupportExchangeToken() {
        return true;
    }

    @Override
    public AccountTypeMapper getAccountTypeMapper() {
        return ACCOUNT_TYPE_MAPPER;
    }
}
