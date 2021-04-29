package se.tink.backend.aggregation.agents.creditcards.ikano.api;

import com.google.common.io.Files;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import java.io.File;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.IkanoApiConstants.Error;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.errors.UserErrorException;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.cards.Card;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.cards.CardList;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.constants.CommonHeaders;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.i18n.LocalizableKey;

@AgentCapabilities(generateFromImplementedExecutors = true)
public final class IkanoApiAgent extends AbstractAgent
        implements RefreshCreditCardAccountsExecutor, RefreshIdentityDataExecutor {
    private final IkanoApiClient apiClient;
    private final Credentials credentials;
    private static final int MAX_BANK_ID_POLLING_ATTEMPTS = 60;
    private final int bankIdPollIntervalMS;
    private List<Account> accounts;

    public IkanoApiAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair)
            throws NoSuchAlgorithmException {
        super(request, context);

        bankIdPollIntervalMS = 2000;
        credentials = request.getCredentials();

        apiClient =
                new IkanoApiClient(
                        clientFactory.createCustomClient(
                                context.getLogOutputStream(), createClientConfig()),
                        credentials,
                        request.getProvider().getPayload(),
                        CommonHeaders.DEFAULT_USER_AGENT);
    }

    /** This constructor is used for unit tests */
    public IkanoApiAgent(
            CredentialsRequest request,
            CompositeAgentContext context,
            SignatureKeyPair signatureKeyPair,
            IkanoApiClient apiClient,
            int pollIntervalMS) {
        super(request, context);

        bankIdPollIntervalMS = pollIntervalMS;
        credentials = request.getCredentials();

        this.apiClient = apiClient;
    }

    private ApacheHttpClient4Config createClientConfig() {
        try {
            final ApacheHttpClient4Config config = new DefaultApacheHttpClient4Config();

            final KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC");
            final InputStream stream =
                    Files.asByteSource(new File("data/agents/ikano/ikano.p12")).openStream();
            final char[] keystorePassword = "changeme".toCharArray();
            keyStore.load(stream, keystorePassword);

            final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, keystorePassword);

            final TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStore);

            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(
                    keyManagerFactory.getKeyManagers(),
                    trustManagerFactory.getTrustManagers(),
                    new SecureRandom());

            final SSLSocketFactory factory =
                    new SSLSocketFactory(sslContext, SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
            final ClientConnectionManager manager = new BasicClientConnectionManager();

            final Scheme https = new Scheme("https", 443, factory);
            manager.getSchemeRegistry().register(https);

            config.getProperties()
                    .put(ApacheHttpClient4Config.PROPERTY_CONNECTION_MANAGER, manager);
            return config;
        } catch (Exception e) {
            throw new IllegalStateException("Could not create client config", e);
        }
    }

    @Override
    public boolean login() throws Exception {

        try {
            String reference = apiClient.authenticateWithBankId();
            openBankIdApp();

            pollBankIdSession(reference);

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
