package se.tink.backend.aggregation.agents.creditcards.ikano.api;

import com.google.inject.Inject;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.PersistentLogin;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.IkanoApiConstants.Error;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.errors.UserErrorException;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.cards.Card;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.cards.CardList;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.session.CredentialsPersistence;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.constants.CommonHeaders;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.i18n.LocalizableKey;

@AgentCapabilities(generateFromImplementedExecutors = true)
public final class IkanoApiAgent extends AbstractAgent
        implements RefreshCreditCardAccountsExecutor, RefreshIdentityDataExecutor, PersistentLogin {
    private final IkanoApiClient apiClient;
    private final Credentials credentials;
    private static final int MAX_BANK_ID_POLLING_ATTEMPTS = 60;
    private final int bankIdPollIntervalMS;
    private final CredentialsPersistence credentialsPersistence;
    private final PersistentStorage persistentStorage;
    private List<Account> accounts;

    @Inject
    public IkanoApiAgent(AgentComponentProvider agentComponentProvider)
            throws NoSuchAlgorithmException {
        super(agentComponentProvider.getCredentialsRequest(), agentComponentProvider.getContext());

        TinkHttpClient client = agentComponentProvider.getTinkHttpClient();
        bankIdPollIntervalMS = 2000;
        credentials = request.getCredentials();

        apiClient =
                new IkanoApiClient(
                        client,
                        credentials,
                        request.getProvider().getPayload(),
                        CommonHeaders.DEFAULT_USER_AGENT);
        this.persistentStorage = new PersistentStorage();
        this.credentialsPersistence =
                new CredentialsPersistence(
                        persistentStorage, new SessionStorage(), this.credentials, client);
        this.credentialsPersistence.load();
    }

    private boolean isAuthenticated() {
        try {
            getAccounts();
        } catch (IllegalStateException ex) {
            persistentStorage.clear();
            return false;
        }
        return true;
    }

    private List<Account> getAccounts() {
        if (accounts != null) {
            return accounts;
        }

        try {
            accounts = apiClient.fetchAccounts();
            return accounts;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean keepAlive() throws Exception {
        return isAuthenticated();
    }

    @Override
    public void persistLoginSession() {
        credentialsPersistence.store();
    }

    @Override
    public void loadLoginSession() {
        this.credentialsPersistence.load();
    }

    @Override
    public void clearLoginSession() {
        // NOP
    }

    @Override
    public boolean isLoggedIn() throws Exception {
        return keepAlive();
    }

    @Override
    public boolean login() throws Exception {

        try {
            String reference = apiClient.authenticateWithBankId();
            openBankIdApp();

            pollBankIdSession(reference);

            credentialsPersistence.store();
            CardList cards = apiClient.fetchCards();
            List<Card> unregisteredCards = cards.getUnregisteredCards();

            if (unregisteredCards.size() > 0) {
                apiClient.registerCards(unregisteredCards);
                cards = apiClient.fetchCards();
            }

            cards.ensureRegisteredCardExists();

            // Fetch and cache accounts, we do this here because we want to throw a NOT_CUSTOMER
            // login exception if the user has no valid cards.
            accounts = apiClient.fetchAccounts();

            return true;
        } catch (UserErrorException e) {
            stopLoginAttempt(e);
        }

        return false;
    }

    void pollBankIdSession(String reference) throws Exception {

        for (int i = 0; i < MAX_BANK_ID_POLLING_ATTEMPTS; i++) {
            boolean sessionReceived = apiClient.fetchBankIdSession(reference);

            if (sessionReceived) {
                return;
            }

            Thread.sleep(bankIdPollIntervalMS);
        }

        throw BankIdError.TIMEOUT.exception();
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return new FetchAccountsResponse(accounts);
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        Map<Account, List<Transaction>> response = new HashMap<>();

        try {
            for (Account account : accounts) {
                List<Transaction> transactions = apiClient.getTransactionsFor(account);

                boolean stop = false;

                while (apiClient.hasMoreTransactionHistory(account)) {
                    if (isContentWithRefresh(account, transactions)) {
                        /*
                         * Since Ikano transactions can be unsettled for longer periods of time (3-4
                         * weeks), we need to fetch more transactions than usual in order to avoid
                         * duplicates.
                         */
                        stop = true;
                    }

                    transactions = apiClient.fetchMoreTransactionsFor(account);

                    if (stop) {
                        break;
                    }
                }

                response.put(account, transactions);
            }
        } catch (LoginException | AccountRelationNotFoundException e) {
            log.error("No relation between account and card found, this SHOULD never happen", e);
        }

        return new FetchTransactionsResponse(response);
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return new IkanoIdentityFetcher(apiClient, credentials).fetchIdentityData();
    }

    public void logout() {}

    private void openBankIdApp() {
        supplementalInformationController.openMobileBankIdAsync(null);
    }

    private void stopLoginAttempt(UserErrorException e) throws LoginException {
        String message = e.getMessage();
        if (IkanoApiConstants.Error.TECHNICAL_ISSUES.equalsIgnoreCase(message)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(
                    new LocalizableKey("Error message: " + message));
        } else if (Error.WRONG_SSN.equalsIgnoreCase(message)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        log.info("Ikano agent user error: " + message, e);
        throw new IllegalStateException("Ikano agent user error: " + message);
    }

    public static class AccountRelationNotFoundException extends Exception {}
}
