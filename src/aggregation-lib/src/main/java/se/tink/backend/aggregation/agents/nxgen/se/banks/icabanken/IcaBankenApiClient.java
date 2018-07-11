package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken;

import com.sun.jersey.api.client.ClientResponse;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.rpc.bankid.BankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.entities.SignBundleResponseBody;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.entities.SignedAssignmentList;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.Accounts.entities.AccountBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.Accounts.entities.AccountFetcherRoot;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.entities.AcceptEInvoiceTransferRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.entities.EInvoiceBody;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.entities.PaymentNameBody;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.entities.UpdateEInvoiceRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.entities.ValidateEInvoiceRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.entities.ValidateEInvoiceResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.investment.entities.InstrumentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.investment.entities.InvestmentAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transactions.entities.RootTransactionModel;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transactions.entities.UpcomingTransactionsBody;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transactions.entities.UpcomingTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.AssignmentsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.AssignmentsResponseBody;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.BanksResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.RecipientEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.RecipientsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.TransferRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.TransferResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class IcaBankenApiClient {

    private final TinkHttpClient client;
    private final Logger log = LoggerFactory.getLogger(IcaBankenApiClient.class);

    public IcaBankenApiClient(TinkHttpClient client) {
        this.client = client;
    }

    protected RequestBuilder createPostRequest(URL url) {
        return createRequest(url).type(MediaType.APPLICATION_JSON);
    }

    protected RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)
                .header(IcaBankenConstants.Headers.HEADER_APIKEY, IcaBankenConstants.Headers.VALUE_APIKEY)
                .header(IcaBankenConstants.Headers.HEADER_USERAGENT, IcaBankenConstants.Headers.VALUE_USERAGENT)
                .header(IcaBankenConstants.Headers.HEADER_CLIENTAPPVERSION,
                        IcaBankenConstants.Headers.VALUE_CLIENTAPPVERSION);
    }

    public BankIdResponse initBankId(String ssn) {
        return createPostRequest(
                IcaBankenConstants.Urls.LOGIN_BANKID.parameter(IcaBankenConstants.IdTags.IDENTIFIER_TAG, ssn)).post(
                BankIdResponse.class);
    }

    public BankIdResponse initBankIdEInvoice(String invoiceId) {
        return createPostRequest(
                IcaBankenConstants.Urls.INIT_EINVOICE_SIGN_URL.parameter(IcaBankenConstants.IdTags.INVOICE_ID_TAG,
                        invoiceId)).post(BankIdResponse.class);
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

    public BankIdResponse authenticate(String reference) {
        return createRequest(IcaBankenConstants.Urls.LOGIN_BANKID.parameter(IcaBankenConstants.IdTags.IDENTIFIER_TAG,
                reference)).get(BankIdResponse.class);
    }

    public BankIdResponse sign(String requestId) {
        return createRequest(
                IcaBankenConstants.Urls.SIGN_TRANSFER_COLLECT_URL.parameter(IcaBankenConstants.IdTags.REQUEST_ID_TAG,
                        requestId)).get(BankIdResponse.class);
    }

    public AccountBodyEntity requestAccountsBody() {
        return createRequest(IcaBankenConstants.Urls.ACCOUNTS).get(AccountFetcherRoot.class).getBody();
    }

    public TransferResponse redoTransferWithValidDate(TransferRequest transferRequest,
            TransferResponse transferResponse) {
        transferRequest.setDueDate(transferResponse.getBody().getProposedNewDate());
        return createPostRequest(IcaBankenConstants.Urls.UNSIGNED_ASSIGNMENTS_URL).post(TransferResponse.class,
                transferRequest);
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

        String transferId = responseBody.getAssignments()
                .stream()
                .findFirst()
                .orElseThrow(IllegalStateException::new)
                .getRegistrationId();

        createRequest(IcaBankenConstants.Urls.DELETE_UNSIGNED_TRANSFER_URL.parameter(
                IcaBankenConstants.IdTags.TRANSFER_ID_TAG, transferId)).delete();
    }

    public RootTransactionModel fetchTransactions(String accountNumber, String fromDate, String toDate) {
        URL request = IcaBankenConstants.Urls.TRANSACTIONS.parameter(IcaBankenConstants.IdTags.IDENTIFIER_TAG,
                accountNumber)
                .queryParam(IcaBankenConstants.IdTags.FROM_DATE_TAG, fromDate)
                .queryParam(IcaBankenConstants.IdTags.TO_DATE_TAG, toDate);
        return postTransactionRequest(request);
    }

    public UpcomingTransactionsBody fetchUpcomingTransactions() {
        return createRequest(IcaBankenConstants.Urls.UPCOMING_TRANSACTIONS_URL).get(UpcomingTransactionsResponse.class)
                .getBody();
    }

    public RootTransactionModel fetchReservedTransactions(String accountNumber) {
        URL request = IcaBankenConstants.Urls.RESERVED_TRANSACTIONS.parameter(IcaBankenConstants.IdTags.IDENTIFIER_TAG,
                accountNumber);
        return postTransactionRequest(request);
    }

    public RootTransactionModel postTransactionRequest(URL request) {

        try {
            return createPostRequest(request).get(RootTransactionModel.class);
        } catch (HttpResponseException httpException) {

            return null;
        }
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

    public boolean keepAlive() {
        return createRequest(IcaBankenConstants.Urls.HEARTBEAT).get(BankIdResponse.class).getResponseStatus().getCode()
                == 0;
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
}
