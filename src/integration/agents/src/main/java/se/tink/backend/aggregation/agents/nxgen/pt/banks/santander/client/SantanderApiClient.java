package se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.client;

import static se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.client.Requests.ACCOUNTS_BODY;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.client.Requests.ASSETS_BODY;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.client.Requests.BUSINESS_DATA_REQUEST_HEADER;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.client.Requests.CARDS_BODY;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.client.Requests.CONTROL_HEADER;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.client.Requests.LOANS_BODY;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.client.Requests.SESSION_TOKEN_REQUEST_HEADER;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.client.Requests.constructCreditCardTransactionsRequestBody;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.client.Requests.constructDepositDetailsBody;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.client.Requests.constructTokenRequestBody;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.client.Requests.constructTransactionsRequestBody;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.client.Requests.contructInvestmentTransactionsRequestBody;

import java.time.LocalDate;
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
                        .header(CONTROL_HEADER, SESSION_TOKEN_REQUEST_HEADER)
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

    public ApiResponse fetchLoans() {
        return executeRequest(LOANS_BODY);
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

    public ApiResponse fetchDepositDetails(String accountNumber, String branchCode) {
        String body = constructDepositDetailsBody(accountNumber, branchCode);
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
}
