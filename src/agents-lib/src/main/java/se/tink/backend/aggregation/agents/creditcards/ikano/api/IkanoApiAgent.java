package se.tink.backend.aggregation.agents.creditcards.ikano.api;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.naming.LimitExceededException;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.DeprecatedRefreshExecutor;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.errors.UserErrorException;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.cards.Card;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.cards.CardList;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.agents.models.Transaction;

public class IkanoApiAgent extends AbstractAgent implements DeprecatedRefreshExecutor {
    private final IkanoApiClient apiClient;
    private final Credentials credentials;
    private static final int MAX_BANK_ID_POLLING_ATTEMPTS = 60;
    private final int bankIdPollIntervalMS;
    private boolean hasRefreshed = false;

    public IkanoApiAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) throws NoSuchAlgorithmException {
        super(request, context);

        bankIdPollIntervalMS = 2000;
        credentials = request.getCredentials();

        apiClient = new IkanoApiClient(
                clientFactory.createCookieClient(context.getLogOutputStream()),
                credentials,
                request.getProvider().getPayload(),
                DEFAULT_USER_AGENT);
    }

    /**
     *  This constructor is used for unit tests
     */
    public IkanoApiAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair,
            IkanoApiClient apiClient, int pollIntervalMS) {
        super(request, context);

        bankIdPollIntervalMS = pollIntervalMS;
        credentials = request.getCredentials();

        this.apiClient = apiClient;
    }

    public boolean login() throws Exception {

        try {
            String reference = apiClient.authenticateWithBankId();

            pollBankIdSession(reference);

            CardList cards = apiClient.fetchCards();
            List<Card> unregisteredCards = cards.getUnregisteredCards();

            if (unregisteredCards.size() > 0) {
                apiClient.registerCards(unregisteredCards);
                cards = apiClient.fetchCards();
            }

            cards.ensureRegisteredCardExists();

            return true;
        } catch (CardNotFoundException e) {
            stopLoginAttempt("Inga kort för det angivna personnumret hittades, vänligen kontrollera personnumret och försök igen");
        } catch (LimitExceededException e) {
            stopLoginAttempt("Inloggningen med Mobilt BankID tog för lång tid, vänligen försök igen");
        } catch (UserErrorException e) {
            stopLoginAttempt(e.getMessage());
        }

        return false;
    }

    public void pollBankIdSession(String reference) throws Exception {
        int i = 0;

        openBankIdApp();

        while (true) {
            boolean sessionReceived = apiClient.fetchBankIdSession(reference);
            if (sessionReceived) {
                break;
            }

            if (i > MAX_BANK_ID_POLLING_ATTEMPTS) {
                throw new LimitExceededException();
            }

            Thread.sleep(bankIdPollIntervalMS);

            i++;
        }
    }

    @Override
    public void refresh() throws Exception {
        // The refresh command will call refresh multiple times.
        // This check ensures the refresh only runs once.
        if (hasRefreshed) {
            return;
        }
        hasRefreshed = true;

        try {
            List<Account> accounts = apiClient.fetchAccounts();
            financialDataCacher.cacheAccounts(accounts);

            for (Account account : accounts) {
                List<Transaction> transactions = apiClient.getTransactionsFor(account);

                boolean stop = false;

                while (apiClient.hasMoreTransactionHistory(account)) {
                    if (isContentWithRefresh(account, transactions)) {
                        /** Since Ikano transactions can be unsettled for longer periods of time (3-4 weeks),
                         *  we need to fetch more transactions than usual in order to avoid duplicates.
                         */
                        log.info(account, "Reached content with refresh date, fetching transactions one more time");
                        stop = true;
                    }

                    log.info(account, String.format(
                            "fetch more transactions - current transactions size ( %s )", transactions.size()));

                    transactions = apiClient.fetchMoreTransactionsFor(account);

                    log.info(account, String.format(
                            "fetched more transactions - new transactions size ( %s )", transactions.size()));

                    if (stop) {
                        break;
                    }
                }

                log.info(account, "Finished fetching transactions for account");
                financialDataCacher.updateTransactions(account, transactions);
            }
        } catch (CardNotFoundException e) {
            stopLoginAttempt("Inga kort för det angivna personnumret hittades, vänligen kontrollera personnumret och försök igen");
        } catch (AccountRelationNotFoundException e) {
            log.error("No relation between account and card were found, this SHOULD never happen", e);
        }
    }

    public void logout() {

    }

    private void openBankIdApp() {
        credentials.setSupplementalInformation(null);
        credentials.setStatus(CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION);

        supplementalRequester.requestSupplementalInformation(credentials, false);
    }

    private void stopLoginAttempt(String message) {
        log.info(message);
        statusUpdater.updateStatus(CredentialsStatus.AUTHENTICATION_ERROR, message);
    }

    public static class CardNotFoundException extends Exception {}

    public static class AccountRelationNotFoundException extends Exception {}
}
