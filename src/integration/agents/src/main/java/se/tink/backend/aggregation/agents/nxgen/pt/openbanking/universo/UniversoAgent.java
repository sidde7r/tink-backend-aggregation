package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.universo;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import lombok.SneakyThrows;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersTransactionalAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.Xs2aDevelopersAuthenticatorHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.Xs2aDevelopersTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionKeyWithInitDateFromFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
@AgentCapabilities({CHECKING_ACCOUNTS})
public class UniversoAgent extends Xs2aDevelopersTransactionalAgent {

    @Inject
    public UniversoAgent(AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {
        super(componentProvider, "https://api.psd2.universo.pt");
        client.addFilter(
                new UniversoSigningFilter(
                        (UniversoProviderConfiguration) configuration, qsealcSigner));
    }

    @Override
    @SneakyThrows
    protected UniversoProviderConfiguration getConfiguration(String baseUrl) {
        AgentConfiguration<UniversoConfiguration> agentConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(UniversoConfiguration.class);
        String organizationIdentifier =
                CertificateUtils.getOrganizationIdentifier(agentConfiguration.getQwac());
        String redirectUrl = agentConfiguration.getRedirectUrl();
        UniversoConfiguration universoConfiguration =
                agentConfiguration.getProviderSpecificConfiguration();

        return new UniversoProviderConfiguration(
                organizationIdentifier,
                baseUrl,
                redirectUrl,
                universoConfiguration.getApiKey(),
                universoConfiguration.getKeyId(),
                agentConfiguration.getQsealc());
    }

    @Override
    protected Xs2aDevelopersAuthenticatorHelper constructXs2aAuthenticator(
            AgentComponentProvider componentProvider) {
        return new UniversoAuthenticator(
                apiClient,
                persistentStorage,
                configuration,
                componentProvider.getLocalDateTimeSource(),
                credentials);
    }

    @Override
    protected Xs2aDevelopersApiClient constructApiClient(AgentComponentProvider componentProvider) {
        return new UniversoApiClient(
                componentProvider.getTinkHttpClient(),
                persistentStorage,
                configuration,
                request.isManual(),
                userIp,
                componentProvider.getRandomValueGenerator());
    }

    @Override
    protected TransactionalAccountRefreshController constructTransactionalAccountRefreshController(
            AgentComponentProvider agentComponentProvider) {
        final Xs2aDevelopersTransactionalAccountFetcher accountFetcher =
                new Xs2aDevelopersTransactionalAccountFetcher(apiClient, authenticatorHelper);

        final TransactionFetcher<TransactionalAccount> transactionFetcher =
                new TransactionKeyWithInitDateFromFetcherController<>(
                        request,
                        new UniversoTransactionDateFromFetcher(
                                apiClient,
                                agentComponentProvider.getLocalDateTimeSource(),
                                request.isManual()));

        return new TransactionalAccountRefreshController(
                metricRefreshController, updateController, accountFetcher, transactionFetcher);
    }
}
