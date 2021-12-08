package se.tink.agent.agents.example;

import java.util.Optional;
import javax.inject.Inject;
import se.tink.agent.agents.example.authentication.ExampleOauth2Authenticator;
import se.tink.agent.agents.example.fetcher.ExampleCheckingAccountsFetcher;
import se.tink.agent.agents.example.fetcher.ExampleCheckingTransactionsFetcher;
import se.tink.agent.agents.example.fetcher.ExampleIdentityDataFetcher;
import se.tink.agent.agents.example.fetcher.ExampleSavingsAccountsFetcher;
import se.tink.agent.agents.example.fetcher.ExampleSavingsTransactionsFetcher;
import se.tink.agent.agents.example.payments.ExampleBankTransferExecutor;
import se.tink.agent.agents.example.payments.ExampleCreateBeneficiaryExecutor;
import se.tink.agent.agents.example.payments.ExampleGPIPaymentExecutor;
import se.tink.agent.agents.example.payments.ExamplePaymentExecutor;
import se.tink.agent.sdk.authentication.authenticators.oauth2.Oauth2Authenticator;
import se.tink.agent.sdk.authentication.features.AuthenticateOauth2;
import se.tink.agent.sdk.environment.Operation;
import se.tink.agent.sdk.environment.Utilities;
import se.tink.agent.sdk.fetching.accounts.CheckingAccountsFetcher;
import se.tink.agent.sdk.fetching.accounts.SavingsAccountsFetcher;
import se.tink.agent.sdk.fetching.features.FetchCheckingAccounts;
import se.tink.agent.sdk.fetching.features.FetchCheckingTransactions;
import se.tink.agent.sdk.fetching.features.FetchIdentityData;
import se.tink.agent.sdk.fetching.features.FetchSavingsAccounts;
import se.tink.agent.sdk.fetching.features.FetchSavingsTransactions;
import se.tink.agent.sdk.fetching.identity_data.IdentityDataFetcher;
import se.tink.agent.sdk.fetching.transactions.TransactionsFetcher;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.payments.CreateBeneficiaryControllerable;
import se.tink.backend.aggregation.agents.payments.PaymentControllerable;
import se.tink.backend.aggregation.agents.payments.TransferExecutorNxgen;
import se.tink.backend.aggregation.agents.payments.TypedPaymentControllerable;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.rpc.Transfer;

public class ExampleAgent
        implements AuthenticateOauth2,
                FetchCheckingAccounts,
                FetchCheckingTransactions,
                FetchSavingsAccounts,
                FetchSavingsTransactions,
                FetchIdentityData,
                CreateBeneficiaryControllerable,
                PaymentControllerable,
                TransferExecutorNxgen,
                TypedPaymentControllerable {

    private final Utilities utilities;
    private final Operation operation;

    @Inject
    public ExampleAgent(Utilities utilities, Operation operation) {
        this.utilities = utilities;
        this.operation = operation;
    }

    @Override
    public Oauth2Authenticator authenticator() {
        return new ExampleOauth2Authenticator();
    }

    @Override
    public CheckingAccountsFetcher checkingAccountsFetcher() {
        return new ExampleCheckingAccountsFetcher();
    }

    @Override
    public TransactionsFetcher checkingTransactionsFetcher() {
        return new ExampleCheckingTransactionsFetcher();
    }

    @Override
    public SavingsAccountsFetcher savingsAccountsFetcher() {
        return new ExampleSavingsAccountsFetcher();
    }

    @Override
    public TransactionsFetcher savingsTransactionsFetcher() {
        return new ExampleSavingsTransactionsFetcher();
    }

    @Override
    public IdentityDataFetcher identityDataFetcher() {
        return new ExampleIdentityDataFetcher();
    }

    @Override
    public Optional<CreateBeneficiaryController> getCreateBeneficiaryController() {
        return Optional.of(new CreateBeneficiaryController(new ExampleCreateBeneficiaryExecutor()));
    }

    @Override
    public Optional<PaymentController> getPaymentController() {
        ExampleGPIPaymentExecutor examplePaymentExecutor = new ExampleGPIPaymentExecutor();
        return Optional.of(new PaymentController(examplePaymentExecutor, examplePaymentExecutor));
    }

    @Override
    public Optional<PaymentController> getPaymentController(Payment payment)
            throws PaymentRejectedException {
        ExampleGPIPaymentExecutor examplePaymentExecutor = new ExampleGPIPaymentExecutor();
        return Optional.of(new PaymentController(examplePaymentExecutor, examplePaymentExecutor));
    }

    @Override
    public Optional<String> execute(Transfer transfer) {
        return new TransferController(
                        new ExamplePaymentExecutor(), new ExampleBankTransferExecutor())
                .execute(transfer);
    }
}
