package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2;

import java.util.NoSuchElementException;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants.Authentication;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.SpankkiAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.SpankkiKeyCardAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.SpankkiSmsOtpAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.entities.CustomerEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.transactionalaccount.SpankkiTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.transactionalaccount.SpankkiTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.sessionhandler.SpankkiSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycardandsmsotp.KeyCardAndSmsOtpAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SpankkiAgent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {
    private final SpankkiApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public SpankkiAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        apiClient = new SpankkiApiClient(client, persistentStorage, sessionStorage);

        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final KeyCardAndSmsOtpAuthenticationController mfaCtrl =
                new KeyCardAndSmsOtpAuthenticationController(
                        catalog,
                        supplementalInformationHelper,
                        new SpankkiKeyCardAuthenticator(
                                apiClient, persistentStorage, sessionStorage),
                        Authentication.KEY_CARD_VALUE_LENGTH,
                        new SpankkiSmsOtpAuthenticator(
                                apiClient, persistentStorage, sessionStorage),
                        Authentication.SMS_OTP_VALUE_LENGTH);

        return new AutoAuthenticationController(
                request,
                context,
                mfaCtrl,
                new SpankkiAutoAuthenticator(
                        apiClient, credentials, persistentStorage, sessionStorage));
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
    public FetchAccountsResponse fetchSavingsAccounts() {
        return transactionalAccountRefreshController.fetchSavingsAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return transactionalAccountRefreshController.fetchSavingsTransactions();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new SpankkiSessionHandler(apiClient);
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        // There is also the endpoint "/v2/core/customer/profile" that can also fetch identity data
        return sessionStorage
                .get(Storage.CUSTOMER_ENTITY, CustomerEntity.class)
                .map(CustomerEntity::toTinkIdentity)
                .map(FetchIdentityDataResponse::new)
                .orElseThrow(NoSuchElementException::new);
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new SpankkiTransactionalAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(
                                new SpankkiTransactionFetcher(apiClient), 0)));
    }
}
