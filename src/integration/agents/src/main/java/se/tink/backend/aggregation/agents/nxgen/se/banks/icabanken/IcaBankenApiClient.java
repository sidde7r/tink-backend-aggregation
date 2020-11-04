package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants.Error;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants.Headers;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.entities.BankIdAuthInitBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.entities.BankIdAuthPollBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.entities.BankIdBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.entities.EvaluatedPoliciesBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.entities.SessionBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.rpc.BankIdAuthPollResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.rpc.BankIdAuthRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.rpc.BankIdAuthResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.rpc.BankIdInitRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.rpc.BankIdInitResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.rpc.BankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.rpc.EvaluatedPoliciesResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.entities.ResponseStatusEntity;
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
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.identitydata.entities.CustomerBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.identitydata.rpc.CustomerResponse;
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
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.filter.filters.ServiceUnavailableBankServiceErrorFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class IcaBankenApiClient {
    private final TinkHttpClient client;
    private final IcaBankenSessionStorage icaBankenSessionStorage;
    private final IcabankenPersistentStorage icabankenPersistentStorage;

    private AccountsEntity cachedAccounts;
    private CustomerBodyEntity cachedCustomer;

    public IcaBankenApiClient(
            TinkHttpClient client,
            IcaBankenSessionStorage icaBankenSessionStorage,
            IcabankenPersistentStorage icabankenPersistentStorage) {
        this.client = client;
        this.icaBankenSessionStorage = icaBankenSessionStorage;
        this.icabankenPersistentStorage = icabankenPersistentStorage;
        client.setUserAgent(Headers.VALUE_USER_AGENT);

        this.client.addFilter(new ServiceUnavailableBankServiceErrorFilter());
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

    public BankIdAuthInitBodyEntity initBankId(String ssn) {
        return createPostRequest(Urls.BANKID_CREATE)
                .post(BankIdInitResponse.class, new BankIdInitRequest(ssn))
                .getBody();
    }

    public BankIdBodyEntity initTransferSign() {
        return createPostRequest(IcaBankenConstants.Urls.INIT_TRANSFER_SIGN)
                .post(BankIdResponse.class)
                .getBody();
    }

    public BankIdAuthPollBodyEntity pollBankId(String reference) {
        return createRequest(
                        Urls.BANKID_COLLECT.parameter(
                                IcaBankenConstants.IdTags.IDENTIFIER_TAG, reference))
                .get(BankIdAuthPollResponse.class)
                .getBody();
    }

    public BankIdResponse pollTransferBankId(String requestId) {
        return createRequest(
                        IcaBankenConstants.Urls.SIGN_TRANSFER_COLLECT_URL.parameter(
                                IcaBankenConstants.IdTags.REQUEST_ID_TAG, requestId))
                .get(BankIdResponse.class);
    }

    public SessionBodyEntity authenticateBankId(String reference) {
        return createPostRequest(Urls.BANKID_AUTH)
                .post(BankIdAuthResponse.class, new BankIdAuthRequest(reference))
                .getBody();
    }

    public AccountsEntity fetchAccounts() {
        if (cachedAccounts == null) {
            cachedAccounts =
                    createRequest(IcaBankenConstants.Urls.ACCOUNTS)
                            .get(AccountsResponse.class)
                            .getBody()
                            .getAccounts();
        }

        return cachedAccounts;
    }

    public EvaluatedPoliciesBodyEntity fetchPolicies() {
        return createRequest(Urls.EVALUATED_POLICIES)
                .get(EvaluatedPoliciesResponse.class)
                .getBody();
    }

    public CustomerBodyEntity fetchCustomer() {
        if (cachedCustomer == null) {
            cachedCustomer =
                    createRequest(IcaBankenConstants.Urls.CUSTOMER)
                            .get(CustomerResponse.class)
                            .getBody();
        }
        return cachedCustomer;
    }

    public TransactionsBodyEntity fetchTransactionsWithDate(
            Account account, LocalDate fromDate, LocalDate toDate) {
        try {
            return createRequest(
                            IcaBankenConstants.Urls.TRANSACTIONS
                                    .parameter(
                                            IcaBankenConstants.IdTags.IDENTIFIER_TAG,
                                            account.getApiIdentifier())
                                    .queryParam(
                                            IcaBankenConstants.IdTags.FROM_DATE_TAG,
                                            fromDate.toString())
                                    .queryParam(
                                            IcaBankenConstants.IdTags.TO_DATE_TAG,
                                            toDate.toString()))
                    .get(TransactionsResponse.class)
                    .getBody();
        } catch (HttpResponseException hre) {
            if (hre.getResponse().getStatus() != HttpStatus.SC_FORBIDDEN) {
                throw hre;
            }

            ResponseStatusEntity error = hre.getResponse().getBody(ResponseStatusEntity.class);
            if (error.getCode() == Error.MULTIPLE_LOGIN_ERROR_CODE) {
                throw BankServiceError.MULTIPLE_LOGIN.exception(hre);
            }

            throw hre;
        }
    }

    public TransactionsBodyEntity fetchTransactions(Account account) {
        return createRequest(
                        IcaBankenConstants.Urls.TRANSACTIONS.parameter(
                                IcaBankenConstants.IdTags.IDENTIFIER_TAG,
                                account.getApiIdentifier()))
                .get(TransactionsResponse.class)
                .getBody();
    }

    public TransactionsBodyEntity fetchReservedTransactions(Account account) {
        return createRequest(
                        IcaBankenConstants.Urls.RESERVED_TRANSACTIONS.parameter(
                                IcaBankenConstants.IdTags.IDENTIFIER_TAG,
                                account.getApiIdentifier()))
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
