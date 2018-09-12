package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.entities.SignBundleResponseBody;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.entities.SignedAssignmentList;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.entities.AcceptEInvoiceTransferRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.entities.EInvoiceBody;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.entities.PaymentNameBody;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.entities.UpdateEInvoiceRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.entities.ValidateEInvoiceRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.entities.ValidateEInvoiceResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.investment.entities.InstrumentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.investment.entities.InvestmentAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.entities.LoanListEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.entities.LoanOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.entities.MortgageListEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.AssignmentsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.AssignmentsResponseBody;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.BanksResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.rpc.BankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.AccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.TransactionsBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.UpcomingTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.rpc.UpcomingTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.RecipientEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.RecipientsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.TransferRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.TransferResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.storage.IcaBankenSessionStorage;
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

    public IcaBankenApiClient(TinkHttpClient client, IcaBankenSessionStorage icaBankenSessionStorage) {
        this.client = client;
        this.icaBankenSessionStorage = icaBankenSessionStorage;
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

        return request;
    }

    public BankIdResponse initBankIdEInvoice(String invoiceId) {
    public String initBankId(String ssn) {
        return createPostRequest(
                IcaBankenConstants.Urls.INIT_EINVOICE_SIGN_URL.parameter(IcaBankenConstants.IdTags.INVOICE_ID_TAG,
                        invoiceId)).post(BankIdResponse.class);
                IcaBankenConstants.Urls.LOGIN_BANKID.parameter(IcaBankenConstants.IdTags.IDENTIFIER_TAG, ssn))
                .post(BankIdResponse.class)
                .getBody()
                .getRequestId();
    }

    public BankIdResponse initBankIdTransfer() {
        return createPostRequest(IcaBankenConstants.Urls.INIT_TRANSFER_SIGN_URL).post(BankIdResponse.class);
    }

    public SignedAssignmentList getSignedAssignmentsList(String requestId) {
        SignBundleResponseBody assignmentRequest = createPostRequest(
                IcaBankenConstants.Urls.SIGNED_ASSIGNMENTS_URL.queryParam(IcaBankenConstants.IdTags.REQUEST_ID_TAG,
                        requestId)).post(SignBundleResponseBody.class);
        return assignmentRequest.getSignedAssignmentList();
    }

    public BankIdResponse pollBankId(String reference) {
        return createRequest(IcaBankenConstants.Urls.LOGIN_BANKID.parameter(
                IcaBankenConstants.IdTags.IDENTIFIER_TAG, reference))
                .get(BankIdResponse.class);
    }

    public BankIdResponse sign(String requestId) {
        return createRequest(
                IcaBankenConstants.Urls.SIGN_TRANSFER_COLLECT_URL.parameter(IcaBankenConstants.IdTags.REQUEST_ID_TAG,
                        requestId)).get(BankIdResponse.class);
    }

    public AccountsEntity fetchAccounts() {
        return createRequest(IcaBankenConstants.Urls.ACCOUNTS)
                .get(AccountsResponse.class)
                .getBody().getAccounts();
    }

    public TransferResponse redoTransferWithValidDate(TransferRequest transferRequest,
            TransferResponse transferResponse) {
        transferRequest.setDueDate(transferResponse.getBody().getProposedNewDate());
        return createPostRequest(IcaBankenConstants.Urls.UNSIGNED_ASSIGNMENTS_URL).post(TransferResponse.class,
                transferRequest);
    public TransactionsBodyEntity fetchTransactionsWithDate(Account account, Date toDate) {
        return createRequest(IcaBankenConstants.Urls.TRANSACTIONS.parameter(IcaBankenConstants.IdTags.IDENTIFIER_TAG,
                account.getBankIdentifier())
                .queryParam(IcaBankenConstants.IdTags.TO_DATE_TAG,
                        ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate)))
                .get(TransactionsResponse.class)
                .getBody();
    }

    public void deleteUnsignedTransfer(AssignmentsResponseBody responseBody) {
        // Should not be more than one transfer to cancel.
        if (responseBody == null || responseBody.getAssignments() == null) {
            return;
        }

        int assignments = responseBody.getAssignments().size();
        if (assignments > 1) {
            log.warn("Unexpected size of list of assignments. Expected 1 - Real size %s", assignments);
        }
    public TransactionsBodyEntity fetchTransactions(Account account) {
        return createRequest(IcaBankenConstants.Urls.TRANSACTIONS.parameter(
                IcaBankenConstants.IdTags.IDENTIFIER_TAG, account.getBankIdentifier()))
                .get(TransactionsResponse.class)
                .getBody();
    }

        String transferId = responseBody.getAssignments()
                .stream()
                .findFirst()
                .orElseThrow(IllegalStateException::new)
                .getRegistrationId();
    public TransactionsBodyEntity fetchReservedTransactions(Account account) {
        return createRequest(
                IcaBankenConstants.Urls.RESERVED_TRANSACTIONS.parameter(IcaBankenConstants.IdTags.IDENTIFIER_TAG,
                        account.getBankIdentifier()))
                .get(TransactionsResponse.class)
                .getBody();

        createRequest(IcaBankenConstants.Urls.DELETE_UNSIGNED_TRANSFER_URL.parameter(
                IcaBankenConstants.IdTags.TRANSFER_ID_TAG, transferId)).delete();
    }

    public List<UpcomingTransactionEntity> fetchUpcomingTransactions() {
        return createRequest(IcaBankenConstants.Urls.UPCOMING_TRANSACTIONS)
                .get(UpcomingTransactionsResponse.class)
                .getBody()
                .getUpcomingTransactions();
    }

                .getBody();
    }

    }



    public void keepAlive() {
        createRequest(IcaBankenConstants.Urls.KEEP_ALIVE).get(HttpResponse.class);
    }

    public void acceptEInvoiceTransfer(String accountId, String invoiceId) {
        AcceptEInvoiceTransferRequest request = new AcceptEInvoiceTransferRequest();
        request.setDebitAccountId(accountId);
        request.setInvoiceId(invoiceId);

        createPostRequest(IcaBankenConstants.Urls.ACCEPT_EINVOICE_URL).post(ClientResponse.class, request).close();
    }

    public List<RecipientEntity> fetchDestinationAccounts() {
        return createRequest(IcaBankenConstants.Urls.TRANSFER_DESTINATIONS_URL).get(RecipientsResponse.class)
                .getBody()
                .getRecipients();
    }

    public BanksResponse getBanksResponse() {
        return createRequest(IcaBankenConstants.Urls.TRANSFER_BANKS_URL).type(MediaType.APPLICATION_JSON)
                .get(BanksResponse.class);
    }

    public TransferResponse makeTransferRequest(TransferRequest transferRequest) {
        TransferResponse transferResponse = new TransferResponse();
        try {
            transferResponse = createPostRequest(IcaBankenConstants.Urls.UNSIGNED_ASSIGNMENTS_URL).post(
                    TransferResponse.class, transferRequest);
        } catch (HttpResponseException e) {

            if (e.getResponse().getStatus() == 409) {
                transferResponse = e.getResponse().getBody(TransferResponse.class);

                if (transferResponse.getBody().getProposedNewDate() != null) {
                    transferResponse = redoTransferWithValidDate(transferRequest, transferResponse);
                }
            }
        } catch (Exception e) {
        }
        return transferResponse;
    }

    public void TransferDestinationRecipient(RecipientEntity recipientEntity) {
        createPostRequest(IcaBankenConstants.Urls.TRANSFER_DESTINATIONS_URL).post(RecipientsResponse.class,
                recipientEntity);
    }

    public AssignmentsResponse fetchUnsignedTransfers() {
        return createRequest(IcaBankenConstants.Urls.UNSIGNED_TRANSFERS_URL).type(MediaType.APPLICATION_JSON)
                .get(AssignmentsResponse.class);
    }

    public InvestmentAccountResponse getInvestments() {
        return createRequest(IcaBankenConstants.Urls.INVESTMENTS).get(InvestmentAccountResponse.class);
    }

    public InstrumentResponse getInstrument(String fundId) {
        return createRequest(
                IcaBankenConstants.Urls.INSTRUMENT.parameter(IcaBankenConstants.IdTags.FUND_ID_TAG, fundId)).get(
                InstrumentResponse.class);
    }

    }

    public Optional<String> fetchDestinationNameFor(String pgNumber) {
        PaymentNameBody paymentNameResponse = createRequest(
                IcaBankenConstants.Urls.GIRO_DESTINATION_NAME.parameter(IcaBankenConstants.IdTags.GIRO_NUMBER_TAG,
                        pgNumber)).type(MediaType.APPLICATION_JSON).get(PaymentNameBody.class);

        return Optional.ofNullable(paymentNameResponse.getName());
    }

    public ValidateEInvoiceResponse validateEInvoice(ValidateEInvoiceRequest validateRequest) {
        ClientResponse response = createPostRequest(IcaBankenConstants.Urls.VALIDATE_INVOICE_URL).type(
                MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class, validateRequest);
        try {
            return response.getEntity(ValidateEInvoiceResponse.class);
        } finally {
            response.close();
        }
    }

    public EInvoiceBody fetchEInvoices() {
        return createRequest(IcaBankenConstants.Urls.EINVOICES_URL).get(EInvoiceBody.class);
    }

    public ValidateEInvoiceResponse updateEInvoice(UpdateEInvoiceRequest updateEInvoiceRequest) {
        return createRequest(IcaBankenConstants.Urls.UPDATE_INVOICE_URL).put(ValidateEInvoiceResponse.class,
                updateEInvoiceRequest);
    }

    public LoanListEntity fetchLoanOverview() {
        return createRequest(IcaBankenConstants.Urls.LOAN_OVERVIEW).get(LoanOverviewResponse.class).getBody().getLoanList();
    }

    public MortgageListEntity fetchMortgages() {
        return createRequest(IcaBankenConstants.Urls.MORTGAGES_URL).get(LoanOverviewResponse.class).getBody().getMortgageList();
    }

}
