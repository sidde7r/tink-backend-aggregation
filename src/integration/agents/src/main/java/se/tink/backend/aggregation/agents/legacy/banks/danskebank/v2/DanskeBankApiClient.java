package se.tink.backend.aggregation.agents.banks.danskebank.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.encryption.MessageContainer;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.encryption.MobileBankingEncryptionHelper;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.helpers.BankIdResourceHelper;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.helpers.BankIdServiceType;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.AbstractResponse;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.AccountEntity;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.AccountListResponse;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.BankIdModuleInput;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.BankIdRequest;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.BankIdResponse;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.BillRequest;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.BillResponse;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.CardsResponse;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.ChallengeResponseRequest;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.ChallengeResponseResponse;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.CreateSessionRequest;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.CreateSessionResponse;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.EInvoiceDetailsResponse;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.EInvoiceListResponse;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.InitBankIdRequest;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.InitBankIdResponse;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.InitSessionRequest;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.InitSessionResponse;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.PortfoliosListResponse;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.SignBankIdRequest;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.SignBankIdResponse;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.TransferDetailsResponse;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.TransferRequest;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.TransferResponse;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.VerifyBankIdRequest;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.VerifyBankIdResponse;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;

public class DanskeBankApiClient {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String COOKIE_KEY_NSSID = "NSSID";
    private static final String HEADER_KEY_AUTHORIZATION = "Authorization";
    private static final String HEADER_KEY_USERAGENT = "User-Agent";
    private static final String QUERYPARAM_KEY_MAGICKEY = "magicKey";
    private static final String CHARSET_NAME_ASCII = "ASCII";
    private static final String USER_NOT_CUSTOMER_STATUS_CODE = "333";

    private final BankIdResourceHelper bankIdResourceHelper;
    private final String providerCountry;
    private final String sessionLanguage;
    private final Client client;
    private final String userAgent;
    private Credentials credentials;

    private CardsResponse cardsResponse;
    private String magicKey;
    private String nssId;

    public DanskeBankApiClient(
            Client client,
            String userAgent,
            BankIdResourceHelper bankIdResourceHelper,
            String providerCountry,
            String sessionLanguage,
            Credentials credentials) {
        this.client = client;
        this.userAgent = userAgent;
        this.bankIdResourceHelper = bankIdResourceHelper;
        this.providerCountry = providerCountry;
        this.sessionLanguage = sessionLanguage;
        this.magicKey = null;
        this.nssId = null;
        this.credentials = credentials;
    }

    /** Initializes a session for a password login. */
    public CreateSessionResponse initAndCreateSession() throws LoginException, SessionException {
        return initAndCreateSession(Optional.empty());
    }

    public LoginResponse loginWithPassword(LoginRequest loginRequest)
            throws LoginException, SessionException {
        LoginResponse response = post(DanskeBankUrl.LOGIN, LoginResponse.class, loginRequest);
        handleAbstractResponseError(response, false);
        return response;
    }

    public LoginResponse loginConfirmChallenge(
            LoginResponse loginResponse, String challengeResponse)
            throws LoginException, SessionException {
        ChallengeResponseRequest challengeRequest = new ChallengeResponseRequest();
        challengeRequest.setChallengeData(loginResponse.getChallengeData());
        challengeRequest.setResponse(challengeResponse);

        LoginResponse response =
                post(DanskeBankUrl.LOGIN_CHALLENGE, LoginResponse.class, challengeRequest);
        handleAbstractResponseError(response);
        return response;
    }

    public InitBankIdResponse bankIdInitAuth(InitBankIdRequest initBankIdRequest) {
        MessageContainer initBankIdAuthMessage = initBankIdRequest.encrypt(bankIdResourceHelper);

        ClientResponse initBankIdClientResponse =
                postBankIdService(BankIdServiceType.INITAUTH, initBankIdAuthMessage);
        handleClientResponseError(initBankIdClientResponse);

        MessageContainer initBankIdAuthResponseMessage =
                initBankIdClientResponse.getEntity(MessageContainer.class);
        return initBankIdAuthResponseMessage.decrypt(
                bankIdResourceHelper, InitBankIdResponse.class);
    }

    public BankIdResponse bankIdVerify(BankIdServiceType bankIdServiceType, String orderReference)
            throws LoginException {
        MessageContainer verifyAuthMessage =
                createEncryptedVerifyRequest(bankIdServiceType, orderReference);

        ClientResponse verifyClientResponse =
                postBankIdService(bankIdServiceType, verifyAuthMessage);

        return getDecryptedBankIdResponse(bankIdServiceType, verifyClientResponse);
    }

    /**
     * Used after BankID auth is complete, to tie the two Danske systems to each other by the NSSID
     * cookie. Without this call the user will never be logged in and authed to access nodes in the
     * `MB` url subset.
     */
    public CreateSessionResponse createAuthenticatedSession()
            throws LoginException, SessionException {
        CreateSessionResponse response = initAndCreateSession(Optional.ofNullable(nssId));
        handleAbstractResponseError(response);
        return response;
    }

    public AccountListResponse getAccounts() {
        AccountListResponse response = get(DanskeBankUrl.ACCOUNTS, AccountListResponse.class);
        handleGenericErrors(response);
        return response;
    }

    public PortfoliosListResponse getPortfolios() {
        PortfoliosListResponse response =
                get(DanskeBankUrl.PORTFOLIOS, PortfoliosListResponse.class);
        handleGenericErrors(response);
        return response;
    }

    public PapersListResponse getPortfolioPapers(String portfolioId) {
        PapersListResponse response =
                get(DanskeBankUrl.portfolioPapers(portfolioId), PapersListResponse.class);
        handleGenericErrors(response);
        return response;
    }

    boolean isCreditCardAccount(AccountEntity accountEntity) {
        if (cardsResponse == null) {
            // The call bellow is used to get a cookie which is needed when fetching the cards
            fetchCustomerSettings();
            cardsResponse = getWithoutParameters(DanskeBankUrl.CARDS, CardsResponse.class);
        }

        if (cardsResponse.getOutput() == null
                || cardsResponse.getOutput().getCreditCardAccounts() == null) {
            return false;
        }

        List<String> creditCardAccountIds = cardsResponse.getOutput().getCreditCardAccounts();
        return creditCardAccountIds.contains(accountEntity.getAccountId());
    }

    void fetchCustomerSettings() {
        MultivaluedMap<String, String> queryParameters = new MultivaluedMapImpl();
        queryParameters.put("PushAcceptedOnDevice", Collections.singletonList("no"));

        ClientResponse response =
                createRequestWithCustomParameters(DanskeBankUrl.CUSTOMER_SETTINGS, queryParameters)
                        .get(ClientResponse.class);
        handleClientResponseError(response);
    }

    private WebResource.Builder createRequestWithCustomParameters(
            String url, MultivaluedMap<String, String> queryParameters) {
        return client.resource(url)
                .queryParams(queryParameters)
                .cookie(new NewCookie("NSSID", nssId))
                .header(HEADER_KEY_USERAGENT, userAgent)
                .accept(MediaType.APPLICATION_JSON)
                .acceptLanguage("sv-se")
                .type(MediaType.APPLICATION_JSON);
    }

    /**
     * Fetch pages with transactions (in batches of 40 as the app does).
     *
     * <p>FYI: After logging number of pages fetched over ~1 day the following stats were outcome: -
     * Median: 1 page fetched - Mean: 1.24… pages fetched - Max: 4 pages
     *
     * @param previousResponse For paging, so that we continue from the last transaction and
     *     continue backwards
     * @return One page of transactions. To fetch more do another get and send in the previous
     *     response returned.
     */
    public AccountResponse getTransactions(
            AccountEntity accountEntity,
            DanskeBankV2Agent.TransactionType type,
            Optional<AccountResponse> previousResponse) {

        MultivaluedMap<String, String> queryParameters = new MultivaluedMapImpl();
        queryParameters.add("pageSize", "40");

        if (type == DanskeBankV2Agent.TransactionType.FUTURE) {
            queryParameters.add("future", "true");
        }

        if (previousResponse.isPresent()) {
            queryParameters.add("lastId", previousResponse.get().getLastId());
        } else if (type == DanskeBankV2Agent.TransactionType.NORMAL) {
            queryParameters.add("fromDateSums", "2013-01-01");
        }

        String transactionsUrl =
                DanskeBankUrl.serviceAccountsIdTransaction(accountEntity.getAccountId(), type);
        AccountResponse response = get(transactionsUrl, queryParameters, AccountResponse.class);
        handleGenericErrors(response);
        return response;
    }

    public TransferDetailsResponse getTransferAccounts() {
        TransferDetailsResponse response =
                get(DanskeBankUrl.ACCOUNTS_TRANSFER_DETAILS, TransferDetailsResponse.class);
        handleGenericErrors(response);
        return response;
    }

    public TransferDetailsResponse getPaymentAccounts() {
        TransferDetailsResponse response =
                get(DanskeBankUrl.ACCOUNTS_BILLS_DETAILS, TransferDetailsResponse.class);
        handleGenericErrors(response);
        return response;
    }

    public TransferResponse createTransfer(TransferRequest transferRequest) throws IOException {
        // Hack to ensure Jersey/Jackson doesn't escape stuff.
        String transferRequestSerialized =
                MAPPER.writeValueAsString(transferRequest).replace("\\\\", "\\");

        TransferResponse response =
                post(DanskeBankUrl.TRANSFER, TransferResponse.class, transferRequestSerialized);
        handleGenericErrors(response);
        return response;
    }

    public BillResponse createPayment(BillRequest billRequest) throws IOException {
        // Hack to ensure Jersey/Jackson doesn't escape stuff.
        String billRequestSerialized = MAPPER.writeValueAsString(billRequest).replace("\\\\", "\\");

        BillResponse response = post(DanskeBankUrl.BILL, BillResponse.class, billRequestSerialized);
        handleGenericErrors(response);
        return response;
    }

    public void confirmTransfer(String challengeData) {
        ChallengeResponseRequest challengeResponseRequest = new ChallengeResponseRequest();
        challengeResponseRequest.setChallengeData(challengeData);
        challengeResponseRequest.setResponse("");

        confirmTransfer(challengeResponseRequest);
    }

    public void confirmTransfer(ChallengeResponseRequest challengeResponseRequest) {
        sendChallengeResponse(DanskeBankUrl.TRANSFER_CONFIRMATION, challengeResponseRequest);
    }

    public void confirmPayment(ChallengeResponseRequest challengeResponseRequest) {
        sendChallengeResponse(DanskeBankUrl.BILL_CONFIRMATION, challengeResponseRequest);
    }

    private void sendChallengeResponse(String url, ChallengeResponseRequest request) {
        ChallengeResponseResponse response = post(url, ChallengeResponseResponse.class, request);

        if (!response.isStatusOk()) {
            String statusText = response.getStatus().getStatusText();
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage("Error from DanskeBank: " + statusText)
                    .setMessage("Failed to confirm transfer, error from Danske Bank: " + statusText)
                    .build();
        }
    }

    public EInvoiceListResponse getEInvoices() {
        EInvoiceListResponse response =
                get(DanskeBankUrl.EINVOICE_LIST, EInvoiceListResponse.class);
        handleGenericErrors(response);
        return response;
    }

    public EInvoiceDetailsResponse getEInvoiceDetails(String transactionId)
            throws UnsupportedEncodingException {
        String detailsUrl = DanskeBankUrl.eInvoiceDetails(transactionId);

        EInvoiceDetailsResponse response = get(detailsUrl, EInvoiceDetailsResponse.class);
        return response;
    }

    private <T extends AbstractResponse> T get(
            String url, MultivaluedMap<String, String> queryParameters, Class<T> responseType) {
        T response = createRequest(url, queryParameters).get(responseType);
        updateMagicKey(response);
        return response;
    }

    private <T extends CardsResponse> T getWithoutParameters(String url, Class<T> responseType) {
        return createRequestWithNssidCookie(url).get(responseType);
    }

    private <T extends AbstractResponse> T get(String url, Class<T> responseType) {
        T response = createRequest(url).get(responseType);
        updateMagicKey(response);
        return response;
    }

    private <T extends AbstractResponse> T post(
            String url, Class<T> responseType, Object requestEntity) {
        T response = postWithoutMagicKeyUpdate(url, responseType, requestEntity);

        // This is needed to intercept the nssid from password login
        if (response instanceof LoginResponse) {
            nssId = ((LoginResponse) response).getSgSession();
            // Store tokens in sensitive payload, so it will be masked from logs
            credentials.setSensitivePayload(COOKIE_KEY_NSSID, nssId);
        }

        updateMagicKey(response);
        return response;
    }

    private void updateMagicKey(AbstractResponse response) {
        magicKey = response.getMagicKey();

        // Store tokens in sensitive payload, so it will be masked from logs
        credentials.setSensitivePayload(QUERYPARAM_KEY_MAGICKEY, magicKey);
    }

    private <T extends AbstractResponse> T postWithoutMagicKeyUpdate(
            String url, Class<T> responseType, Object requestEntity) {
        return createRequest(url).post(responseType, requestEntity);
    }

    private ClientResponse postBankIdService(
            BankIdServiceType serviceType, MessageContainer messageContainer) {
        ClientResponse response =
                createRequest(serviceType).post(ClientResponse.class, messageContainer);
        interceptNssIdCookie(response);
        return response;
    }

    private void interceptNssIdCookie(ClientResponse response) {
        for (NewCookie cookie : response.getCookies()) {
            if (cookie.getName().equals(COOKIE_KEY_NSSID)) {
                nssId = cookie.getValue();
                // Store tokens in sensitive payload, so it will be masked from logs
                credentials.setSensitivePayload(COOKIE_KEY_NSSID, nssId);
                return;
            }
        }
    }

    /** Make sure to do `updateMagicKey(…)` on the result entity if needed. */
    private WebResource.Builder createRequest(String url) {
        return createRequest(url, new MultivaluedMapImpl());
    }

    /** Make sure to do `updateMagicKey(…)` on the result entity if needed. */
    private WebResource.Builder createRequest(BankIdServiceType bankIdServiceType) {
        String authorizationHeader =
                bankIdResourceHelper.generateAuthorizationHeader(bankIdServiceType);
        String url = BankIdResourceHelper.getServiceUrl(bankIdServiceType);

        WebResource.Builder builder =
                client.resource(url)
                        .header(HEADER_KEY_AUTHORIZATION, authorizationHeader)
                        .header(HEADER_KEY_USERAGENT, userAgent)
                        .accept(MediaType.APPLICATION_JSON)
                        .type(MediaType.APPLICATION_JSON);

        if (nssId != null) {
            builder = builder.cookie(new NewCookie(COOKIE_KEY_NSSID, nssId));
        }

        return builder;
    }

    /** Make sure to do `updateMagicKey(…)` on the result entity if needed. */
    private WebResource.Builder createRequest(
            String url, MultivaluedMap<String, String> queryParameters) {
        if (magicKey != null) {
            queryParameters.add(QUERYPARAM_KEY_MAGICKEY, getObfuscatedMagicKey());
        }

        return client.resource(url)
                .queryParams(queryParameters)
                .header(HEADER_KEY_USERAGENT, userAgent)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private WebResource.Builder createRequestWithNssidCookie(String url) {

        return client.resource(url)
                .cookie(new NewCookie("NSSID", nssId))
                .header(HEADER_KEY_USERAGENT, userAgent)
                .accept(MediaType.APPLICATION_JSON)
                .acceptLanguage("sv-se")
                .type(MediaType.APPLICATION_JSON);
    }

    private String getObfuscatedMagicKey() {
        try {
            return URLEncoder.encode(
                    MobileBankingEncryptionHelper.modifyMagicKey(magicKey), CHARSET_NAME_ASCII);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private MessageContainer createEncryptedVerifyRequest(
            BankIdServiceType bankIdServiceType, String orderReference) {
        BankIdRequest<? extends BankIdModuleInput> bankIdRequest;

        switch (bankIdServiceType) {
            case VERIFYAUTH:
                bankIdRequest = new VerifyBankIdRequest(orderReference);
                break;
            case VERIFYSIGN:
                bankIdRequest = new SignBankIdRequest(orderReference);
                break;
            default:
                throw new IllegalStateException("Unexpected BankID service type for verification");
        }

        return bankIdRequest.encrypt(bankIdResourceHelper);
    }

    private BankIdResponse getDecryptedBankIdResponse(
            BankIdServiceType bankIdServiceType, ClientResponse verifyBankidClientResponse)
            throws LoginException {
        MessageContainer verifyResponseMessage =
                verifyBankidClientResponse.getEntity(MessageContainer.class);
        if (verifyResponseMessage.getServiceStatus() != null
                && Objects.equals(
                        USER_NOT_CUSTOMER_STATUS_CODE,
                        verifyResponseMessage.getServiceStatus().getStatusCode())) {
            throw LoginError.NOT_CUSTOMER.exception();
        }

        handleClientResponseError(verifyBankidClientResponse);

        Class<? extends BankIdResponse> responseClass = getBankIdResponseClass(bankIdServiceType);
        return verifyResponseMessage.decrypt(bankIdResourceHelper, responseClass);
    }

    private static Class<? extends BankIdResponse> getBankIdResponseClass(
            BankIdServiceType bankIdServiceType) {
        switch (bankIdServiceType) {
            case VERIFYAUTH:
                return VerifyBankIdResponse.class;
            case VERIFYSIGN:
                return SignBankIdResponse.class;
            default:
                throw new IllegalStateException("Unexpected BankID service type for verification");
        }
    }

    /**
     * Used both by BankID login and by password login. When using BankID login we're using two
     * DanskeBank systems, at which point we're using the NSSID cookie to create an authenticated
     * session that allows us to be authenticated in both systems. For password logins we only use
     * the same system for login and for other authed calls, so there we don't use this call with
     * NSSID.
     */
    private CreateSessionResponse initAndCreateSession(Optional<String> nssId)
            throws LoginException, SessionException {
        InitSessionResponse initSessionResponse =
                postWithoutMagicKeyUpdate(
                        DanskeBankUrl.INITSESSION,
                        InitSessionResponse.class,
                        new InitSessionRequest());
        handleAbstractResponseError(initSessionResponse);

        CreateSessionRequest createSessionRequest =
                CreateSessionRequest.create(
                        initSessionResponse, credentials, providerCountry, sessionLanguage, nssId);

        CreateSessionResponse createSessionResponse =
                post(
                        DanskeBankUrl.CREATESESSION,
                        CreateSessionResponse.class,
                        createSessionRequest);
        handleAbstractResponseError(createSessionResponse, false);

        if (!createSessionResponse.isStatusOk()) {
            throw new IllegalStateException(
                    String.format(
                            "[userid: %s] Failed to create session: [%d] %s",
                            credentials.getUserId(),
                            createSessionResponse.getStatus().getStatusCode(),
                            createSessionResponse.getStatus().getStatusText()));
        }
        return createSessionResponse;
    }

    private void handleClientResponseError(ClientResponse response) {
        if (response.getStatus() >= 400) {
            throw new UniformInterfaceException(response);
        }
    }

    private <T extends AbstractResponse> void handleAbstractResponseError(T response)
            throws LoginException, SessionException {
        handleAbstractResponseError(response, true);
    }

    private <T extends AbstractResponse> void handleAbstractResponseError(
            T response, boolean failOnUnkownError) throws LoginException, SessionException {
        if (!response.isStatusOk()) {

            if (response.isStatusAuthorizationNotPossible()) {
                throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception();
            }
            if (response.isStatusTechnicalError()) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception();
            }
            if (response.isUnauthorizedError() || response.isSessionExpiredError()) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
            if (failOnUnkownError) {
                handleGenericErrors(response);
            }
        }
    }

    private <T extends AbstractResponse> void handleGenericErrors(T response) {
        if (!response.isStatusOk()) {
            throw new IllegalStateException(
                    "Error code: "
                            + response.getStatus().getStatusCode()
                            + ", error text: "
                            + response.getStatus().getStatusText());
        }
    }
}
