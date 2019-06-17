package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp;

import java.util.NoSuchElementException;
import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.OmaspConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.authentication.OmaspAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.authentication.OmaspKeyCardAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.creditcard.OmaspCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.loan.OmaspLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.transactionalaccount.OmaspTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.sessionhandler.OmaspSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.identitydata.IdentityData;

public class OmaspAgent extends NextGenerationAgent implements RefreshIdentityDataExecutor {
    private final OmaspApiClient apiClient;

    public OmaspAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        configureHttpClient(client);

        apiClient = new OmaspApiClient(client, sessionStorage);
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.setUserAgent(OmaspConstants.USER_AGENT);
        client.addFilter(new OmaspAccessTokenFilter(sessionStorage));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new KeyCardAuthenticationController(
                        catalog,
                        supplementalInformationHelper,
                        new OmaspKeyCardAuthenticator(
                                apiClient, persistentStorage, sessionStorage)),
                new OmaspAutoAuthenticator(
                        apiClient, persistentStorage, credentials, sessionStorage));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        OmaspTransactionalAccountFetcher omaspTransactionalAccountFetcher =
                new OmaspTransactionalAccountFetcher(apiClient, credentials);
        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        omaspTransactionalAccountFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionPagePaginationController<>(
                                        omaspTransactionalAccountFetcher, 0))));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        OmaspCreditCardFetcher omaspCreditCardFetcher = new OmaspCreditCardFetcher(apiClient);
        return Optional.of(
                new CreditCardRefreshController(
                        metricRefreshController,
                        updateController,
                        omaspCreditCardFetcher,
                        omaspCreditCardFetcher));
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.of(
                new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        new OmaspLoanFetcher(apiClient)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new OmaspSessionHandler();
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return sessionStorage
                .get(Storage.FULL_NAME, String.class)
                .map(name -> IdentityData.builder().setFullName(name).setDateOfBirth(null).build())
                .map(FetchIdentityDataResponse::new)
                .orElseThrow(NoSuchElementException::new);
    }
}
