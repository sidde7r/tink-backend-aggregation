package se.tink.agent.agents.example;

import javax.inject.Inject;
import se.tink.agent.agents.example.authentication.ExampleOauth2Authenticator;
import se.tink.agent.agents.example.fetcher.ExampleCheckingAccountsFetcher;
import se.tink.agent.agents.example.fetcher.ExampleCheckingTransactionsFetcher;
import se.tink.agent.agents.example.fetcher.ExampleIdentityDataFetcher;
import se.tink.agent.agents.example.fetcher.ExampleSavingsAccountsFetcher;
import se.tink.agent.agents.example.fetcher.ExampleSavingsTransactionsFetcher;
import se.tink.agent.agents.example.payments.ExampleBulkPaymentInitiator;
import se.tink.agent.agents.example.payments.ExampleSinglePaymentInitiator;
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
import se.tink.agent.sdk.payments.beneficiary.BeneficiariesFetcher;
import se.tink.agent.sdk.payments.beneficiary.generic.GenericBeneficiaryRegistrator;
import se.tink.agent.sdk.payments.bulk.generic.GenericBulkPaymentInitiator;
import se.tink.agent.sdk.payments.features.beneficiary.FetchBeneficiaries;
import se.tink.agent.sdk.payments.features.beneficiary.RegisterBeneficiaryGeneric;
import se.tink.agent.sdk.payments.features.bulk.InitiateBulkPaymentGeneric;
import se.tink.agent.sdk.payments.features.global_signing_basket.DeleteUnsignedPayments;
import se.tink.agent.sdk.payments.features.single.InitiateSinglePaymentGeneric;
import se.tink.agent.sdk.payments.global_signing_basket.UnsignedPaymentsDeleter;
import se.tink.agent.sdk.payments.single.generic.GenericSinglePaymentInitiator;

public class ExampleAgent
        implements AuthenticateOauth2,
                FetchCheckingAccounts,
                FetchCheckingTransactions,
                FetchSavingsAccounts,
                FetchSavingsTransactions,
                FetchIdentityData,
                InitiateBulkPaymentGeneric,
                InitiateSinglePaymentGeneric,
                RegisterBeneficiaryGeneric,
                FetchBeneficiaries,
                DeleteUnsignedPayments {

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
    public GenericBulkPaymentInitiator bulkPaymentInitiator() {
        return new ExampleBulkPaymentInitiator();
    }

    @Override
    public GenericSinglePaymentInitiator singlePaymentInitiator() {
        return new ExampleSinglePaymentInitiator();
    }

    @Override
    public BeneficiariesFetcher beneficiariesFetcher() {
        return null;
    }

    @Override
    public GenericBeneficiaryRegistrator beneficiaryRegistrator() {
        return null;
    }

    @Override
    public UnsignedPaymentsDeleter unsignedPaymentsDeleter() {
        return null;
    }
}
