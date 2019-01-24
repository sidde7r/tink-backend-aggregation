package se.tink.backend.aggregation.agents.banks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.HttpStatusCodes;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource.Builder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.client.utils.URIBuilder;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.PersistentLogin;
import se.tink.backend.aggregation.agents.RefreshableItemExecutor;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.TransferExecutor;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.LFUtils;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.Session;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.errors.HttpStatusCodeErrorException;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.AccountEntity;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.BankEntity;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.BankIdAuthenticationRequest;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.BankIdLoginRequest;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.BankIdLoginResponse;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.BankListResponse;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.CancelPaymentRequest;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.CardEntity;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.CardTransactionEntity;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.CashBalanceResponse;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.CreditTransactionListResponse;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.DebitTransactionListResponse;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.DeleteSignedTransactionRequest;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.EInvoice;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.EInvoiceAndCreditAlertsResponse;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.EInvoicePaymentRequest;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.EInvoicesListResponse;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.FundHoldingsResponse;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.FundInformationWrapper;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.InstrumentDetailsResponse;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.InvestmentSavingsDepotEntity;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.InvestmentSavingsDepotResponse;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.InvestmentSavingsDepotWrappersEntity;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.ListAccountTransactionRequest;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.ListCardTransactionRequest;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.ListUpcomingTransactionRequest;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.LoanDetailsEntity;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.LoanDetailsRequest;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.LoanEntity;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.LoanListResponse;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.LoginResponse;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.OverviewEntity;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.PaymentAccountsResponse;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.PaymentEntity;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.PaymentRequest;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.PaymentsListResponse;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.RecipientEntity;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.RecipientRequest;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.RecipientsResponse;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.SecurityHoldingsResponse;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.ShareDepotResponse;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.TokenChallengeRequest;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.TokenChallengeResponse;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.TokenResponse;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.TransactionEntity;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.TransferRequest;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.TransferrableResponse;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.UpcomingTransactionEntity;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.UpcomingTransactionListResponse;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.UpdatePaymentRequest;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.general.GeneralUtils;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.http.filter.ClientFilterFactory;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.aggregation.rpc.RefreshableItem;
import se.tink.backend.aggregation.utils.transfer.StringNormalizerSwedish;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.backend.aggregation.utils.transfer.TransferMessageLengthConfig;
import se.tink.backend.core.account.TransferDestinationPattern;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.core.transfer.TransferPayloadType;
import se.tink.backend.system.rpc.AccountFeatures;
import se.tink.backend.system.rpc.Instrument;
import se.tink.backend.system.rpc.Loan;
import se.tink.backend.system.rpc.Portfolio;
import se.tink.backend.system.rpc.Transaction;
import se.tink.backend.system.rpc.TransactionPayloadTypes;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.se.ClearingNumber;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableEnum;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.net.TinkApacheHttpClient4;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.strings.StringUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class LansforsakringarAgent extends AbstractAgent implements RefreshableItemExecutor, TransferExecutor,
        PersistentLogin {
    private static final DefaultAccountIdentifierFormatter DEFAULT_FORMATTER = new DefaultAccountIdentifierFormatter();
    private static final DisplayAccountIdentifierFormatter GIRO_FORMATTER = new DisplayAccountIdentifierFormatter();
    private static final TypeReference<HashMap<String, Object>> TYPE_MAP_REF = new TypeReference<HashMap<String, Object>>() {
    };
    private static final TransferMessageLengthConfig TRANSFER_MESSAGE_LENGTH_CONFIG = TransferMessageLengthConfig
            .createWithMaxLength(30, 14);
    private static final int MAX_ATTEMPTS = 80;

    public enum SavingsAccountTypes {
        fixedrate,
        county,
        saving
    }

    private static final String BASE_URL = "https://mobil.lansforsakringar.se/appoutlet";
    private static final String EINVOICE_DESCRIPTION = "ElectronicInvoice";
    private static final String OVERVIEW_URL = BASE_URL + "/overview";
    private static final String PASSWORD_LOGIN_URL = BASE_URL + "/security/user";
    private static final String BANKID_AUTHENTICATE_URL = BASE_URL + "/security/user/bankid/authenticate";
    private static final String BANKID_COLLECT_URL = BASE_URL + "/security/user/bankid/login/2.0";
    private static final String BANKID_COLLECT_DIRECT_TRANSFER_URL = BASE_URL + "/directtransfer/bankid";
    private static final String CREATE_BANKID_REFERENCE_URL = BASE_URL + "/directtransfer/createbankidreference";
    private static final String CREATE_PAYMENT_URL = BASE_URL + "/directpayment/createreference/bankid";
    private static final String SAVED_RECIPIENTS_URL = BASE_URL + "/payment/savedrecipients";
    private static final String PAYMENT_ACCOUNTS_URL = BASE_URL + "/payment/paymentaccount";
    private static final String SEND_PAYMENT_URL = BASE_URL + "/directpayment/send/bankid";
    private static final String FETCH_EINVOICES_URL = BASE_URL + "/payment/einvoice/einvoiceandcredit";
    private static final String TRANSFER_DESTINATIONS_URL = BASE_URL + "/account/transferrablewithsavedrecipients";
    private static final String EINVOICE_AND_CREDIT_ALERTS_URL = BASE_URL + "/payment/einvoice/einvoiceandcreditalerts";
    private static final String TRANSACTIONS_URL = BASE_URL + "/account/transaction/2.0";
    private static final String VALIDATE_PAYMENT_URL = BASE_URL + "/directpayment/validate";
    private static final String RECIPIENT_NAME_URL = BASE_URL + "/payment/recipientname";
    private static final String LIST_UNSIGNED_TRANSFERS_AND_PAYMENTS_URL = BASE_URL + "/unsigned/paymentsandtransfers/list/2.0";
    private static final String DELETE_SIGNED_TRANSACTION_URL = BASE_URL + "/payment/signed/delete";
    private static final String FETCH_TOKEN_URL = BASE_URL + "/security/client";
    private static final String FETCH_UPCOMING_TRANSACTIONS_URL = BASE_URL + "/account/upcoming/5.0";
    private static final String FETCH_CARD_TRANSACTIONS_URL = BASE_URL + "/card/transaction";
    private static final String FETCH_LOANS_URL = BASE_URL + "/loan/loans/withtotal";
    private static final String FETCH_LOAN_DETAILS_URL = BASE_URL + "/loan/details";
    private static final String FETCH_UNSIGNED_PAYMENTS_URL = BASE_URL + "/unsigned/paymentsandtransfers/list";
    private static final String VALIDATE_UNSIGNED_PAYMENTS_URL = BASE_URL + "/unsigned/paymentsandtransfers/validate";
    private static final String CREATE_BANKID_REFERENCE_PAYMENTS_URL = BASE_URL + "/unsigned/paymentsandtransfers/bankid/createreference";
    private static final String SEND_UNSIGNED_PAYMENT_URL = BASE_URL + "/unsigned/paymentsandtransfers/bankid/send";
    private static final String DELETE_UNSIGNED_PAYMENT_URL = BASE_URL + "/payment/deleteunsigned";
    private static final String SIGNLIST_ADD_EINVOICE_URL = BASE_URL + "/payment/einvoice/addeinvoice";
    private static final String SIGN_PAYMENT_CREATE_REFERENCE_URL = BASE_URL + "/payment/signed/bankid/createreference";
    private static final String SIGN_PAYMENT_SEND_PAYMENT_URL = BASE_URL + "/payment/signed/modify/bankid";
    private static final String FETCH_TRANSFER_SOURCE_ACCOUNTS = BASE_URL + "/account/transferrable?direction=from";
    private static final String INTERNAL_TRANSFER_URL = BASE_URL + "/directtransfer";
    private static final String FETCH_ALL_BANKNAMES_URL = BASE_URL + "/directtransfer/fetchallbanknames";
    private static final String ISK_URL = BASE_URL + "/depot/investmentsavings/3.0";
    private static final String FUND_SECURITIES_URL = BASE_URL + "/depot/holding/fund/securityholdings/withdetails/2.0";
    private static final String STOCK_SECURITIES_URL = BASE_URL + "/depot/holding/share/securityholdings/2.0";
    private static final String STOCK_DETAILS_URL = BASE_URL + "/depot/trading/share/instrumentwithisin";
    private static final String FUND_DEPOT_URL = BASE_URL + "/fund/holdings";
    private static final String FUND_INFORMATION_URL = BASE_URL + "/fund/fundinformation";
    private static final String STOCK_DEPOT_URL = BASE_URL + "/depot/share";
    private static final String STOCK_DEPOT_HOLDINGS_URL = BASE_URL + "/depot/holding/share/securityholdings";
    private static final String STOCK_DEPOT_CASH_BALANCE_URL = BASE_URL + "/depot/holding/depotcashbalance";
    private static final String ISK_CASH_BALANCE_URL = BASE_URL + "/depot/holding/depotcashbalance/2.0";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static Optional<BankEntity> findBankForAccountNumber(String destinationAccount, List<BankEntity> banks) {
        final Integer accountClearingNumber = Integer.parseInt(destinationAccount.substring(0, 4));

        return banks.stream().filter(
                be -> be.getFromClearingRange() <= accountClearingNumber && accountClearingNumber <= be
                        .getToClearingRange())
                .findFirst();
    }

    private final Catalog catalog;
    private final TinkApacheHttpClient4 client;
    private final Credentials credentials;
    private final String deviceId;
    private final TransferMessageFormatter transferMessageFormatter;
    private String ticket = null;
    private String token = null;

    // cache
    private Map<AccountEntity, Account> accounts = null;

    public LansforsakringarAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context);

        catalog = context.getCatalog();
        credentials = request.getCredentials();
        client = clientFactory.createCustomClient(context.getLogOutputStream());
        // client.addFilter(new LoggingFilter(new PrintStream(System.out)));

        deviceId = new String(Hex.encodeHex(StringUtils.hashSHA1(credentials.getField(Field.Key.USERNAME) + "-TINK")));
        transferMessageFormatter = new TransferMessageFormatter(
                catalog, TRANSFER_MESSAGE_LENGTH_CONFIG, new StringNormalizerSwedish(",.-_"));
    }

    private LoginResponse authenticateBankId() throws AuthorizationException, AuthenticationException {
        ClientResponse bankIdloginClientResponse = createPostRequest(BANKID_AUTHENTICATE_URL,
                new BankIdAuthenticationRequest(credentials.getField(Field.Key.USERNAME)));

        int status = bankIdloginClientResponse.getStatus();

        if (status != Status.OK.getStatusCode()) {
            String errorCode = bankIdloginClientResponse.getHeaders().getFirst("Error-Code");
            String errorMessage = bankIdloginClientResponse.getHeaders().getFirst("Error-Message");

            switch (errorCode) {
            case "000114":
                throw BankIdError.ALREADY_IN_PROGRESS.exception();
            case "99312":
                // Error-Message: Du behöver vara över 16 år för att använda Mobilt BankID. Kontakta gärna oss om du har frågor.
                throw AuthorizationError.UNAUTHORIZED.exception(UserMessage.UNDERAGE.getKey());
            case "99021":
                // Error-Message: Dina inloggningsuppgifter stämmer inte. Kontrollera dem och försök igen. Kontakta oss om problemet kvarstår.
                // This occurs if the user enters the wrong personal number, e.g. 188705030142
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            default:
                // TODO: If we get this often and the fault is on their side, do an implementation which handles that specific error.
                // Known errors:
                // - 00012 "Tyvärr har det uppstått ett tekniskt fel. Försök igen och kontakta oss om problemet kvarstår."
                // - 00019 "Tyvärr har det uppstått ett tekniskt fel. Försök igen och kontakta oss om problemet kvarstår."
                throw new IllegalStateException(
                        String.format(
                                "#login-refactoring - LF - Login failed with errorCode: %s, errorMessage: %s",
                                errorCode,
                                errorMessage
                        )
                );
            }
        }

        BankIdLoginResponse bankIdloginResponse = bankIdloginClientResponse.getEntity(BankIdLoginResponse.class);

        credentials.setSupplementalInformation(null);
        credentials.setStatus(CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION);

        context.requestSupplementalInformation(credentials, false);

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            ClientResponse clientLoginResponse = createPostRequest(BANKID_COLLECT_URL,
                    new BankIdLoginRequest(bankIdloginResponse.getReference(),
                            credentials.getField(Field.Key.USERNAME)));

            status = clientLoginResponse.getStatus();

            if (status == Status.OK.getStatusCode()) {
                return clientLoginResponse.getEntity(LoginResponse.class);
            } else if (status == Status.UNAUTHORIZED.getStatusCode() || status == Status.BAD_REQUEST.getStatusCode()) {
                switch (clientLoginResponse.getHeaders().getFirst("Error-Code")) {
                case "00013":
                case "00014":
                    log.info("Waiting for Mobilt BankID authentication ("
                            + clientLoginResponse.getHeaders().getFirst("Error-Message") + ")");
                    break;
                case "000114":
                    throw BankIdError.ALREADY_IN_PROGRESS.exception();
                case "00011":
                    throw BankIdError.NO_CLIENT.exception();
                case "00015":
                    // Cancelled from BankId due to multiple BankId logins - fall trough
                case "000115":
                    throw BankIdError.CANCELLED.exception();
                case "1011":
                    // errorMessage:
                    // Välkommen till mobilbanken. Du aktiverar appen via Länsförsäkringars internetbank.
                    // Logga in på lansforsakringar.se och godkänn våra allmänna internetvillkor.
                    throw LoginError.NOT_CUSTOMER.exception();
                default:
                    // NOP
                }
            }
            Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
        }

        throw BankIdError.TIMEOUT.exception();
    }

    private Builder createClientRequest(String url) {
        Builder request = client.resource(url).accept(MediaType.APPLICATION_JSON);

        if (token != null) {
            request = request.header("ctoken", token);
        }

        if (ticket != null) {
            request = request.header("utoken", ticket);
        }

        if (deviceId != null) {
            request = request.header("DeviceId", deviceId);
        }

        return request;
    }

    private <T> T createPostRequestWithResponseHandling(String url, Class<T> returnClass, Object requestEntity)
            throws HttpStatusCodeErrorException {
        ClientResponse response = createPostRequest(url, requestEntity);
        return handleRequestResponse(response, returnClass);
    }

    private ClientResponse createPostRequest(String url, Object requestEntity) {
        Builder request = createClientRequest(url);
        request.type(MediaType.APPLICATION_JSON);
        return request.post(ClientResponse.class, requestEntity);
    }

    private <T> T createGetRequest(String url, Class<T> returnClass) throws HttpStatusCodeErrorException {
        Builder request = createClientRequest(url);
        ClientResponse response = request.get(ClientResponse.class);
        return handleRequestResponse(response, returnClass);
    }

    private ClientResponse createGetRequest(String url) {
        Builder request = createClientRequest(url);
        return request.get(ClientResponse.class);
    }

    private <T> T handleRequestResponse(ClientResponse response, Class<T> returnClass)
            throws HttpStatusCodeErrorException {
        if (response.getStatus() == 200) {
            return response.getEntity(returnClass);
        } else {
            String errorMsg = response.getHeaders().getFirst("Error-Message");
            throw new HttpStatusCodeErrorException(response,
                    "Request status code " + response.getStatus() + ": '" + errorMsg + "'");
        }
    }

    private void debug(CredentialsRequest request, String message) {
        if (request.getCredentials().isDebug()) {
            log.info(message);
        }
    }

    private Map<Account, List<TransferDestinationPattern>> getPaymentAccountDestinations(
            List<AccountEntity> accountEntities, List<Account> accounts) throws HttpStatusCodeErrorException {
        if (accountEntities.isEmpty()) {
            log.info("Payment accounts not updated: The user has no accounts.");
            return Collections.emptyMap();
        }

        List<AccountEntity> paymentAccounts = fetchPaymentAccounts();

        RecipientsResponse recipientsResponse = createGetRequest(SAVED_RECIPIENTS_URL,
                RecipientsResponse.class);

        return new TransferDestinationPatternBuilder()
                .setSourceAccounts(paymentAccounts)
                .setDestinationAccounts(recipientsResponse.getRecipients())
                .setTinkAccounts(accounts)
                .addMultiMatchPattern(AccountIdentifier.Type.SE_BG, TransferDestinationPattern.ALL)
                .addMultiMatchPattern(AccountIdentifier.Type.SE_PG, TransferDestinationPattern.ALL)
                .build();
    }

    /**
     * Helper that gracefully returns empty account list if the user doesn't have this service.
     * @return Non-null list of account entities
     * @throws HttpStatusCodeErrorException If the client threw from bad status code others than "missing service"
     */
    private List<AccountEntity> fetchPaymentAccounts() throws HttpStatusCodeErrorException {
        try {
            PaymentAccountsResponse paymentAccounts = createGetRequest(PAYMENT_ACCOUNTS_URL,
                    PaymentAccountsResponse.class);

            return paymentAccounts.getPaymentAccounts();
        } catch (HttpStatusCodeErrorException e) {
            // Error-Message: "Du saknar den här tjänsten. Kontakta oss för att få hjälp att komma igång."
            if (e.hasErrorCode(12051)) {
                log.info(e.getMessage());
                return Lists.newArrayList();
            }

            throw e;
        }
    }

    private Map<Account, List<TransferDestinationPattern>> getTransferAccountDestinations(
            List<AccountEntity> accountEntities, List<Account> updatedAccounts) throws HttpStatusCodeErrorException {
        if (accountEntities.isEmpty()) {
            log.info("Transfer accounts not updated: The user has no accounts.");
            return Collections.emptyMap();
        }

        TransferrableResponse toResponse = fetchTransferDestinationAccounts();

        if (toResponse == null) {
            return Collections.emptyMap();
        }

        TransferrableResponse fromResponse = fetchTransferSourceAccounts();

        return new TransferDestinationPatternBuilder()
                .setSourceAccounts(fromResponse.getAccounts())
                .setDestinationAccounts(toResponse.getAccounts())
                .setTinkAccounts(updatedAccounts)
                .addMultiMatchPattern(AccountIdentifier.Type.SE, TransferDestinationPattern.ALL)
                .build();
    }

    private void updateEInvoices() {
        try {
            List<AccountEntity> accountEntities = fetchAccountEntities();

            if (accountEntities.isEmpty()) {
                log.info("EInvoices not updated: The user has no accounts.");
                return;
            }

            if (!hasPendingEInvoices()) {
                return;
            }

            EInvoicesListResponse invoicesListResponse = createGetRequest(FETCH_EINVOICES_URL,
                    EInvoicesListResponse.class);

            List<EInvoice> eInvoiceEntities = invoicesListResponse.getElectronicInvoices();
            if (eInvoiceEntities.isEmpty()) {
                log.error("User should have eInvoices, but we got no transfers.");
                return;
            }

            List<Transfer> eInvoices = Lists.newArrayList();
            for (EInvoice eInvoice : eInvoiceEntities) {
                eInvoices.add(eInvoice.toTransfer());
            }

            context.updateEinvoices(eInvoices);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean hasPendingEInvoices() throws HttpStatusCodeErrorException {
        try {
            EInvoiceAndCreditAlertsResponse alertsResponse = createGetRequest(EINVOICE_AND_CREDIT_ALERTS_URL,
                    EInvoiceAndCreditAlertsResponse.class);

            return alertsResponse.getNumberOfNewInvoices() > 0;
        } catch (HttpStatusCodeErrorException e) {
            // Error-Message: "Du har inte behörighet att använda denna tjänst."
            if (e.hasErrorCode(12231)) {
                log.info(e.getMessage());
                return false;
            }

            throw e;
        }
    }

    private TransferrableResponse fetchTransferDestinationAccounts() throws HttpStatusCodeErrorException {
        ClientResponse clientResponse = createGetRequest(TRANSFER_DESTINATIONS_URL);

        if (clientResponse.getStatus() != HttpStatus.SC_OK) {
            return null;
        }

        return clientResponse.getEntity(TransferrableResponse.class);
    }

    private void refreshAccountsTransactions(RefreshableItem type) {
        getAccounts().entrySet().stream()
                .filter(set -> type.isAccountType(set.getValue().getType()))
                .forEach(set -> {
                    AccountEntity accountEntity = set.getKey();
                    Account account = set.getValue();

                    List<Transaction> transactions = Lists.newArrayList();
                    String accountNumber = accountEntity.getAccountNumber();
                    // Fetch upcoming transactions.

                    for (UpcomingTransactionEntity transactionEntity : fetchUpcomingTransactions(accountNumber)) {
                        Transfer transfer = createTransferForUpcomingPayment(transactionEntity);
                        Transaction transaction = transactionEntity.toTransaction();

                        if (transfer != null) {
                            transfer.setId(UUIDUtils.fromTinkUUID(transaction.getId()));

                            try {
                                transaction.setPayload(TransactionPayloadTypes.EDITABLE_TRANSACTION_TRANSFER,
                                        MAPPER.writeValueAsString(transfer));
                            } catch (Exception e) {
                                throw new IllegalStateException(e);
                            }
                            transaction.setPayload(TransactionPayloadTypes.EDITABLE_TRANSACTION_TRANSFER_ID,
                                    UUIDUtils.toTinkUUID(transfer.getId()));
                        }

                        transactions.add(transaction);
                    }

                    // Fetch transactions.

                    boolean hasMoreTransactions;
                    int currentPage = 0;

                    do {
                        debug(request, "Requesting page " + (currentPage + 1));

                        DebitTransactionListResponse transactionListResponse;
                        try {
                            transactionListResponse = createPostRequestWithResponseHandling(
                                    TRANSACTIONS_URL, DebitTransactionListResponse.class,
                                    new ListAccountTransactionRequest(currentPage, accountNumber));
                        } catch (Exception e) {
                            throw new IllegalStateException(e);
                        }

                        debug(request, "Requested page size " + MoreObjects
                                .firstNonNull(transactionListResponse.getTransactions(), Lists.newArrayList()).size());

                        for (TransactionEntity transactionEntity : transactionListResponse.getTransactions()) {
                            transactions.add(transactionEntity.toTransaction());
                        }

                        if (isContentWithRefresh(account, transactions)) {
                            hasMoreTransactions = false;
                        } else {
                            hasMoreTransactions = transactionListResponse.getHasMore();
                            currentPage = transactionListResponse.getNextSequenceNumber();
                        }

                        context.updateStatus(CredentialsStatus.UPDATING, account, transactions);
                    } while (hasMoreTransactions);

                    context.updateTransactions(account, transactions);
                });
    }

    private List<UpcomingTransactionEntity> fetchUpcomingTransactions(String accountNumber) {
        ClientResponse clientResponse = createPostRequest(FETCH_UPCOMING_TRANSACTIONS_URL,
                new ListUpcomingTransactionRequest(accountNumber));

        if (Objects.equal(clientResponse.getStatus(), HttpStatus.SC_OK)) {
            UpcomingTransactionListResponse upcomingTransactionListResponse = clientResponse
                    .getEntity(UpcomingTransactionListResponse.class);

            return upcomingTransactionListResponse.getUpcomingTransactions();

        } else {
            log.error("Could not fetch upcoming transactions (status: " + clientResponse.getStatus() + ")");
        }

        return Lists.newArrayList();
    }

    private Transfer createTransferForUpcomingPayment(UpcomingTransactionEntity transactionEntity) {
        Transfer transfer = null;
        if (transactionEntity.getPaymentInfo() != null
                && transactionEntity.getPaymentInfo().isModificationAllowed()
                && Objects.equal(transactionEntity.getPaymentInfo().getPaymentType(), EINVOICE_DESCRIPTION)) {

            transfer = transactionEntity.toTransfer();
        }
        return transfer;
    }

    private void refreshCardsTransactions() throws Exception {
        List<CardEntity> cardEntities = fetchCardEntities();

        // loop the cards, that are not debit cards, and fetch transactions
        for (CardEntity cardEntity : cardEntities) {

            if (cardEntity.getCardType().equals("DEBIT")) {
                /*
                 * Cards of type DEBIT are connected to an account
                 * There for these cards are handled by:
                 * - fetchAndUpdateAccounts()
                 * - fetchAndUpdateAccountsAndTransactions()
                 */
                continue;
            }

            Account account = cardEntity.getAccount();

            List<Transaction> transactions = new LinkedList<>();

            boolean hasMoreTransactions;
            int currentPage = 1;

            do {
                debug(request, "Requesting page " + currentPage);

                CreditTransactionListResponse transactionListResponse = createPostRequestWithResponseHandling(
                        FETCH_CARD_TRANSACTIONS_URL, CreditTransactionListResponse.class,
                        new ListCardTransactionRequest(currentPage, account.getAccountNumber()));

                debug(request, "Requested page size " + MoreObjects
                        .firstNonNull(transactionListResponse.getTransactions(), Lists.newArrayList()).size());

                for (CardTransactionEntity transactionEntity : transactionListResponse.getTransactions()) {
                    transactions.add(transactionEntity.toTransaction());
                }

                if (isContentWithRefresh(account, transactions)) {
                    hasMoreTransactions = false;
                } else {
                    hasMoreTransactions = transactionListResponse.hasMorePages();
                    currentPage++;
                }

                context.updateStatus(CredentialsStatus.UPDATING, account, transactions);
            } while (hasMoreTransactions);

            context.updateTransactions(account, transactions);
        }
    }

    private void updateLoans() {
        LoanListResponse loans = null;

        try {
            loans = createGetRequest(FETCH_LOANS_URL, LoanListResponse.class);
        } catch (Exception e) {
            // Seeing LF return 400 and 500 responses from time to time, therefore this try-catch
            log.warn("Was not able to retrieve loans from Lansforsakringar: " + e.getMessage());
        }

        if (loans == null) {
            return;
        }

        for (LoanEntity le : loans.getLoans()) {
            try {
                String detailsString = createPostRequestWithResponseHandling(FETCH_LOAN_DETAILS_URL, String.class,
                        new LoanDetailsRequest(le.getLoanNumber()));

                LoanDetailsEntity details = MAPPER.readValue(detailsString, LoanDetailsEntity.class);

                Account account = details.toAccount();
                Loan loan = details.toLoan(detailsString);

                context.cacheAccount(account, AccountFeatures.createForLoan(loan));
            } catch (Exception e) {
                log.warn("Was not able to retrieve loan: " + e.getMessage());
            }
        }
    }

    @Override
    public void attachHttpFilters(ClientFilterFactory filterFactory) {
        filterFactory.addClientFilter(client);
    }

    @Override
    public void execute(final Transfer transfer) throws Exception, TransferExecutionException {

        if (transfer.getType() == TransferType.BANK_TRANSFER) {
            executeBankTransfer(transfer);
        } else if (transfer.getType() == TransferType.PAYMENT) {
            executePayment(transfer);
        } else {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("Not implemented.")
                    .setEndUserMessage("Not implemented.").build();
        }
    }

    @Override
    public void update(Transfer transfer) throws Exception, TransferExecutionException {
        switch (transfer.getType()) {
        case EINVOICE:
            approveEInvoice(transfer);
            break;
        case PAYMENT:
            updatePayment(transfer);
            break;
        default:
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("Not implemented.")
                    .setEndUserMessage("Not implemented.").build();
        }
    }

    private void approveEInvoice(final Transfer transfer) throws Exception {
        validateUpdateIsPermitted(catalog, transfer);
        AccountEntity sourceAccount = validatePaymentSourceAccount(transfer.getSource());
        EInvoice eInvoice = validateEInvoice(transfer);
        validateNumberOfOutstandingPaymentEntities(0);
        addEInvoiceToApproveList(transfer, sourceAccount, eInvoice);
        PaymentEntity paymentEntityToSign = validateNumberOfOutstandingPaymentEntities(1);
        signEInvoice(paymentEntityToSign);
    }

    private void validateUpdateIsPermitted(Catalog catalog, Transfer transfer) {
        Transfer originalTransfer = transfer.getOriginalTransfer().get();

        if (!Objects.equal(transfer.getDestinationMessage(), originalTransfer.getDestinationMessage())) {

            throw TransferExecutionException
                    .builder(SignableOperationStatuses.CANCELLED)
                    .setMessage("Not allowed to change destination message")
                    .setEndUserMessage(catalog.getString(
                            TransferExecutionException.EndUserMessage.EINVOICE_MODIFY_DESTINATION_MESSAGE))
                    .build();
        }

        if (!Objects.equal(transfer.getSourceMessage(), originalTransfer.getSourceMessage())) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setMessage("Not allowed to change source message")
                    .setEndUserMessage(
                            catalog.getString(TransferExecutionException.EndUserMessage.EINVOICE_MODIFY_SOURCE_MESSAGE))
                    .build();
        }
    }

    private PaymentEntity validateNumberOfOutstandingPaymentEntities(int numberOfOutstandingPayments) throws Exception {
        PaymentsListResponse paymentsListResponse = createGetRequest(
                FETCH_UNSIGNED_PAYMENTS_URL, PaymentsListResponse.class);

        if (paymentsListResponse != null) {
            List<PaymentEntity> eInvoices = paymentsListResponse.getPayments().stream()
                    .filter(p -> Objects.equal(p.getPaymentType(), EINVOICE_DESCRIPTION)).collect(
                            Collectors.toList());

            if (Iterables.size(eInvoices) == numberOfOutstandingPayments) {
                if (numberOfOutstandingPayments == 0) {
                    return null;
                } else {
                    return Iterables.get(eInvoices, 0);
                }
            }
        }

        throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setEndUserMessage(
                        catalog.getString(TransferExecutionException.EndUserMessage.EXISTING_UNSIGNED_TRANSFERS))
                .build();
    }

    private void signEInvoice(PaymentEntity paymentEntityToSign) {
        try {
            ClientResponse validateResponse = createGetRequest(VALIDATE_UNSIGNED_PAYMENTS_URL);
            validateTransactionClientResponse(validateResponse);

            ClientResponse createReferenceResponse = createGetRequest(CREATE_BANKID_REFERENCE_PAYMENTS_URL);
            validateTransactionClientResponse(createReferenceResponse);

            credentials.setStatus(CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION);
            credentials.setStatusPayload(null);
            context.requestSupplementalInformation(credentials, false);

            ClientResponse bankIdResponse = createGetRequest(SEND_UNSIGNED_PAYMENT_URL);
            validateTransactionClientResponse(bankIdResponse);

        } catch (TransferExecutionException e) {
            cancelUnsignedPayment(paymentEntityToSign.getUniqueId());
            throw e;
        }
    }

    private void cancelUnsignedPayment(String uniqueId) throws TransferExecutionException {
        log.info("Removing e-invoice/payment from inbox since signing failed.");
        CancelPaymentRequest request = new CancelPaymentRequest();
        request.setUniqueId(uniqueId);

        ClientResponse cancelPaymentResponse = createPostRequest(DELETE_UNSIGNED_PAYMENT_URL, request);

        validateTransactionClientResponse(cancelPaymentResponse);
    }

    private void addEInvoiceToApproveList(final Transfer transfer, AccountEntity sourceAccount, EInvoice eInvoice) {
        EInvoicePaymentRequest request = new EInvoicePaymentRequest();
        request.setOcr(transfer.getDestinationMessage());
        request.setDate(transfer.getDueDate().getTime());
        request.setToAccount(transfer.getDestination().getIdentifier(GIRO_FORMATTER));
        request.setElectronicInvoiceId(eInvoice.getElectronicInvoiceId());
        request.setAmount(transfer.getAmount().getValue());
        request.setFromAccount(sourceAccount.generalGetAccountIdentifier().getIdentifier(DEFAULT_FORMATTER));

        ClientResponse createPaymentClientResponse = createPostRequest(SIGNLIST_ADD_EINVOICE_URL, request);

        validateTransactionClientResponse(createPaymentClientResponse);
    }

    private EInvoice validateEInvoice(final Transfer transfer) throws Exception {
        final Optional<String> electronicInvoiceId = transfer.getPayloadValue(TransferPayloadType.PROVIDER_UNIQUE_ID);

        if (!electronicInvoiceId.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("No electronicInvoiceId on transfer").build();
        }

        EInvoicesListResponse invoicesListResponse = createGetRequest(
                FETCH_EINVOICES_URL, EInvoicesListResponse.class);

        Optional<EInvoice> eInvoice = invoicesListResponse.getElectronicInvoices().stream().filter(i ->
                LFUtils.findEInvoice(electronicInvoiceId.get()).apply(i)).findFirst();

        if (!eInvoice.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(catalog.getString(TransferExecutionException.EndUserMessage.EINVOICE_NO_MATCHES))
                    .build();
        }

        return eInvoice.get();
    }

    private AccountEntity validatePaymentSourceAccount(AccountIdentifier source) throws Exception {
        Optional<AccountEntity> sourceAccount = GeneralUtils.find(source, fetchPaymentAccounts());

        if (!sourceAccount.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(catalog.getString(TransferExecutionException.EndUserMessage.SOURCE_NOT_FOUND))
                    .build();
        }

        return sourceAccount.get();
    }

    private void updatePayment(Transfer transfer) throws Exception {
        // Validate the payment is at the bank.

        Transfer originalTransfer = transfer.getOriginalTransfer().get();

        OverviewEntity overview = createGetRequest(OVERVIEW_URL, OverviewEntity.class);
        Optional<UpcomingTransactionEntity> payment = Optional.empty();

        accountLoop:
        for (AccountEntity accountEntity : overview.getAccountEntities()) {
            for (UpcomingTransactionEntity upcomingTransaction : fetchUpcomingTransactions(
                    accountEntity.getAccountNumber())) {
                Transfer upcomingTransactionTransfer = upcomingTransaction.toTransfer();

                if (Objects.equal(originalTransfer.getHash(), upcomingTransactionTransfer.getHash())) {
                    payment = Optional.of(upcomingTransaction);
                    break accountLoop;
                }
            }
        }

        if (!payment.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("Cannot find payment in list from bank.")
                    .setEndUserMessage(catalog.getString("Something went wrong."))
                    .build();
        }

        AccountEntity source = validatePaymentSourceAccount(transfer.getSource());

        // Create update request and sign.

        UpdatePaymentRequest paymentRequest = new UpdatePaymentRequest();
        paymentRequest.setAmount(transfer.getAmount().getValue());
        paymentRequest.setReference(transfer.getDestinationMessage());
        paymentRequest.setFromAccountNumber(source.generalGetAccountIdentifier().getIdentifier(DEFAULT_FORMATTER));
        paymentRequest.setPaymentDate(transfer.getDueDate().getTime());
        paymentRequest.setPaymentId(payment.get().getId());

        ClientResponse createPaymentClientResponse = createPostRequest(SIGN_PAYMENT_CREATE_REFERENCE_URL,
                paymentRequest);
        validateTransactionClientResponse(createPaymentClientResponse);

        credentials.setStatus(CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION);
        credentials.setStatusPayload(null);
        context.requestSupplementalInformation(credentials, false);

        ClientResponse sendPaymentClientResponse = createPostRequest(SIGN_PAYMENT_SEND_PAYMENT_URL, paymentRequest);
        validateTransactionClientResponse(sendPaymentClientResponse);
    }

    private void executePayment(Transfer transfer) throws Exception {
        AccountIdentifier source = transfer.getSource();
        AccountIdentifier destination = transfer.getDestination();

        validatePaymentSourceAccount(source);

        String formattedDestination = destination.getIdentifier(GIRO_FORMATTER);

        ClientResponse recipientNameClientResponse = fetchRecipientNameAndValidateResponse(formattedDestination);
        RecipientEntity recipientNameResponse = recipientNameClientResponse.getEntity(RecipientEntity.class);

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(transfer.getAmount().getValue());
        paymentRequest.setToBgPg(formattedDestination);
        paymentRequest.setReference(transfer.getDestinationMessage());
        paymentRequest.setElectronicInvoiceId("");
        paymentRequest.setFromAccount(source.getIdentifier(DEFAULT_FORMATTER));
        paymentRequest.setPaymentDate(transfer.getDueDate() != null ? transfer.getDueDate().getTime() : 0);

        if (Objects.equal(recipientNameResponse.getOcrType(), "OCR_REQUIRED")) {
            paymentRequest.setReferenceType("OCR");
        } else {
            paymentRequest.setReferenceType("MESSAGE");
        }

        validatePaymentAndValidateResponse(paymentRequest);
        createPaymentAndValidateResponse(paymentRequest);

        try {
            signAndValidatePayment(paymentRequest);
        } catch (Exception initialException) {
            boolean cancelled = cancelFailedPayment(paymentRequest);
            boolean deleted = deleteSignedTransaction(paymentRequest);
            if (cancelled || deleted) {
                throw initialException;
            }

            // if we fail to remove a payment after
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(catalog.getString("We encountered problems signing the payment/transfer with your bank. Please log in to your bank app and validate the payment/transfer."))
                    .build();
        }
    }

    private ClientResponse fetchRecipientNameAndValidateResponse(String formattedDestination) {
        ClientResponse recipientNameClientResponse = postRequestAndValidateResponse(
                RECIPIENT_NAME_URL, new RecipientRequest(formattedDestination));
        validateTransactionClientResponse(recipientNameClientResponse);

        return recipientNameClientResponse;
    }

    private boolean deleteSignedTransaction(Object deleteRequest) throws Exception {
        if (deleteRequest instanceof PaymentRequest) {
            PaymentRequest paymentRequest = (PaymentRequest) deleteRequest;
            Optional<String> uniqueId = findFailedPaymentInSignedList(paymentRequest);

            if (!uniqueId.isPresent()) {
                return false;
            }

            return deleteAndValidateRemovalOfTransaction(paymentRequest.getFromAccount(), uniqueId.get());
        } else if (deleteRequest instanceof TransferRequest) {
            TransferRequest transferRequest = (TransferRequest) deleteRequest;
            Optional<String> uniqueId = findFailedTransferInSignedList(transferRequest);

            if (!uniqueId.isPresent()) {
                return false;
            }

            return deleteAndValidateRemovalOfTransaction(transferRequest.getFromAccount(), uniqueId.get());
        } else {
            log.warn(String.format("Got unexpected delete request object: %s",
                    deleteRequest.getClass().getSimpleName()));
        }

        return false;
    }

    private boolean deleteAndValidateRemovalOfTransaction(String fromAccount, String uniqueId) {
        try {
            log.info("Removing payment/transfer from signed since there was an exception.");
            DeleteSignedTransactionRequest request = createDeleteSignedTransactionRequest(false, fromAccount, uniqueId);

            ClientResponse deleteTransactionResponse = createPostRequest(DELETE_SIGNED_TRANSACTION_URL, request);
            validateTransactionClientResponse(deleteTransactionResponse);

            return true;
        } catch (Exception e) {
            log.error("Was expecting to delete signed payment/transfer but failed.", e);
        }

        return false;
    }

    private DeleteSignedTransactionRequest createDeleteSignedTransactionRequest(boolean isTransfer, String fromAccount,
            String uniqueId) {
        DeleteSignedTransactionRequest request = new DeleteSignedTransactionRequest();
        request.setFromAccountNumber(fromAccount);

        if (isTransfer) {
            request.setTransferId(uniqueId);
        } else {
            request.setPaymentId(uniqueId);
        }

        return request;
    }

    private Optional<String> findFailedPaymentInSignedList(PaymentRequest paymentRequest)
            throws Exception {
        for (UpcomingTransactionEntity transaction : fetchUpcomingTransactions(paymentRequest.getFromAccount())) {
            if (LFUtils.isSamePayment(paymentRequest, transaction)) {
                return Optional.of(transaction.getId());
            }
        }

        return Optional.empty();
    }

    private Optional<String> findFailedTransferInSignedList(TransferRequest transferRequest)
            throws Exception {
        for (UpcomingTransactionEntity transaction : fetchUpcomingTransactions(transferRequest.getFromAccount())) {
            if (LFUtils.isSameTransfer(transferRequest, transaction)) {
                return Optional.of(transaction.getId());
            }
        }

        return Optional.empty();
    }

    private boolean cancelFailedPayment(PaymentRequest paymentRequest)
            throws TransferExecutionException, HttpStatusCodeErrorException {
        List<PaymentEntity> unsignedPayments = fetchUnsignedPaymentsAndTransfers();

        Optional<String> uniqueId = findFailedPaymentInUnsignedList(paymentRequest, unsignedPayments);

        if (!uniqueId.isPresent()) {
            return false;
        }

        try {
            cancelUnsignedPayment(uniqueId.get());
            return true;
        } catch (TransferExecutionException deleteException) {
            log.warn("Could not delete unsigned transfer from outbox but was expecting it to be possible.",
                    deleteException);
        }

        return false;
    }

    private Optional<String> findFailedPaymentInUnsignedList(PaymentRequest paymentRequest,
            List<PaymentEntity> unsignedPayments) {
        for (PaymentEntity payment : unsignedPayments) {
            if (LFUtils.isSamePayment(paymentRequest, payment)) {
                return Optional.of(payment.getUniqueId());
            }
        }

        return Optional.empty();
    }

    private List<PaymentEntity> fetchUnsignedPaymentsAndTransfers() throws HttpStatusCodeErrorException {
        PaymentsListResponse paymentsList = createGetRequest(
                LIST_UNSIGNED_TRANSFERS_AND_PAYMENTS_URL, PaymentsListResponse.class);

        if (paymentsList == null) {
            return Lists.newArrayList();
        }

        return paymentsList.getPayments();
    }

    private void validatePaymentAndValidateResponse(PaymentRequest paymentRequest) {
        postRequestAndValidateResponse(VALIDATE_PAYMENT_URL, paymentRequest);
    }

    private void createPaymentAndValidateResponse(PaymentRequest paymentRequest) {
        postRequestAndValidateResponse(CREATE_PAYMENT_URL, paymentRequest);
    }

    private void signAndValidatePayment(PaymentRequest paymentRequest) throws BankIdException {
        credentials.setStatus(CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION);
        credentials.setStatusPayload(null);
        context.requestSupplementalInformation(credentials, false);

        collectTransferResponse(SEND_PAYMENT_URL, paymentRequest);
    }

    private void collectTransferResponse(String url, Object transferRequest) throws BankIdException {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            ClientResponse clientResponse = createPostRequest(url, transferRequest);

            int status = clientResponse.getStatus();

            if (status == Status.OK.getStatusCode()) {
                return;
            } else if (status == Status.UNAUTHORIZED.getStatusCode()
                    || status == Status.BAD_REQUEST.getStatusCode()) {
                switch (clientResponse.getHeaders().getFirst("Error-Code")) {
                case "00153":
                case "00154":
                    break;
                case "000114":
                case "001514":
                    throw BankIdError.ALREADY_IN_PROGRESS.exception();
                case "001515":
                case "000115":
                case "00015":
                    throw BankIdError.CANCELLED.exception();
                case "00159":
                case "001512":
                    throw BankIdError.TIMEOUT.exception();
                default:
                    if (clientResponse.getHeaders().getFirst("Error-Message") != null) {
                        throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                                .setEndUserMessage(clientResponse.getHeaders().getFirst("Error-Message")).build();
                    } else {
                        throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                                .setEndUserMessage(catalog.getString("Failed to sign using BankID, please try again later"))
                                .build();
                    }
                }
            } else {
                throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                        .setEndUserMessage(catalog.getString("Failed to sign using BankID, please try again later"))
                        .build();
            }
            Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
        }

        throw BankIdError.TIMEOUT.exception();
    }

    private ClientResponse postRequestAndValidateResponse(String url, Object requestEntity) {
        ClientResponse clientResponse = createPostRequest(url, requestEntity);
        validateTransactionClientResponse(clientResponse);

        return clientResponse;
    }

    /**
     * Helper method to validate a client response in the payment process.
     */
    private void validateTransactionClientResponse(ClientResponse clientResponse) throws TransferExecutionException {
        if (clientResponse.getStatus() == 400 && clientResponse.getHeaders().getFirst("Error-Message") != null) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(clientResponse.getHeaders().getFirst("Error-Message")).build();
        } else if (clientResponse.getStatus() != 200) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(catalog.getString("Failed to sign payment using BankID"))
                    .build();
        }
    }

    private TransferrableResponse fetchTransferSourceAccounts() throws HttpStatusCodeErrorException {
        return createGetRequest(FETCH_TRANSFER_SOURCE_ACCOUNTS, TransferrableResponse.class);
    }

    private void executeBankTransfer(final Transfer transfer) throws Exception {
        AccountIdentifier source = transfer.getSource();
        AccountIdentifier destination = transfer.getDestination();

        TransferrableResponse sourceAccounts = fetchTransferSourceAccounts();
        validateSourceAccount(source, sourceAccounts);

        // Find the destination account or the bank of the destination account.

        TransferrableResponse destinationAccounts = fetchTransferDestinationAccounts();

        Preconditions.checkState(destinationAccounts != null, "Could not collect transfer accounts");

        TransferRequest transferRequest = new TransferRequest();

        // Ensure correctly formatted transfer messages
        boolean isBetweenSameUserAccounts = LFUtils.find(
                destination, sourceAccounts.getAccounts()).isPresent();
        TransferMessageFormatter.Messages formattedMessages = transferMessageFormatter.getMessages(
                transfer, isBetweenSameUserAccounts);

        transferRequest.setBankName(findBankName(destination, destinationAccounts));
        transferRequest.setAmount(transfer.getAmount().getValue());
        transferRequest.setToText(formattedMessages.getDestinationMessage());
        transferRequest.setFromText(formattedMessages.getSourceMessage());
        transferRequest.setChallenge("");
        transferRequest.setResponse("");
        transferRequest.setFromAccount(source.getIdentifier(DEFAULT_FORMATTER));

        String apiFormattedDestination = getToAccount(destination);
        transferRequest.setToAccount(apiFormattedDestination);

        // Execute the transfer.

        if (Strings.isNullOrEmpty(transferRequest.getBankName())) {
            // Local transfer, no need for signing.

            createPostRequest(INTERNAL_TRANSFER_URL, transferRequest);
        } else {
            // External transfer, need to sign.
            try {
                signAndValidateTransfer(transferRequest);
            } catch (Exception initialException) {
                deleteSignedTransaction(transferRequest);
                throw initialException;
            }
        }
    }

    private void validateSourceAccount(AccountIdentifier source, TransferrableResponse sourceAccounts) {
        Optional<AccountEntity> fromAccountDetails = LFUtils.find(source, sourceAccounts.getAccounts());

        if (!fromAccountDetails.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(catalog.getString(TransferExecutionException.EndUserMessage.SOURCE_NOT_FOUND))
                    .build();
        }
    }

    private String findBankName(AccountIdentifier destination, TransferrableResponse destinationAccounts)
            throws HttpStatusCodeErrorException {
        Optional<AccountEntity> destinationAccount = LFUtils.find(destination, destinationAccounts.getAccounts());
        if (destinationAccount.isPresent()) {
            if (destinationAccount.get().isLocalAccount()) {
                return "";
            } else {
                return destinationAccount.get().getBankName();
            }
        } else {
            BankListResponse bankListResponse = createGetRequest(FETCH_ALL_BANKNAMES_URL, BankListResponse.class);

            Optional<BankEntity> bankEntity = findBankForAccountNumber(destination.getIdentifier(DEFAULT_FORMATTER),
                    bankListResponse.getAllBankNames());

            if (bankEntity.isPresent()) {
                return bankEntity.get().getBankName();
            } else {
                // If not found, let's try anyway to create the transaction (should work for major banks at least).
                if (destination.is(AccountIdentifier.Type.SE)) {
                    SwedishIdentifier swedishDestination = destination.to(SwedishIdentifier.class);
                    ClearingNumber.Details clearingNumber = LFUtils.getClearingNumberDetails(swedishDestination);
                    return clearingNumber.getBankName();
                } else {
                    throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                            .setEndUserMessage(catalog.getString(
                                    "Could not find a bank for the given destination account. Check the account number and try again."))
                            .build();
                }
            }
        }
    }

    private void signAndValidateTransfer(TransferRequest transferRequest) throws BankIdException {
        createPostRequest(CREATE_BANKID_REFERENCE_URL, transferRequest);

        credentials.setSupplementalInformation(null);
        credentials.setStatus(CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION);

        context.requestSupplementalInformation(credentials, false);

        collectTransferResponse(BANKID_COLLECT_DIRECT_TRANSFER_URL, transferRequest);
    }

    private String getToAccount(AccountIdentifier destination) throws Exception {
        if (!destination.is(AccountIdentifier.Type.SE)) {
            throw new Exception("Transfer account identifiers other than Swedish ones not implemented yet.");
        }

        return LFUtils.getApiAdaptedToAccount(
                destination.to(SwedishIdentifier.class));
    }

    private String fetchToken() {
        TokenResponse tokenResponse;
        try {
            tokenResponse = createGetRequest(FETCH_TOKEN_URL, TokenResponse.class);
        } catch (HttpStatusCodeErrorException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        String tokenChallangeHash = new String(
                Hex.encodeHex(StringUtils.hashSHA1(Integer.toHexString(tokenResponse.getNumber() + 4112))));

        TokenChallengeResponse tokenChallengeResponse = createPostRequest(FETCH_TOKEN_URL,
                new TokenChallengeRequest(tokenResponse.getNumber(), tokenChallangeHash, tokenResponse.getNumberPair()))
                .getEntity(TokenChallengeResponse.class);

        return tokenChallengeResponse.getToken();
    }

    @Override
    public boolean login() throws AuthenticationException, AuthorizationException {
        token = fetchToken();

        LoginResponse loginResponse;

        switch (credentials.getType()) {
        case MOBILE_BANKID:
            loginResponse = authenticateBankId();
            break;
        default:
            throw new IllegalStateException("Credentials type not implemented");
        }

        Preconditions.checkNotNull(loginResponse);

        ticket = loginResponse.getTicket();

        return true;
    }

    @Override
    public void logout() throws Exception {
        createClientRequest(PASSWORD_LOGIN_URL).delete();
    }

    private void logWithHackToAvoidCharacterLimit(String investmentDataString, String endpoint) {
        // Hack to handle max characters 2048 in logging message
        int limit = 1600;
        int lower = 0;
        int upper = investmentDataString.length() > limit ? limit : investmentDataString.length();
        int counter = 0;

        do {
            log.info(String.format("#investment-pre-study-lf - %s - counter: %s - response: %s",
                    endpoint, counter, investmentDataString.substring(lower, upper)));

            counter++;
            lower = upper;
            upper += limit;

            // Safety against index out of bounds exception
            if (upper > investmentDataString.length()) {
                upper = investmentDataString.length();
            }

        } while (lower < investmentDataString.length());
    }

    private void updateInvestmentAccounts() throws HttpStatusCodeErrorException, IOException {
        refreshIskAccounts();
        refreshFundDepot();
        refreshStockDepot();
    }

    private void refreshIskAccounts() throws HttpStatusCodeErrorException, IOException {
        String investmentSavingsDepotResponseString = createGetRequest(ISK_URL, String.class);

        InvestmentSavingsDepotResponse investmentSavingsDepotResponse;

        try {
            investmentSavingsDepotResponse = MAPPER.readValue(investmentSavingsDepotResponseString,
                    InvestmentSavingsDepotResponse.class);
        } catch (IOException e) {
            log.warn("#lf - investment savings deserialization failed - " +
                    investmentSavingsDepotResponseString, e);
            return;
        }

        InvestmentSavingsDepotEntity investmentSavingsDepotWrapper = investmentSavingsDepotResponse
                .getInvestmentSavingsDepotWrapper();

        if (investmentSavingsDepotWrapper == null ||
                investmentSavingsDepotWrapper.getInvestmentSavingsDepotWrappers() == null ||
                investmentSavingsDepotWrapper.getInvestmentSavingsDepotWrappers().isEmpty())  {
            return;
        }

        for (InvestmentSavingsDepotWrappersEntity investmentDepotWrapper :
                investmentSavingsDepotWrapper.getInvestmentSavingsDepotWrappers()) {
            Double totalValue = investmentDepotWrapper.getDepot().getTotalValue();
            Double balance = investmentDepotWrapper.getAccount().getBalance();
            Double marketValue = totalValue != null && balance != null ? (totalValue - balance) : null;

            Account account = investmentDepotWrapper.getAccount().toAccount(totalValue);
            String depotNumber = investmentDepotWrapper.getDepot().getDepotNumber();
            Double cashValue = null;

            try {
                CashBalanceResponse cashBalanceResponse = createGetRequestFromUrlAndDepotNumber(
                        CashBalanceResponse.class, ISK_CASH_BALANCE_URL, depotNumber);
                cashValue = cashBalanceResponse.getCashValue();
            } catch (URISyntaxException e) {
                log.warn("Could not build URI", e);
            }

            Portfolio portfolio =
                    investmentDepotWrapper.getDepot().toPortfolio(marketValue, cashValue, Portfolio.Type.ISK);

            SecurityHoldingsResponse funds;
            SecurityHoldingsResponse stocks;
            try {
                funds = createGetRequestFromUrlAndDepotNumber(SecurityHoldingsResponse.class,
                        FUND_SECURITIES_URL, depotNumber);
                stocks = createGetRequestFromUrlAndDepotNumber(SecurityHoldingsResponse.class,
                        STOCK_SECURITIES_URL, depotNumber);
            } catch (URISyntaxException e) {
                return;
            }

            List<Instrument> instruments = Lists.newArrayList();
            // Add funds
            funds.getSecurityHoldings().getFunds().forEach(fundEntity ->
                    fundEntity.toInstrument().ifPresent(instruments::add));

            // Add stocks
            stocks.getSecurityHoldings().getShares().forEach(shareEntity -> {
                InstrumentDetailsResponse instrumentDetails;
                try {
                    instrumentDetails = getInstrumentDetails(depotNumber, shareEntity.getIsinCode());
                    if (instrumentDetails == null) {
                        return;
                    }
                } catch (HttpStatusCodeErrorException e) {
                    // If the user don't have an fund depot this request will get a response with status code 400.
                    // Just return and don't do anything.
                    return;
                }

                shareEntity.toInstrument(instrumentDetails.getInstrument()).ifPresent(instruments::add);
            });

            // Add bonds
            stocks.getSecurityHoldings().getBonds().forEach(bondEntity -> {
                InstrumentDetailsResponse instrumentDetails;
                try {
                    instrumentDetails = getInstrumentDetails(depotNumber, bondEntity.getIsinCode());
                    if (instrumentDetails == null) {
                        return;
                    }
                    log.info("#lf - bond details: " + MAPPER.writeValueAsString(instrumentDetails));
                } catch (HttpStatusCodeErrorException e) {
                    // If the user don't have an fund depot this request will get a response with status code 400.
                    // Just return and don't do anything.
                    return;
                } catch (JsonProcessingException e) {
                    // Just continue
                }
                bondEntity.toInstrument().ifPresent(instruments::add);
            });



            portfolio.setInstruments(instruments);

            context.cacheAccount(account, AccountFeatures.createForPortfolios(portfolio));
        }
    }

    private void refreshFundDepot() {
        FundHoldingsResponse fundHoldingsResponse;
        try {
            fundHoldingsResponse = createGetRequest(FUND_DEPOT_URL, FundHoldingsResponse.class);
        } catch (HttpStatusCodeErrorException e) {
            // If the user don't have an fund depot this request will get a response with status code 400.
            // Just return and don't do anything.
            return;
        }

        Account account = fundHoldingsResponse.toAccount();
        Portfolio portfolio = fundHoldingsResponse.toPortfolio();

        List<Instrument> instruments = Lists.newArrayList();

        fundHoldingsResponse.getFunds().forEach(fundEntity -> {
            String fundInformationUrlWithFundId;
            try {
                fundInformationUrlWithFundId = new URIBuilder(FUND_INFORMATION_URL)
                        .addParameter("fundid", String.valueOf(fundEntity.getFundId()))
                        .build()
                        .toString();
            } catch (URISyntaxException e) {
                log.error("failed to create uri builder", e);
                return;
            }

            FundInformationWrapper fundInformationWrapper;
            try {
                fundInformationWrapper = createGetRequest(fundInformationUrlWithFundId, FundInformationWrapper.class);
            } catch (HttpStatusCodeErrorException e) {
                log.warn("failed to fetch fund details", e);
                return;
            }

            fundEntity.toInstrument(fundInformationWrapper.getFundInformation()).ifPresent(instruments::add);
        });
        portfolio.setInstruments(instruments);

        context.cacheAccount(account, AccountFeatures.createForPortfolios(portfolio));
    }

    private <T> T createGetRequestFromUrlAndDepotNumber(Class<T> responseClass, String url, String depotNumber)
            throws HttpStatusCodeErrorException, URISyntaxException {
        String requestUrl = new URIBuilder(url)
                .addParameter("depotNumber", depotNumber)
                .build().toString();
        return createGetRequest(requestUrl, responseClass);
    }

    private void refreshStockDepot() {
        ShareDepotResponse shareDepotResponse;
        SecurityHoldingsResponse securityHoldingsResponse;
        CashBalanceResponse cashBalanceResponse;
        try {
            shareDepotResponse = createGetRequest(STOCK_DEPOT_URL, ShareDepotResponse.class);
            if (shareDepotResponse.getShareDepotWrapper() == null
                    || shareDepotResponse.getShareDepotWrapper().getDepot() == null
                    || shareDepotResponse.getShareDepotWrapper().getDepot().getDepotNumber() == null) {
                return;
            }

            String depotNumber = shareDepotResponse.getShareDepotWrapper().getDepot().getDepotNumber();
            securityHoldingsResponse = createGetRequestFromUrlAndDepotNumber(SecurityHoldingsResponse.class,
                    STOCK_DEPOT_HOLDINGS_URL, depotNumber);
            cashBalanceResponse = createGetRequestFromUrlAndDepotNumber(CashBalanceResponse.class,
                    STOCK_DEPOT_CASH_BALANCE_URL, depotNumber);
        } catch (HttpStatusCodeErrorException e) {
            // If the user don't have an fund depot this request will get a response with status code 400.
            // Just return and don't do anything.
            if (e.getResponse().getStatus() != 400) {
                log.error("could not fetch fund depot", e);
            }

            return;
        } catch (URISyntaxException e) {
            log.warn("failed to build url", e);
            return;
        }

        Optional<Account> account = securityHoldingsResponse.toShareDepotAccount(
                shareDepotResponse.getShareDepotWrapper());

        if (!account.isPresent()) {
            return;
        }

        Portfolio portfolio = securityHoldingsResponse.toShareDepotPortfolio(shareDepotResponse.getShareDepotWrapper(),
                cashBalanceResponse.getCashValue());

        List<Instrument> instruments = Lists.newArrayList();

        String depotNumber = shareDepotResponse.getShareDepotWrapper().getDepot().getDepotNumber();
        securityHoldingsResponse.getSecurityHoldings().getShares().forEach(shareEntity -> {
            InstrumentDetailsResponse instrumentDetails;

            try {
                instrumentDetails = getInstrumentDetails(depotNumber, shareEntity.getIsinCode());
                if (instrumentDetails == null) {
                    return;
                }
            } catch (HttpStatusCodeErrorException e) {
                // If the user don't have an fund depot this request will get a response with status code 400.
                // Just return and don't do anything.
                return;
            }

            shareEntity.toInstrument(instrumentDetails.getInstrument()).ifPresent(instruments::add);
        });

        securityHoldingsResponse.getSecurityHoldings().getBonds().forEach(bondEntity -> {
            InstrumentDetailsResponse instrumentDetails;
            try {
                instrumentDetails = getInstrumentDetails(depotNumber, bondEntity.getIsinCode());
                if (instrumentDetails == null) {
                    return;
                }
                log.info("#lf - stockdepot - bond details: " + MAPPER.writeValueAsString(instrumentDetails));
            } catch (HttpStatusCodeErrorException e) {
                // If the user don't have an fund depot this request will get a response with status code 400.
                // Just return and don't do anything.
                return;
            } catch (JsonProcessingException e) {
                // Just continue
            }
            bondEntity.toInstrument().ifPresent(instruments::add);
        });
        portfolio.setInstruments(instruments);

        context.cacheAccount(account.get(), AccountFeatures.createForPortfolios(portfolio));
    }

    private InstrumentDetailsResponse getInstrumentDetails(String depotNumber, String isin)
            throws HttpStatusCodeErrorException {
        String stockDetailsUrl;
        try {
            stockDetailsUrl = new URIBuilder(STOCK_DETAILS_URL)
                    .addParameter("depotNumber", depotNumber)
                    .addParameter("isinCode", isin)
                    .build()
                    .toString();
        } catch (URISyntaxException e) {
            return null;
        }

        return createGetRequest(stockDetailsUrl, InstrumentDetailsResponse.class);
    }

    private Map<AccountEntity, Account> getAccounts() {
        if (accounts != null) {
            return accounts;
        }

        accounts = new HashMap<>();
        try {
            List<AccountEntity> accountEntities = fetchAccountEntities();

            for (AccountEntity accountEntity : accountEntities) {
                accounts.put(accountEntity, accountEntity.toAccount());
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        return accounts;
    }

    private void updateAccountPerType(RefreshableItem type) {
        getAccounts().entrySet().stream()
                .filter(set -> type.isAccountType(set.getValue().getType()))
                .forEach(set -> context.cacheAccount(set.getValue()));
    }

    @Override
    public void refresh(RefreshableItem item) {
        switch (item) {
        case EINVOICES:
            updateEInvoices();
            break;

        case TRANSFER_DESTINATIONS:
            updateTransferDestinations();
            break;

        case CHECKING_ACCOUNTS:
        case SAVING_ACCOUNTS:
            updateAccountPerType(item);
            break;

        case CHECKING_TRANSACTIONS:
        case SAVING_TRANSACTIONS:
            refreshAccountsTransactions(item);
            break;

        case CREDITCARD_ACCOUNTS:
            try {
                updateCards();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            break;

        case CREDITCARD_TRANSACTIONS:
            try {
                refreshCardsTransactions();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            break;

        case LOAN_ACCOUNTS:
            updateLoans();
            break;

        case INVESTMENT_ACCOUNTS:
            try {
                updateInvestmentAccounts();
            } catch (Exception e) {
                // Just catch and exit gently
                log.warn("Caught exception while logging investment data", e);
            }
            break;
        }
    }

    private void updateTransferDestinations() {
        try {
            List<AccountEntity> accountEntities = fetchAccountEntities();

            TransferDestinationsResponse response = new TransferDestinationsResponse();

            response.addDestinations(getTransferAccountDestinations(accountEntities, context.getUpdatedAccounts()));
            response.addDestinations(getPaymentAccountDestinations(accountEntities, context.getUpdatedAccounts()));

            context.updateTransferDestinationPatterns(response.getDestinations());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private List<CardEntity> fetchCardEntities()
            throws HttpStatusCodeErrorException {
        return createGetRequest(OVERVIEW_URL, OverviewEntity.class).getCards();
    }

    private List<AccountEntity> fetchAccountEntities()
            throws HttpStatusCodeErrorException {
        return createGetRequest(OVERVIEW_URL, OverviewEntity.class).getAccountEntities();
    }

    private void updateAccounts() throws HttpStatusCodeErrorException {
        List<AccountEntity> accountEntities = fetchAccountEntities();

        for (AccountEntity accountEntity : accountEntities) {
            context.cacheAccount(accountEntity.toAccount());
        }
    }

    private void updateCards() throws HttpStatusCodeErrorException {
        List<CardEntity> cardEntities = fetchCardEntities();

        for (CardEntity cardEntity : cardEntities) {
            if (cardEntity.getCardType().equals("DEBIT")) {
                /*
                 * Cards of type DEBIT are connected to an account
                 *  Handled by: cacheAccounts()
                 */
                continue;
            }

            context.cacheAccount(cardEntity.getAccount());
        }
    }

    private boolean assertStatusCodeOK(ClientResponse response) {
        if (response.getStatus() != HttpStatusCodes.STATUS_CODE_OK) {
            log.warn("Status code was not 200. Status code " + response.getStatus());
            return false;
        }
        return true;
    }

    // This could be moved into abstract agent when we have support for different data types in result
    protected Map<String, Object> requestSupplementalInformation(Credentials credentials, List<Field> fields) {

        credentials.setStatus(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);
        credentials.setSupplementalInformation(SerializationUtils.serializeToString(fields));

        String supplementalInformation = context.requestSupplementalInformation(credentials, true);

        log.info("Supplemental Information response is: " + supplementalInformation);

        if (Strings.isNullOrEmpty(supplementalInformation)) {
            log.warn("Supplemental information is empty/null");
            return null;
        }

        return SerializationUtils.deserializeFromString(supplementalInformation, TYPE_MAP_REF);
    }

    @Override
    public boolean isLoggedIn() throws Exception {
        return keepAlive();
    }

    @Override
    public boolean keepAlive() throws Exception {
        return createGetRequest(OVERVIEW_URL).getStatus() == Status.OK.getStatusCode();
    }

    @Override
    public void persistLoginSession() {
        Session session = new Session();
        session.setToken(token);
        session.setTicket(ticket);
        session.setCookiesFromClient(client);

        credentials.setPersistentSession(session);
    }

    @Override
    public void loadLoginSession() {

        Session session = credentials.getPersistentSession(Session.class);

        if (session == null) {
            return;
        }

        token = session.getToken();
        ticket = session.getTicket();

        addSessionCookiesToClient(client, session);
    }

    @Override
    public void clearLoginSession() {
        // Clean the session in memory
        token = null;
        ticket = null;

        // Clean the persisted session
        credentials.removePersistentSession();
    }

    private enum UserMessage implements LocalizableEnum {
        UNDERAGE(new LocalizableKey("You must be over the age of 16 to use Mobilt BankID."));

        private LocalizableKey userMessage;

        UserMessage(LocalizableKey userMessage) {
            this.userMessage = userMessage;
        }
        @Override
        public LocalizableKey getKey() {
            return userMessage;
        }
    }
}
