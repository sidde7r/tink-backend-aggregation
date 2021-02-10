package se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;

import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken.executor.payment.HandelsbankenSEPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Market;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.HandelsbankenBankIdAuthenticator;
import se.tink.backend.aggregation.agents.utils.transfer.InferredTransferDestinations;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.libraries.account.AccountIdentifier;

@AgentCapabilities({CHECKING_ACCOUNTS, CREDIT_CARDS})
public final class HandelsbankenSEAgent extends HandelsbankenBaseAgent
        implements RefreshTransferDestinationExecutor, RefreshCreditCardAccountsExecutor {

    private final HandelsbankenAccountConverter accountConverter;

    @Inject
    public HandelsbankenSEAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.accountConverter = new HandelsbankenAccountConverter();

        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        creditCardRefreshController = getCreditCardRefreshController();
    }

    @Override
    protected HandelsbankenBaseAccountConverter getAccountConverter() {
        return accountConverter;
    }

    @Override
    protected LocalDate getMaxPeriodTransactions() {
        return LocalDate.now().minusMonths(HandelsbankenSEConstants.MAX_FETCH_PERIOD_MONTHS);
    }

    @Override
    protected String getMarket() {
        return Market.SWEDEN;
    }

    @Override
    protected Authenticator constructAuthenticator() {
        BankIdAuthenticationController bankIdAuthenticationController =
                new BankIdAuthenticationController<>(
                        supplementalInformationController,
                        new HandelsbankenBankIdAuthenticator(apiClient, sessionStorage),
                        persistentStorage,
                        credentials);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                bankIdAuthenticationController,
                bankIdAuthenticationController);
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        final HandelsbankenSEPaymentExecutor paymentExecutor =
                new HandelsbankenSEPaymentExecutor(
                        apiClient, credentials, supplementalRequester, persistentStorage);
        return Optional.of(new PaymentController(paymentExecutor, paymentExecutor));
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return InferredTransferDestinations.forPaymentAccounts(
                accounts,
                AccountIdentifier.Type.SE,
                AccountIdentifier.Type.SE_PG,
                AccountIdentifier.Type.SE_BG);
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }
}
