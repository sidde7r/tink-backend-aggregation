package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarConstants.Fetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.authenticator.rpc.BankIdInitRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.authenticator.rpc.BankIdInitResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.authenticator.rpc.BankIdLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.authenticator.rpc.BankIdLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.executor.rpc.DirectTransferRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.creditcard.rpc.FetchCardTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.creditcard.rpc.FetchCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.creditcard.rpc.FetchCreditCardResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.rpc.FetchISKResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.rpc.FetchInstrumentDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.rpc.FetchPensionResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.rpc.FetchPensionWithLifeInsuranceAgreementRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.rpc.FetchPensionWithLifeInsuranceAgreementResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.rpc.FetchPensionWithLifeInsuranceResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.rpc.FetchPensionWithLifeinsuranceRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.rpc.FetchPortfolioCashBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.rpc.FetchSecurityHoldingResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.loan.rpc.FetchLoanDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.loan.rpc.FetchLoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.loan.rpc.FetchLoanOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.rpc.FetchTransactionRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.rpc.FetchTransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.rpc.FetchUpcomingRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.rpc.FetchUpcomingResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transfer.rpc.FetchPaymentAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transfer.rpc.FetchSavedPaymentRecipientsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transfer.rpc.FetchTransferrableResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.i18n_aggregation.Catalog;

public class LansforsakringarApiClient {
    private TinkHttpClient httpClient;
    private final SessionStorage sessionStorage;
    private final Catalog catalog;
    private final PersistentStorage persistentStorage;

    public LansforsakringarApiClient(
            TinkHttpClient httpClient,
            SessionStorage sessionStorage,
            Catalog catalog,
            PersistentStorage persistentStorage) {
        this.httpClient = httpClient;
        this.sessionStorage = sessionStorage;
        this.catalog = catalog;
        this.persistentStorage = persistentStorage;
    }

    public BankIdInitResponse initBankIdLogin(String ssn) {
        return getBaseRequest(Urls.INIT_BANKID)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(BankIdInitRequest.of(ssn), MediaType.APPLICATION_JSON_TYPE)
                .post(BankIdInitResponse.class);
    }

    public BankIdLoginResponse pollBankIdLogin(String reference) {
        return getBaseRequest(Urls.LOGIN_BANKID)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(
                        BankIdLoginRequest.of(
                                sessionStorage.get(StorageKeys.SSN), reference, false),
                        MediaType.APPLICATION_JSON_TYPE)
                .post(BankIdLoginResponse.class);
    }

    public void renewSession() {
        getBaseRequest(Urls.RENEW_SESSION)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(HttpResponse.class);
    }

    public FetchAccountsResponse fetchAccounts() {
        return getBaseRequest(Urls.FETCH_ACCOUNTS)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(FetchAccountsResponse.class);
    }

    public FetchTransactionResponse fetchBookedTransactions(String accountNumber, int page) {
        return getBaseRequest(Urls.FETCH_TRANSACTIONS)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(
                        FetchTransactionRequest.of(
                                accountNumber,
                                sessionStorage.get(StorageKeys.SSN),
                                Fetcher.CUSTOMER_PROFILE_TYPE,
                                Fetcher.BOOKED_TRANSACTION_STATUS,
                                page),
                        MediaType.APPLICATION_JSON_TYPE)
                .post(FetchTransactionResponse.class);
    }

    public FetchUpcomingResponse fetchUpcomingTransactions(String accountNumber) {
        return getBaseRequest(Urls.FETCH_UPCOMING)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(FetchUpcomingRequest.of(accountNumber), MediaType.APPLICATION_JSON_TYPE)
                .post(FetchUpcomingResponse.class);
    }

    public FetchCardTransactionsResponse getCardTransactions(String cardNumber, int page) {
        return getBaseRequest(Urls.FETCH_CARD_TRANSACTIONS)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(
                        FetchCardTransactionsRequest.of(page, cardNumber),
                        MediaType.APPLICATION_JSON_TYPE)
                .post(FetchCardTransactionsResponse.class);
    }

    public FetchCreditCardResponse fetchCreditCards() {
        return getBaseRequest(Urls.FETCH_CARDS)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(FetchCreditCardResponse.class);
    }

    public FetchPensionResponse fetchPension() {
        return getBaseRequest(Urls.FETCH_PENSION_OVERVIEW)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(FetchPensionResponse.class);
    }

    public FetchPensionWithLifeInsuranceResponse fetchPensionWithLifeInsurance() {
        return getBaseRequest(Urls.FETCH_PENSION_WITH_LIFE_INSURANCE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(
                        FetchPensionWithLifeinsuranceRequest.of(
                                sessionStorage.get(StorageKeys.SSN),
                                Fetcher.PENSION_ACCOUNT_TYPE,
                                Fetcher.CUSTOMER_PROFILE_TYPE),
                        MediaType.APPLICATION_JSON_TYPE)
                .post(FetchPensionWithLifeInsuranceResponse.class);
    }

    public FetchPensionWithLifeInsuranceAgreementResponse fetchPensionWithLifeInsuranceAgreement(
            String agreementId) {
        return getBaseRequest(Urls.FETCH_PENSION_WITH_LIFE_INSURANCE_AGREEMENT)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(
                        FetchPensionWithLifeInsuranceAgreementRequest.of(
                                sessionStorage.get(StorageKeys.SSN),
                                Fetcher.CUSTOMER_PROFILE_TYPE,
                                agreementId),
                        MediaType.APPLICATION_JSON_TYPE)
                .post(FetchPensionWithLifeInsuranceAgreementResponse.class);
    }

    public FetchLoanOverviewResponse fetchLoanOverview() {
        return getBaseRequest(Urls.FETCH_LOAN_OVERVIEW)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(FetchLoanOverviewResponse.class);
    }

    public FetchLoanDetailsResponse fetchLoanDetails(String loanNumber) {
        return getBaseRequest(Urls.FETCH_LOAN_DETAILS)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(FetchLoanDetailsRequest.of(loanNumber), MediaType.APPLICATION_JSON_TYPE)
                .post(FetchLoanDetailsResponse.class);
    }

    public FetchSavedPaymentRecipientsResponse fetchSavedPaymentRecipients() {
        return getBaseRequest(Urls.FETCH_PAYMENT_SAVED_RECEPIENTS)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(FetchSavedPaymentRecipientsResponse.class);
    }

    public FetchTransferrableResponse fetchSavedTransferDestinationAccounts() {
        return getBaseRequest(Urls.FETCH_TRANSFER_SAVED_RECEPIENTS)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(FetchTransferrableResponse.class);
    }

    public FetchTransferrableResponse fetchTransferSourceAccounts() {
        return getBaseRequest(Urls.FETCH_TRANSFER_SOURCE_ACCOUNTS)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(FetchTransferrableResponse.class);
    }

    public FetchTransferrableResponse fetchTransferDestinationAccounts() {
        return getBaseRequest(Urls.FETCH_TRANSFER_DESTINATION_ACCOUNTS)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(FetchTransferrableResponse.class);
    }

    public FetchPaymentAccountResponse fetchPaymentAccounts() {
        return getBaseRequest(Urls.FETCH_PAYMENT_ACCOUNTS)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(FetchPaymentAccountResponse.class);
    }

    public FetchISKResponse fetchISK() {
        return getBaseRequest(Urls.FETCH_ISK)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(FetchISKResponse.class);
    }

    public FetchPortfolioCashBalanceResponse fetchPortfolioCashBalance(String depotNumber) {
        return getBaseRequest(
                        Urls.FETCH_PORTFOLIO_CASH_BALANCE.queryParam(
                                QueryKeys.DEPOT_NUMBER, depotNumber))
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(FetchPortfolioCashBalanceResponse.class);
    }

    public FetchSecurityHoldingResponse fetchFundSecurityHoldings(String depotNumber) {
        return getBaseRequest(
                        Urls.FETCH_INSTRUMENTS_FUND.queryParam(QueryKeys.DEPOT_NUMBER, depotNumber))
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(FetchSecurityHoldingResponse.class);
    }

    public FetchSecurityHoldingResponse fetchStockSecurityHoldings(String depotNumber) {
        return getBaseRequest(
                        Urls.FETCH_INSTRUMENTS_STOCK.queryParam(
                                QueryKeys.DEPOT_NUMBER, depotNumber))
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(FetchSecurityHoldingResponse.class);
    }

    public FetchInstrumentDetailsResponse fetchInstrumentDetails(String depotNumber, String isin) {
        return getBaseRequest(
                        Urls.FETCH_INSTRUMENT_WITH_ISIN
                                .queryParam(QueryKeys.DEPOT_NUMBER, depotNumber)
                                .queryParam(QueryKeys.ISIN_CODE, isin))
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(FetchInstrumentDetailsResponse.class);
    }

    public void executeValidateTransfer(DirectTransferRequest request) {
        getBaseRequest(Urls.EXECUTE_DIRECT_TRANSFER_VALIDATE)
                .body(request, MediaType.APPLICATION_JSON_TYPE)
                .post();
    }

    public void executeDirectTransfer(DirectTransferRequest request) {
        getBaseRequest(Urls.EXECUTE_DIRECT_TRANSFER)
                .body(request, MediaType.APPLICATION_JSON_TYPE)
                .post();
    }

    private RequestBuilder getBaseRequest(URL url) {
        final String userSession =
                sessionStorage.getOrDefault(StorageKeys.ENTERPRISE_SERVICE_PRIMARY_SESSION, "");
        final String uToken = sessionStorage.getOrDefault(StorageKeys.TICKET, "");
        return httpClient
                .request(url)
                .header(HeaderKeys.DEVICE_ID, persistentStorage.get(HeaderKeys.DEVICE_ID))
                .cookie(HeaderKeys.USER_SESSION, userSession)
                .header(HeaderKeys.UTOKEN, uToken);
    }
}
