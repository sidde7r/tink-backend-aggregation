package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink;

import com.google.common.base.Strings;
import java.util.NoSuchElementException;
import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.authenticator.SamlinkAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.authenticator.SamlinkKeyCardAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.creditcard.SamlinkCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.loan.SamlinkLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.transactionalaccount.SamlinkTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.sessionhandler.SamlinkSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.identitydata.IdentityData;

public abstract class SamlinkAgent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor {

    private final SamlinkApiClient apiClient;
    private final SamlinkSessionStorage samlinkSessionStorage;
    private final SamlinkPersistentStorage samlinkPersistentStorage;

    public SamlinkAgent(
            CredentialsRequest request,
            AgentContext context,
            SignatureKeyPair signatureKeyPair,
            SamlinkConfiguration agentConfiguration) {
        super(request, context, signatureKeyPair);
        samlinkSessionStorage = new SamlinkSessionStorage(sessionStorage);
        apiClient = new SamlinkApiClient(client, samlinkSessionStorage, agentConfiguration);
        samlinkPersistentStorage = new SamlinkPersistentStorage(persistentStorage);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new KeyCardAuthenticationController(
                        catalog,
                        supplementalInformationHelper,
                        new SamlinkKeyCardAuthenticator(
                                apiClient, samlinkPersistentStorage, credentials)),
                new SamlinkAutoAuthenticator(apiClient, samlinkPersistentStorage, credentials));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        SamlinkTransactionalAccountFetcher transactionalAccountFetcher =
                new SamlinkTransactionalAccountFetcher(apiClient);

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        transactionalAccountFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionKeyPaginationController<>(
                                        transactionalAccountFetcher))));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        SamlinkCreditCardFetcher creditCardFetcher = new SamlinkCreditCardFetcher(apiClient);
        return Optional.of(
                new CreditCardRefreshController(
                        metricRefreshController,
                        updateController,
                        creditCardFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionKeyPaginationController<>(creditCardFetcher))));
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.of(
                new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        new SamlinkLoanFetcher(apiClient)));
    }

    @Override
    protected Optional<TransferDestinationRefreshController>
            constructTransferDestinationRefreshController() {
        return Optional.empty();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new SamlinkSessionHandler(apiClient, samlinkSessionStorage);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        final String loginName = samlinkSessionStorage.getLoginName();
        if (Strings.isNullOrEmpty(loginName)) {
            throw new NoSuchElementException("Did not get name from login.");
        }

        final IdentityData identityData =
                IdentityData.builder().setFullName(loginName).setDateOfBirth(null).build();

        return new FetchIdentityDataResponse(identityData);
    }
}
