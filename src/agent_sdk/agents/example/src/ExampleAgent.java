package se.tink.agent.agents.example;

import se.tink.agent.agents.example.authentication.ExampleOauth2Authenticator;
import se.tink.agent.agents.example.fetcher.ExampleCheckingAccountsFetcher;
import se.tink.agent.agents.example.fetcher.ExampleCheckingTransactionsFetcher;
import se.tink.agent.agents.example.fetcher.ExampleIdentityDataFetcher;
import se.tink.agent.agents.example.fetcher.ExampleSavingsAccountsFetcher;
import se.tink.agent.agents.example.fetcher.ExampleSavingsTransactionsFetcher;
import se.tink.agent.sdk.annotations.Agent;
import se.tink.agent.sdk.authentication.authenticators.oauth2.Oauth2Authenticator;
import se.tink.agent.sdk.authentication.capability.AuthenticateOauth2;
import se.tink.agent.sdk.fetching.accounts.CheckingAccountsFetcher;
import se.tink.agent.sdk.fetching.accounts.SavingsAccountsFetcher;
import se.tink.agent.sdk.fetching.capability.FetchCheckingAccounts;
import se.tink.agent.sdk.fetching.capability.FetchCheckingTransactions;
import se.tink.agent.sdk.fetching.capability.FetchIdentityData;
import se.tink.agent.sdk.fetching.capability.FetchSavingsAccounts;
import se.tink.agent.sdk.fetching.capability.FetchSavingsTransactions;
import se.tink.agent.sdk.fetching.identity_data.IdentityDataFetcher;
import se.tink.agent.sdk.fetching.transactions.TransactionsFetcher;

@Agent
public class ExampleAgent
        implements AuthenticateOauth2,
                FetchCheckingAccounts,
                FetchCheckingTransactions,
                FetchSavingsAccounts,
                FetchSavingsTransactions,
                FetchIdentityData {

    public ExampleAgent(String arg) {
        System.out.println("Hello from Example agent: " + arg);
    }

    @Override
    public Oauth2Authenticator oauth2Authenticator() {
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
}
