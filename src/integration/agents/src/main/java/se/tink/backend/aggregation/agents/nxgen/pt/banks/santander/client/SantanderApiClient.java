package se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.client;

import static se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.client.Requests.ACCOUNTS_BODY;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.client.Requests.ASSETS_BODY;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.client.Requests.BUSINESS_DATA_REQUEST_HEADER;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.client.Requests.CARDS_BODY;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.client.Requests.CONTROL_HEADER;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.client.Requests.CREDIT_CARD_TRANSACTIONS_BODY;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.client.Requests.INVESTMENT_TRANSACTIONS_BODY;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.client.Requests.SESSION_TOKEN_BODY;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.client.Requests.SESSION_TOKKEN_REQUEST_HEADER;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.client.Requests.TRANSACTIONS_BODY;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.SantanderConstants;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.SantanderConstants.RESPONSE_CODES;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.SantanderConstants.STORAGE;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SantanderApiClient {

    private final TinkHttpClient tinkHttpClient;
    private final SessionStorage persistentStorage;
    private final Parser parser;

    public SantanderApiClient(TinkHttpClient tinkHttpClient, SessionStorage persistentStorage) {
        this.tinkHttpClient = tinkHttpClient;
        this.persistentStorage = persistentStorage;
        this.parser = new Parser();
    }

    public boolean isAlive() {
        ApiResponse apiResponse = fetchAccounts();
        return !RESPONSE_CODES.SESSION_EXPIRED.equals(apiResponse.getCode());
    }

    public ApiResponse fetchAuthToken(String login, String password) {
        String rawResponse =
                tinkHttpClient
                        .request(SantanderConstants.API_URL)
                        .header(CONTROL_HEADER, SESSION_TOKKEN_REQUEST_HEADER)
                        .body(constructTokenRequestBody(login, password))
                        .post(String.class);

        return parser.parseResponse(rawResponse);
    }

    public ApiResponse fetchIdentityData() {
        return executeRequest(Requests.IDENTITY_DATA_BODY);
    }

    public ApiResponse fetchAccounts() {
        return executeRequest(ACCOUNTS_BODY);
    }

    public ApiResponse fetchTransactions(
            String accountNumber,
            String branchCode,
            LocalDate dateFrom,
            LocalDate dateTo,
            int pageNumber,
            int pageSize) {

        String body =
                constructTransactionsRequestBody(
                        accountNumber, branchCode, dateFrom, dateTo, pageNumber, pageSize);

        return executeRequest(body);
    }

    public ApiResponse fetchCards() {
        return executeRequest(CARDS_BODY);
    }

    public ApiResponse fetchCreditCardTransactions(
            String fullCardNumber, int pageNumber, int pageSize) {
        String body =
                constructCreditCardTransactionsRequestBody(fullCardNumber, pageNumber, pageSize);
        return executeRequest(body);
    }

    public ApiResponse fetchAssets() {
        return executeRequest(ASSETS_BODY);
    }

    public ApiResponse fetchInvestmentTransactions(String accountNumber, int page, int pageSize) {
        String body = contructInvestmentTransactionsRequestBody(accountNumber, page, pageSize);
        return executeRequest(body);
    }

    private ApiResponse executeRequest(String body) {
        String rawResponse =
                tinkHttpClient
                        .request(SantanderConstants.API_URL)
                        .header(
                                CONTROL_HEADER,
                                String.format(
                                        BUSINESS_DATA_REQUEST_HEADER,
                                        persistentStorage.get(STORAGE.SESSION_TOKEN)))
                        .body(body)
                        .post(String.class);

        return parser.parseResponse(rawResponse);
    }

    private String constructTokenRequestBody(String login, String password) {
        return String.format(SESSION_TOKEN_BODY, escapeString(login), escapeString(password));
    }

    private String constructTransactionsRequestBody(
            String accountNumber,
            String branchCode,
            LocalDate dateFrom,
            LocalDate dateTo,
            int pageNumber,
            int pageSize) {

        DateTimeFormatter requestDateFormat =
                DateTimeFormatter.ofPattern(SantanderConstants.DATE_FORMAT);

        return String.format(
                TRANSACTIONS_BODY,
                pageSize,
                accountNumber,
                branchCode,
                accountNumber,
                pageNumber,
                requestDateFormat.format(dateTo),
                requestDateFormat.format(dateFrom));
    }

    private String constructCreditCardTransactionsRequestBody(
            String fullCardNumber, int pageNumber, int pageSize) {
        return String.format(CREDIT_CARD_TRANSACTIONS_BODY, pageNumber, fullCardNumber, pageSize);
    }

    private String contructInvestmentTransactionsRequestBody(
            String accountNumber, int page, int pageSize) {
        return String.format(INVESTMENT_TRANSACTIONS_BODY, page, accountNumber, pageSize);
    }

    private String escapeString(String argValue) {
        return argValue.replace("|", "||");
    }
}
