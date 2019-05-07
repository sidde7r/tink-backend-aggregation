package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.entities.BankIdBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.entities.SessionBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.rpc.BankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.rpc.SessionInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.einvoice.rpc.AcceptEInvoiceTransferRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.einvoice.rpc.FinishEInvoiceSignRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.einvoice.rpc.UpdateEInvoiceRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.einvoice.rpc.ValidateEInvoiceRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.einvoice.rpc.ValidateEInvoiceResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.entities.PaymentNameBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.entities.SignedAssignmentListEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.entities.TransferBankEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.entities.TransferBanksBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.rpc.PaymentNameResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.rpc.SignBundleResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.rpc.TransferBanksResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.rpc.TransferRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.AccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.TransactionsBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.UpcomingTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.rpc.UpcomingTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.entities.EInvoiceBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.rpc.EInvoiceResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.investment.entities.DepotEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.investment.entities.FundDetailsBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.investment.rpc.InstrumentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.investment.rpc.InvestmentAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.entities.LoansBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.rpc.LoanOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.AssignmentEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.AssignmentsBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.RecipientEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.rpc.AssignmentsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.rpc.RecipientsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.rpc.TransferResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.storage.IcaBankenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.storage.IcabankenPersistentStorage;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class IcaBankenApiClient {
    private static final Logger log = LoggerFactory.getLogger(IcaBankenApiClient.class);

    private final TinkHttpClient client;
    private final IcaBankenSessionStorage icaBankenSessionStorage;
    private final IcabankenPersistentStorage icabankenPersistentStorage;

    private AccountsEntity cachedAccounts;

    public IcaBankenApiClient(
            TinkHttpClient client,
            IcaBankenSessionStorage icaBankenSessionStorage,
            IcabankenPersistentStorage icabankenPersistentStorage) {
        this.client = client;
        this.icaBankenSessionStorage = icaBankenSessionStorage;
        this.icabankenPersistentStorage = icabankenPersistentStorage;
    }

    private RequestBuilder createPostRequest(URL url) {
        return createRequest(url).type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequest(URL url) {
        RequestBuilder request = client.request(url);

        String sessionId = icaBankenSessionStorage.getSessionId();

        if (!Strings.isNullOrEmpty(sessionId)) {
            request.header(IcaBankenConstants.IdTags.SESSION_ID_TAG, sessionId);
        }

        String userInstallationId = icabankenPersistentStorage.getUserInstallationId();

        if (!Strings.isNullOrEmpty(userInstallationId)) {
            request.header(IcaBankenConstants.IdTags.USER_INSTALLATION_ID, userInstallationId);
        }

        return request;
    }

    public BankIdBodyEntity initBankId(String ssn) {
        return createPostRequest(
                        IcaBankenConstants.Urls.LOGIN_BANKID.parameter(
                                IcaBankenConstants.IdTags.IDENTIFIER_TAG, ssn))
                .post(BankIdResponse.class)
                .getBody();
    }

    public BankIdBodyEntity initEInvoiceBankId(String invoiceId) {
        return createPostRequest(
                        IcaBankenConstants.Urls.INIT_EINVOICE_SIGN.parameter(
                                IcaBankenConstants.IdTags.INVOICE_ID_TAG, invoiceId))
                .post(BankIdResponse.class)
                .getBody();
    }

    public BankIdBodyEntity initTransferSign() {
        return createPostRequest(IcaBankenConstants.Urls.INIT_TRANSFER_SIGN)
                .post(BankIdResponse.class)
                .getBody();
    }

    public BankIdResponse pollBankId(String reference) {
        return createRequest(
                        IcaBankenConstants.Urls.LOGIN_BANKID.parameter(
                                IcaBankenConstants.IdTags.IDENTIFIER_TAG, reference))
                .get(BankIdResponse.class);
    }

    public BankIdResponse pollTransferBankId(String requestId) {
        return createRequest(
                        IcaBankenConstants.Urls.SIGN_TRANSFER_COLLECT_URL.parameter(
                                IcaBankenConstants.IdTags.REQUEST_ID_TAG, requestId))
                .get(BankIdResponse.class);
    }

    public SessionBodyEntity fetchSessionInfo() {
        return createRequest(IcaBankenConstants.Urls.SESSION)
                .queryParam(
                        IcaBankenConstants.IdTags.DEVICE_APPLICATION_ID,
                        icabankenPersistentStorage.getDeviceApplicationId())
                .get(SessionInfoResponse.class)
                .getBody();
    }

    public AccountsEntity fetchAccounts() {
        if (cachedAccounts == null) {
            cachedAccounts = createRequest(IcaBankenConstants.Urls.ACCOUNTS)
                    .get(AccountsResponse.class).getBody()
                    .getAccounts();
        }

        return cachedAccounts;
    }

    public TransactionsBodyEntity fetchTransactionsWithDate(Account account, Date toDate) {
        return createRequest(
                        IcaBankenConstants.Urls.TRANSACTIONS
                                .parameter(
                                        IcaBankenConstants.IdTags.IDENTIFIER_TAG,
                                        account.getBankIdentifier())
                                .queryParam(
                                        IcaBankenConstants.IdTags.TO_DATE_TAG,
                                        ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate)))
                .get(TransactionsResponse.class)
                .getBody();
    }

    public TransactionsBodyEntity fetchTransactions(Account account) {
        return createRequest(
                        IcaBankenConstants.Urls.TRANSACTIONS.parameter(
                                IcaBankenConstants.IdTags.IDENTIFIER_TAG,
                                account.getBankIdentifier()))
                .get(TransactionsResponse.class)
                .getBody();
    }

    public TransactionsBodyEntity fetchReservedTransactions(Account account) {
        return createRequest(
                        IcaBankenConstants.Urls.RESERVED_TRANSACTIONS.parameter(
                                IcaBankenConstants.IdTags.IDENTIFIER_TAG,
                                account.getBankIdentifier()))
                .get(TransactionsResponse.class)
                .getBody();
    }

    public List<UpcomingTransactionEntity> fetchUpcomingTransactions() {
        return createRequest(IcaBankenConstants.Urls.UPCOMING_TRANSACTIONS)
                .get(UpcomingTransactionsResponse.class)
                .getBody()
                .getUpcomingTransactions();
    }

    public LoansBodyEntity fetchLoanOverview() {
        return createRequest(IcaBankenConstants.Urls.LOAN_OVERVIEW)
                .get(LoanOverviewResponse.class)
                .getBody();
    }

    public List<DepotEntity> getInvestments() {
        List<DepotEntity> depots =
                createRequest(IcaBankenConstants.Urls.DEPOTS)
                        .get(InvestmentAccountResponse.class)
                        .getBody()
                        .getDepots();

        return Optional.ofNullable(depots).orElseGet(Collections::emptyList);
    }

    public FundDetailsBodyEntity getFundDetails(String fundId) {
        return createRequest(
                        IcaBankenConstants.Urls.FUND_DETAILS.parameter(
                                IcaBankenConstants.IdTags.FUND_ID_TAG, fundId))
                .get(InstrumentResponse.class)
                .getBody();
    }

    public EInvoiceBodyEntity fetchEInvoices() {
        return createRequest(IcaBankenConstants.Urls.EINVOICES)
                .get(EInvoiceResponse.class)
                .getBody();
    }

    public void keepAlive() {
        createRequest(IcaBankenConstants.Urls.KEEP_ALIVE).get(HttpResponse.class);
    }

    public SignedAssignmentListEntity getSignedAssignments(String requestId) {
        return createPostRequest(
                        IcaBankenConstants.Urls.SIGNED_ASSIGNMENTS.queryParam(
                                IcaBankenConstants.IdTags.REQUEST_ID_TAG, requestId))
                .post(SignBundleResponse.class)
                .getBody()
                .getSignedAssignmentList();
    }

    public void deleteUnsignedTransfer(String transferId) {
        createRequest(
                        IcaBankenConstants.Urls.DELETE_UNSIGNED_TRANSFER.parameter(
                                IcaBankenConstants.IdTags.TRANSFER_ID_TAG, transferId))
                .delete();
    }

    public List<RecipientEntity> fetchDestinationAccounts() {
        return createRequest(IcaBankenConstants.Urls.TRANSFER_DESTINATIONS)
                .get(RecipientsResponse.class)
                .getBody()
                .getRecipients();
    }

    public TransferResponse putAssignmentInOutbox(TransferRequest transferRequest) {
        return createPostRequest(IcaBankenConstants.Urls.UNSIGNED_ASSIGNMENTS)
                .post(TransferResponse.class, transferRequest);
    }

    public List<AssignmentEntity> fetchUnsignedTransfers() {
        AssignmentsBodyEntity bodyEntity =
                createRequest(IcaBankenConstants.Urls.UNSIGNED_TRANSFERS)
                        .type(MediaType.APPLICATION_JSON)
                        .get(AssignmentsResponse.class)
                        .getBody();

        return Optional.ofNullable(bodyEntity.getAssignments()).orElseGet(Collections::emptyList);
    }

    public ValidateEInvoiceResponse validateEInvoice(ValidateEInvoiceRequest validateRequest) {
        return createPostRequest(IcaBankenConstants.Urls.VALIDATE_INVOICE)
                .post(ValidateEInvoiceResponse.class, validateRequest);
    }

    public ValidateEInvoiceResponse updateEInvoice(UpdateEInvoiceRequest updateEInvoiceRequest) {
        return createRequest(IcaBankenConstants.Urls.UPDATE_INVOICE)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .put(ValidateEInvoiceResponse.class, updateEInvoiceRequest);
    }

    public void acceptEInvoice(AcceptEInvoiceTransferRequest request) {
        createPostRequest(IcaBankenConstants.Urls.ACCEPT_EINVOICE)
                .post(HttpResponse.class, request);
    }

    public void finishEInvoiceSign(FinishEInvoiceSignRequest request) {
        createPostRequest(IcaBankenConstants.Urls.FINISH_EINVOICE_SIGN)
                .post(HttpResponse.class, request);
    }

    public List<TransferBankEntity> fetchTransferBanks() {
        TransferBanksBodyEntity bodyEntity =
                createRequest(IcaBankenConstants.Urls.TRANSFER_BANKS)
                        .get(TransferBanksResponse.class)
                        .getBody();

        return Optional.ofNullable(bodyEntity.getTransferBanks()).orElseGet(Collections::emptyList);
    }

    public Optional<String> fetchPaymentDestinationName(String giroNumber) {
        PaymentNameBodyEntity bodyEntity =
                createRequest(
                                IcaBankenConstants.Urls.GIRO_DESTINATION_NAME.parameter(
                                        IcaBankenConstants.IdTags.GIRO_NUMBER_TAG, giroNumber))
                        .get(PaymentNameResponse.class)
                        .getBody();

        return Optional.ofNullable(bodyEntity.getName());
    }

    public void saveNewRecipient(RecipientEntity recipientEntity) {
        createPostRequest(IcaBankenConstants.Urls.TRANSFER_DESTINATIONS)
                .post(RecipientsResponse.class, recipientEntity);
    }
}
