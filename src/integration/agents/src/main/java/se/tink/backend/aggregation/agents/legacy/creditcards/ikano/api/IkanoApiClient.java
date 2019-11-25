package se.tink.backend.aggregation.agents.creditcards.ikano.api;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.ws.rs.core.MediaType;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.IkanoApiAgent.AccountRelationNotFoundException;
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

public class IkanoApiClient {
    private Client client;
    private Credentials credentials;
    private CardType cardType;

    // Authentication tokens
    private final String deviceId;
    private final String deviceAuth;
    private String sessionId;
    private String sessionKey;

    private static final String ROOT_URL = "https://partner.ikanobank.se/mCommunicationService/";
    private static final String MOBILE_BANK_ID_REFERENCE_URI = "MobileBankIdReference/";
    private static final String MOBILE_BANK_ID_SESSION_URI = "MobileBankIdSession/";
    private static final String CARDS_URI = "Cards/ALL/";
    private static final String REGISTER_CARDS_URI = "RegisteredCards/";
    private static final String ENGAGEMENTS_URI = "Engagements/ALL/?numofbonustrans=1&numoftrans=";

    private CardEntities cardsAndTransactionsResponse = null;
    private static final int DEFAULT_LIMIT = 200;
    private int limit = DEFAULT_LIMIT;
    private final String userAgent;

    public IkanoApiClient(Client client, Credentials credentials, String payload, String userAgent)
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
                createClientRequest(MOBILE_BANK_ID_REFERENCE_URI)
                        .header("Username", credentials.getUsername())
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
        String uri = MOBILE_BANK_ID_SESSION_URI + reference;

        BankIdSession response =
                createClientRequest(uri)
                        .header("Username", credentials.getUsername())
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
        credentials.setSensitivePayload("SessionId", sessionId);
        credentials.setSensitivePayload("SessionKey", sessionKey);
    }

    public CardList fetchCards() throws LoginException, UserErrorException, FatalErrorException {
        CardList response =
                createClientRequest(CARDS_URI)
                        .header("SessionKey", sessionKey)
                        .header("SessionId", sessionId)
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
                    createClientRequest(REGISTER_CARDS_URI)
                            .header("SessionKey", sessionKey)
                            .header("SessionId", sessionId)
                            .post(RegisteredCards.class, request);

            response.logRequestedAndRegisteredCardTypes(credentials, card.getCardType());
        }
    }

    private void fetchCardsAndTransactions(int limit) throws LoginException {
        cardsAndTransactionsResponse =
                createClientRequest(ENGAGEMENTS_URI + limit)
                        .header("SessionKey", sessionKey)
                        .header("SessionId", sessionId)
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

    private WebResource.Builder createClientRequest(String uri) {
        return client.resource(ROOT_URL + uri)
                .header("User-Agent", userAgent)
                .header("DeviceId", deviceId)
                .header("DeviceAuth", deviceAuth)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }
}
