package se.tink.backend.aggregation.agents.creditcards.ikano.api;

import static se.tink.backend.aggregation.agents.creditcards.ikano.api.IkanoApiConstants.QueryValues.DEFAULT_LIMIT;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.ws.rs.core.MediaType;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.IkanoApiAgent.AccountRelationNotFoundException;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.IkanoApiConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.errors.FatalErrorException;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.errors.UserErrorException;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.requests.RegisterCardRequest;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.bankIdReference.BankIdReference;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.bankIdSession.BankIdSession;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.cards.Card;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.cards.CardList;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.engagements.CardEntities;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.engagements.CardType;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.registerCard.RegisteredCards;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.utils.IkanoCrypt;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class IkanoApiClient {
    private final TinkHttpClient client;
    private final Credentials credentials;
    private final CardType cardType;

    // Authentication tokens
    private final String deviceId;
    private final String deviceAuth;
    private String sessionId;
    private String sessionKey;

    private CardEntities cardsAndTransactionsResponse = null;
    private int limit = DEFAULT_LIMIT;
    private final String userAgent;

    public IkanoApiClient(
            TinkHttpClient client, Credentials credentials, String payload, String userAgent)
            throws NoSuchAlgorithmException {
        this.client = client;
        this.credentials = credentials;
        cardType = CardType.valueOf(payload);
        deviceId = IkanoCrypt.findOrGenerateDeviceIdFor(credentials);
        deviceAuth = IkanoCrypt.generateDeviceAuth(deviceId);
        this.userAgent = userAgent;
    }

    String authenticateWithBankId()
            throws BankIdException, UserErrorException, FatalErrorException {
        BankIdReference response =
                createClientRequest(IkanoApiConstants.Endpoints.MOBILE_BANK_ID_REFERENCE_URI)
                        .header(HeaderKeys.USERNAME, credentials.getUsername())
                        .get(BankIdReference.class);

        if (response.isBankIdAlreadyInProgress()) {
            throw BankIdError.ALREADY_IN_PROGRESS.exception();
        }
        if (response.isBankIdUnkownError()) {
            throw BankIdError.UNKNOWN.exception();
        }

        response.checkForErrors();

        return response.getReference();
    }

    boolean fetchBankIdSession(String reference)
            throws BankIdException, UserErrorException, FatalErrorException {
        String uri = IkanoApiConstants.Endpoints.MOBILE_BANK_ID_SESSION_URI + reference;

        BankIdSession response =
                createClientRequest(uri)
                        .header(HeaderKeys.USERNAME, credentials.getUsername())
                        .get(BankIdSession.class);

        checkBankIdResponseForErrors(response);

        if (response.hasSession()) {
            sessionId = response.getSessionId();
            sessionKey = response.getSessionKey();

            storeSessionInSensitivePayload(sessionId, sessionKey);
            return true;
        }

        return false;
    }

    private void checkBankIdResponseForErrors(BankIdSession response)
            throws BankIdException, UserErrorException, FatalErrorException {
        if (response.isBankIdNoClient()) {
            throw BankIdError.NO_CLIENT.exception();
        }

        if (response.isBankIdCancel()) {
            throw BankIdError.CANCELLED.exception();
        }

        if (response.isBankIdUnkownError()) {
            throw BankIdError.UNKNOWN.exception();
        }

        response.checkForErrors();
    }

    /** Store session tokens in sensitive payload, so it will be masked from logs */
    private void storeSessionInSensitivePayload(String sessionId, String sessionKey) {
        credentials.setSensitivePayload(HeaderKeys.SESSION_ID, sessionId);
        credentials.setSensitivePayload(HeaderKeys.SESSION_KEY, sessionKey);
    }

    public CardList fetchCards() throws LoginException, UserErrorException, FatalErrorException {
        CardList response =
                createClientRequest(IkanoApiConstants.Endpoints.CARDS_URI)
                        .header(HeaderKeys.SESSION_KEY, sessionKey)
                        .header(HeaderKeys.SESSION_ID, sessionId)
                        .get(CardList.class);

        // Throws not a customer exception if user has no cards
        response.ensureHasCards();

        response.checkForErrors();

        response.logCards();
        response.keepSelectedCardTypes(cardType);

        return response;
    }

    public void registerCards(List<Card> cards) {
        for (Card card : cards) {
            RegisterCardRequest request = new RegisterCardRequest();
            request.setCardType(card.getCardType());

            RegisteredCards response =
                    createClientRequest(IkanoApiConstants.Endpoints.REGISTER_CARDS_URI)
                            .header(HeaderKeys.SESSION_KEY, sessionKey)
                            .header(HeaderKeys.SESSION_ID, sessionId)
                            .post(RegisteredCards.class, request);

            response.logRequestedAndRegisteredCardTypes(card.getCardType());
        }
    }

    private void fetchCardsAndTransactions(int limit) throws LoginException {
        cardsAndTransactionsResponse =
                createClientRequest(IkanoApiConstants.Endpoints.ENGAGEMENTS_URI + limit)
                        .header(HeaderKeys.SESSION_KEY, sessionKey)
                        .header(HeaderKeys.SESSION_ID, sessionId)
                        .get(CardEntities.class);

        cardsAndTransactionsResponse.keepSelectedCardTypes(cardType);
    }

    public List<Account> fetchAccounts() throws LoginException {
        if (cardsAndTransactionsResponse == null
                || cardsAndTransactionsResponse.getCards().isEmpty()) {
            fetchCardsAndTransactions(DEFAULT_LIMIT);
        }

        return cardsAndTransactionsResponse.getTinkAccounts();
    }

    List<Transaction> fetchMoreTransactionsFor(Account account)
            throws AccountRelationNotFoundException, LoginException {
        if (!hasMoreTransactionHistory(account)) {
            return getTransactionsFor(account);
        }

        limit += DEFAULT_LIMIT;
        fetchCardsAndTransactions(limit);

        return getTransactionsFor(account);
    }

    public List<Transaction> getTransactionsFor(Account account)
            throws IkanoApiAgent.AccountRelationNotFoundException {
        if (cardsAndTransactionsResponse == null) {
            throw new IllegalStateException(
                    "It's not possible to get transactions from the response before fetching cards and transactions");
        }

        return cardsAndTransactionsResponse.getCardFor(account).getTinkTransactions();
    }

    public CardEntities getResponse() throws LoginException {
        if (cardsAndTransactionsResponse == null
                || cardsAndTransactionsResponse.getCards().isEmpty()) {
            fetchCardsAndTransactions(DEFAULT_LIMIT);
        }

        return cardsAndTransactionsResponse;
    }

    boolean hasMoreTransactionHistory(Account account)
            throws IkanoApiAgent.AccountRelationNotFoundException {
        if (cardsAndTransactionsResponse == null) {
            throw new IllegalStateException(
                    "It's not possible to check transaction history before cards and transactions have been fetched");
        }

        int numberOfTransactions =
                cardsAndTransactionsResponse.getCardFor(account).getTransactions().size();
        return numberOfTransactions == this.limit;
    }

    private RequestBuilder createClientRequest(String uri) {
        return client.request(IkanoApiConstants.Endpoints.ROOT_URL + uri)
                .header(HeaderKeys.USER_AGENT, userAgent)
                .header(HeaderKeys.DEVICE_ID, deviceId)
                .header(HeaderKeys.DEVICE_AUTH, deviceAuth)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }
}
