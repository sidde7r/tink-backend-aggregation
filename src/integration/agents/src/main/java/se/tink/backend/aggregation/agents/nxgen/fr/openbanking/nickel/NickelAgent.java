package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.NickelConstants.SESION_EXPIRED_AFTER_DAYS;

import com.google.inject.Inject;
import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.util.Optional;
import lombok.SneakyThrows;
import se.tink.agent.sdk.utils.signer.qsealc.QsealcSigner;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.NickelConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.authenticator.NickelAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.authenticator.NickelAuthorizationFilter;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.authenticator.NickelSMSAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.configuration.NickelConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.fetcher.NickelIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.fetcher.NickelTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.utils.NickelErrorHandler;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.utils.NickelResponseHandler;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.utils.NickelStorage;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.libraries.cryptography.hash.Hash;

@AgentCapabilities({CHECKING_ACCOUNTS, IDENTITY_DATA})
public class NickelAgent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor, RefreshCheckingAccountsExecutor {

    private final NickelStorage storage;
    private final NickelApiClient apiClient;
    private final AgentConfiguration<NickelConfiguration> agentConfiguration;
    private final LocalDateTimeSource localDateTimeSource;
    private final NickelIdentityDataFetcher identityDataFetcher;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final NickelErrorHandler errorHandler;
    private final QsealcSigner qsealcSigner;

    @Inject
    protected NickelAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.storage = new NickelStorage(sessionStorage, persistentStorage);
        this.agentConfiguration =
                getAgentConfigurationController().getAgentConfiguration(NickelConfiguration.class);
        this.errorHandler = new NickelErrorHandler();
        this.apiClient = new NickelApiClient(client, storage);
        this.localDateTimeSource = componentProvider.getLocalDateTimeSource();
        this.qsealcSigner = componentProvider.getQsealcSigner();
        this.identityDataFetcher = new NickelIdentityDataFetcher(apiClient, errorHandler);
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        this.client.setResponseStatusHandler(new NickelResponseHandler());
        addFilters();
        checkSessionExpiryDate();
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return identityDataFetcher.response();
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return transactionalAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        NickelSMSAuthenticator authenticator =
                new NickelSMSAuthenticator(apiClient, errorHandler, storage);
        return new NickelAuthenticationController(
                catalog, supplementalInformationHelper, authenticator);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    private void addFilters() {
        client.addFilter(new TimeoutFilter());
        client.addFilter(createSigningFilter(agentConfiguration.getQsealc()));
    }

    @SneakyThrows
    private Filter createSigningFilter(String qSealc) {
        Optional<X509Certificate> certificate =
                CertificateUtils.getRootX509CertificateFromBase64EncodedString(qSealc);

        if (certificate.isPresent()) {
            return new NickelAuthorizationFilter(
                    CertificateUtils.getOrganizationIdentifier(qSealc),
                    userIp,
                    Hash.sha1AsHex(certificate.get().getEncoded()),
                    qsealcSigner,
                    localDateTimeSource);
        }
        throw new IllegalStateException(ErrorMessages.INVALID_QSEALC_CERTIFICATE);
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final NickelTransactionalAccountFetcher accountFetcher =
                new NickelTransactionalAccountFetcher(apiClient, errorHandler, localDateTimeSource);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(accountFetcher).build()));
    }

    private void checkSessionExpiryDate() {
        if (credentials.getSessionExpiryDate() == null
                || localDateTimeSource
                        .getInstant()
                        .isAfter(credentials.getSessionExpiryDate().toInstant())) {
            credentials.setSessionExpiryDate(
                    localDateTimeSource
                            .getInstant()
                            .atZone(ZoneId.of("CET"))
                            .toLocalDate()
                            .plusDays(SESION_EXPIRED_AFTER_DAYS));
        }
    }
}
