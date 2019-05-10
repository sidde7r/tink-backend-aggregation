package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco;

import java.util.NoSuchElementException;
import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.EvoBancoAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.EvoBancoMultifactorAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entities.UserinfoEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.creditcard.EvoBancoCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.investments.EvoBancoInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.EvoBancoAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.EvoBancoTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.session.EvoBancoSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.smsotp.SmsOtpAuthenticationPasswordController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class EvoBancoAgent extends NextGenerationAgent implements RefreshIdentityDataExecutor {

    private final EvoBancoApiClient bankClient;

    public EvoBancoAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        bankClient = new EvoBancoApiClient(client, sessionStorage);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        SmsOtpAuthenticationPasswordController smsOtpAuthenticationController =
                new SmsOtpAuthenticationPasswordController<>(
                        catalog,
                        supplementalInformationHelper,
                        new EvoBancoMultifactorAuthenticator(
                                bankClient, persistentStorage, sessionStorage, credentials),
                        EvoBancoConstants.Constants.OTP_VALUE_LENGTH);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                smsOtpAuthenticationController,
                new EvoBancoAutoAuthenticator(
                        bankClient, credentials, persistentStorage, sessionStorage));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        EvoBancoAccountFetcher accountFetcher =
                new EvoBancoAccountFetcher(bankClient, sessionStorage);
        TransactionFetcherController<TransactionalAccount> transactionFetcher =
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new EvoBancoTransactionFetcher(bankClient, sessionStorage)));

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        accountFetcher,
                        transactionFetcher));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        EvoBancoCreditCardFetcher creditCardFetcher = new EvoBancoCreditCardFetcher(bankClient);
        return Optional.of(
                new CreditCardRefreshController(
                        metricRefreshController,
                        updateController,
                        creditCardFetcher,
                        new TransactionFetcherController<>(
                                this.transactionPaginationHelper,
                                new TransactionPagePaginationController<>(creditCardFetcher, 0))));
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.of(
                new InvestmentRefreshController(
                        metricRefreshController,
                        updateController,
                        new EvoBancoInvestmentFetcher(bankClient)));
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<TransferDestinationRefreshController>
            constructTransferDestinationRefreshController() {
        return Optional.empty();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new EvoBancoSessionHandler(bankClient, sessionStorage);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return sessionStorage
                .get(Storage.USER_INFO, UserinfoEntity.class)
                .map(UserinfoEntity::toTinkIdentity)
                .map(FetchIdentityDataResponse::new)
                .orElseThrow(NoSuchElementException::new);
    }
}
