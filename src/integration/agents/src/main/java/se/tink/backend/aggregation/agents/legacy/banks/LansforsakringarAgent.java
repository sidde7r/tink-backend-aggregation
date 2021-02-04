package se.tink.backend.aggregation.agents.banks;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.MORTGAGE_AGGREGATION;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.PAYMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.TRANSFERS;
import static se.tink.libraries.credentials.service.RefreshableItem.CHECKING_TRANSACTIONS;
import static se.tink.libraries.credentials.service.RefreshableItem.SAVING_ACCOUNTS;
import static se.tink.libraries.credentials.service.RefreshableItem.SAVING_TRANSACTIONS;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Inject;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.client.utils.URIBuilder;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchEInvoicesResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.PersistentLogin;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshEInvoiceExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.TransferExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.LFUtils;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.LansforsakringarBaseApiClient;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.LansforsakringarDateUtil;
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
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage;
import se.tink.backend.aggregation.agents.general.GeneralUtils;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Loan;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForDecoupledMode;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForProductionMode;
import se.tink.backend.aggregation.agents.modules.LegacyAgentProductionStrategyModule;
import se.tink.backend.aggregation.agents.modules.LegacyAgentWiremockStrategyModule;
import se.tink.backend.aggregation.agents.modules.providers.LegacyAgentStrategyInterface;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.http.filter.factory.ClientFilterFactory;
import se.tink.backend.aggregation.utils.transfer.StringNormalizerSwedish;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.backend.aggregation.utils.transfer.TransferMessageLengthConfig;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.se.ClearingNumber;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableEnum;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.SeIdentityData;
import se.tink.libraries.net.client.TinkApacheHttpClient4;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.strings.StringUtils;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.uuid.UUIDUtils;

@AgentCapabilities({
    CHECKING_ACCOUNTS,
    LOANS,
    PAYMENTS,
    CREDIT_CARDS,
    SAVINGS_ACCOUNTS,
    IDENTITY_DATA,
    TRANSFERS,
    INVESTMENTS,
    MORTGAGE_AGGREGATION
})
@AgentDependencyModulesForProductionMode(modules = LegacyAgentProductionStrategyModule.class)
@AgentDependencyModulesForDecoupledMode(modules = LegacyAgentWiremockStrategyModule.class)
public final class LansforsakringarAgent extends AbstractAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshEInvoiceExecutor,
                RefreshTransferDestinationExecutor,
                RefreshIdentityDataExecutor,
                TransferExecutor,
                PersistentLogin {
    private final Function<ApacheHttpClient4Config, TinkApacheHttpClient4> strategy;
    private static final DefaultAccountIdentifierFormatter DEFAULT_FORMATTER =
            new DefaultAccountIdentifierFormatter();
    private static final DisplayAccountIdentifierFormatter GIRO_FORMATTER =
            new DisplayAccountIdentifierFormatter();
    private static final TransferMessageLengthConfig TRANSFER_MESSAGE_LENGTH_CONFIG =
            TransferMessageLengthConfig.createWithMaxLength(30, 14);
    private static final int MAX_ATTEMPTS = 80;

    private static final String BASE_URL = "https://mobil.lansforsakringar.se/appoutlet";
    private static final String EINVOICE_DESCRIPTION = "ElectronicInvoice";
    private static final String OVERVIEW_URL = BASE_URL + "/overview";
    private static final String PASSWORD_LOGIN_URL = BASE_URL + "/security/user";
    private static final String BANKID_AUTHENTICATE_URL =
            BASE_URL + "/security/user/bankid/authenticate";
    private static final String BANKID_COLLECT_URL = BASE_URL + "/security/user/bankid/login/2.0";
    private static final String BANKID_COLLECT_DIRECT_TRANSFER_URL =
            BASE_URL + "/directtransfer/bankid";
    private static final String CREATE_BANKID_REFERENCE_URL =
            BASE_URL + "/directtransfer/createbankidreference";
    private static final String CREATE_PAYMENT_URL =
            BASE_URL + "/directpayment/createreference/bankid";
    private static final String SAVED_RECIPIENTS_URL = BASE_URL + "/payment/savedrecipients";
    private static final String PAYMENT_ACCOUNTS_URL = BASE_URL + "/payment/paymentaccount";
    private static final String SEND_PAYMENT_URL = BASE_URL + "/directpayment/send/bankid";
    private static final String FETCH_EINVOICES_URL =
            BASE_URL + "/payment/einvoice/einvoiceandcredit";
    private static final String TRANSFER_DESTINATIONS_URL =
            BASE_URL + "/account/transferrablewithsavedrecipients";
    private static final String EINVOICE_AND_CREDIT_ALERTS_URL =
            BASE_URL + "/payment/einvoice/einvoiceandcreditalerts";
    private static final String TRANSACTIONS_URL = BASE_URL + "/account/transaction/2.0";
    private static final String VALIDATE_PAYMENT_URL = BASE_URL + "/directpayment/validate";
    private static final String RECIPIENT_NAME_URL = BASE_URL + "/payment/recipientname";
    private static final String LIST_UNSIGNED_TRANSFERS_AND_PAYMENTS_URL =
            BASE_URL + "/unsigned/paymentsandtransfers/list/2.0";
    private static final String DELETE_SIGNED_TRANSACTION_URL = BASE_URL + "/payment/signed/delete";
    private static final String FETCH_TOKEN_URL = BASE_URL + "/security/client";
    private static final String FETCH_UPCOMING_TRANSACTIONS_URL =
            BASE_URL + "/account/upcoming/5.0";
    private static final String FETCH_CARD_TRANSACTIONS_URL = BASE_URL + "/card/transaction";
    private static final String FETCH_LOANS_URL = BASE_URL + "/loan/loans/withtotal";
    private static final String FETCH_LOAN_DETAILS_URL = BASE_URL + "/loan/details";
    private static final String DELETE_UNSIGNED_PAYMENT_URL = BASE_URL + "/payment/deleteunsigned";
    private static final String FETCH_TRANSFER_SOURCE_ACCOUNTS =
            BASE_URL + "/account/transferrable?direction=from";
    private static final String INTERNAL_TRANSFER_URL = BASE_URL + "/directtransfer";
    private static final String FETCH_ALL_BANKNAMES_URL =
            BASE_URL + "/directtransfer/fetchallbanknames";
    private static final String ISK_URL = BASE_URL + "/depot/investmentsavings/3.0";
    private static final String FUND_SECURITIES_URL =
            BASE_URL + "/depot/holding/fund/securityholdings/withdetails/2.0";
    private static final String STOCK_SECURITIES_URL =
            BASE_URL + "/depot/holding/share/securityholdings/2.0";
    private static final String STOCK_DETAILS_URL =
            BASE_URL + "/depot/trading/share/instrumentwithisin";
    private static final String FUND_DEPOT_URL = BASE_URL + "/fund/holdings";
    private static final String FUND_INFORMATION_URL = BASE_URL + "/fund/fundinformation";
    private static final String STOCK_DEPOT_URL = BASE_URL + "/depot/share";
    private static final String STOCK_DEPOT_HOLDINGS_URL =
            BASE_URL + "/depot/holding/share/securityholdings";
    private static final String STOCK_DEPOT_CASH_BALANCE_URL =
            BASE_URL + "/depot/holding/depotcashbalance";
    private static final String ISK_CASH_BALANCE_URL =
            BASE_URL + "/depot/holding/depotcashbalance/2.0";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static Optional<BankEntity> findBankForAccountNumber(
            String destinationAccount, List<BankEntity> banks) {
        final int accountClearingNumber = Integer.parseInt(destinationAccount.substring(0, 4));

        return banks.stream()
                .filter(
                        be ->
                                be.getFromClearingRange() <= accountClearingNumber
                                        && accountClearingNumber <= be.getToClearingRange())
                .findFirst();
    }

    private final Catalog catalog;
    private final TinkApacheHttpClient4 client;
    private final Credentials credentials;
    private final String deviceId;
    private final TransferMessageFormatter transferMessageFormatter;
    private final LansforsakringarBaseApiClient lansforsakringarBaseApiClient;
    private String ticket = null;
    private String token = null;
    private String loginName = null;
    private String loginSsn = null;
    private long retrySleepMilliseconds = 500;
    private int maxNumRetries = 5;

    // cache
    private Map<AccountEntity, Account> accounts = null;

    private static final ImmutableList<String> BANK_SIDE_FAILURES =
            ImmutableList.of(
                    "connection reset", "connect timed out", "read timed out", "failed to respond");

    @Inject
    public LansforsakringarAgent(
            AgentComponentProvider agentComponentProvider,
            LegacyAgentStrategyInterface strategyProvider) {
        super(agentComponentProvider.getCredentialsRequest(), agentComponentProvider.getContext());
        strategy = strategyProvider.getLegacyHttpClientStrategy();
        catalog = context.getCatalog();
        credentials = request.getCredentials();
        client = strategy.apply(new DefaultApacheHttpClient4Config());

        addBankSideFailureFilter();

        deviceId =
                new String(
                        Hex.encodeHex(
                                StringUtils.hashSHA1(
                                        credentials.getField(Field.Key.USERNAME) + "-TINK")));
        lansforsakringarBaseApiClient =
                new LansforsakringarBaseApiClient(
                        client, strategyProvider.getLegacyHostStrategy(), deviceId);
        transferMessageFormatter =
                new TransferMessageFormatter(
                        catalog,
                        TRANSFER_MESSAGE_LENGTH_CONFIG,
                        new StringNormalizerSwedish(",.-_"));
    }

    private void addBankSideFailureFilter() {
        client.addFilter(
                new ClientFilter() {
                    @Override
                    public ClientResponse handle(ClientRequest cr) {
                        try {
                            return getNext().handle(cr);
                        } catch (ClientHandlerException e) {
                            handleKnownBankSideFailures(e);
                            throw e;
                        }
                    }
                });
    }

    private void handleKnownBankSideFailures(ClientHandlerException e) {
        if (Strings.isNullOrEmpty(e.getMessage())) {
            throw e;
        }

        BANK_SIDE_FAILURES.stream()
                .filter((failure -> e.getMessage().toLowerCase().contains(failure)))
                .findAny()
                .ifPresent(
                        f -> {
                            throw BankServiceError.BANK_SIDE_FAILURE.exception(e);
                        });
    }

    private LoginResponse authenticateBankId()
            throws AuthorizationException, AuthenticationException {
        ClientResponse bankIdloginClientResponse =
                createPostRequest(
                        BANKID_AUTHENTICATE_URL,
                        new BankIdAuthenticationRequest(credentials.getField(Field.Key.USERNAME)));

        int status = bankIdloginClientResponse.getStatus();

        if (status != HttpStatus.SC_OK) {
            String errorCode = bankIdloginClientResponse.getHeaders().getFirst("Error-Code");
            String errorMessage = bankIdloginClientResponse.getHeaders().getFirst("Error-Message");

            switch (errorCode) {
                case "000114":
                    throw BankIdError.ALREADY_IN_PROGRESS.exception();
                case "99312":
                    // Error-Message: Du behöver vara över 16 år för att använda Mobilt BankID.
                    // Kontakta gärna oss om du har frågor.
                    throw AuthorizationError.UNAUTHORIZED.exception(UserMessage.UNDERAGE.getKey());
                case "99021":
                    // Error-Message: Dina inloggningsuppgifter stämmer inte. Kontrollera dem och
                    // försök igen. Kontakta oss om problemet kvarstår.
                    // This occurs if the user enters the wrong personal number, e.g. 188705030142
                    throw LoginError.INCORRECT_CREDENTIALS.exception();
                case "00012":
                case "00019":
                case "000117":
                    throw BankServiceError.BANK_SIDE_FAILURE.exception();
                default:
                    throw new IllegalStateException(
                            String.format(
                                    "#login-refactoring - LF - Login failed with errorCode: %s, errorMessage: %s",
                                    errorCode, errorMessage));
            }
        }

        BankIdLoginResponse bankIdloginResponse =
                bankIdloginClientResponse.getEntity(BankIdLoginResponse.class);

        supplementalRequester.openBankId();

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            ClientResponse clientLoginResponse =
                    createPostRequest(
                            BANKID_COLLECT_URL,
                            new BankIdLoginRequest(
                                    bankIdloginResponse.getReference(),
                                    credentials.getField(Field.Key.USERNAME)));

            status = clientLoginResponse.getStatus();

            if (status == HttpStatus.SC_OK) {
                LoginResponse loginResponse = clientLoginResponse.getEntity(LoginResponse.class);
                loginName = loginResponse.getName();
                loginSsn = loginResponse.getSsn();
                return loginResponse;
            } else if (status == HttpStatus.SC_UNAUTHORIZED
                    || status == HttpStatus.SC_BAD_REQUEST) {
                switch (clientLoginResponse.getHeaders().getFirst("Error-Code")) {
                    case "00013":
                    case "00014":
                        log.info(
                                "Waiting for Mobilt BankID authentication ("
                                        + clientLoginResponse.getHeaders().getFirst("Error-Message")
                                        + ")");
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
                        // Välkommen till mobilbanken. Du aktiverar appen via Länsförsäkringars
                        // internetbank.
                        // Logga in på lansforsakringar.se och godkänn våra allmänna
                        // internetvillkor.
                        throw LoginError.NOT_CUSTOMER.exception();
                    default:
                        // NOP
                }
            }
            Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
        }

        throw BankIdError.TIMEOUT.exception();
    }

    private <T> T createPostRequestWithResponseHandling(
            String url, Class<T> returnClass, Object requestEntity)
            throws HttpStatusCodeErrorException {
        ClientResponse response = createPostRequest(url, requestEntity);
        return handleRequestResponse(response, returnClass);
    }

    private ClientResponse createPostRequest(String url, Object requestEntity) {
        Builder request = lansforsakringarBaseApiClient.createClientRequest(url, token, ticket);
        request.type(MediaType.APPLICATION_JSON);
        return request.post(ClientResponse.class, requestEntity);
    }

    private <T> T createGetRequest(String url, Class<T> returnClass)
            throws HttpStatusCodeErrorException {
        Builder request = lansforsakringarBaseApiClient.createClientRequest(url, token, ticket);
        ClientResponse response = request.get(ClientResponse.class);
        return handleRequestResponse(response, returnClass);
    }

    private ClientResponse createGetRequest(String url) {
        Builder request = lansforsakringarBaseApiClient.createClientRequest(url, token, ticket);
        return request.get(ClientResponse.class);
    }

    private <T> T handleRequestResponse(ClientResponse response, Class<T> returnClass)
            throws HttpStatusCodeErrorException {
        if (response.getStatus() == HttpStatus.SC_OK) {
            return response.getEntity(returnClass);
        } else {
            String errorMsg = response.getHeaders().getFirst("Error-Message");

            // Looking at error message to decide if bank side failure as LF seem to have a bunch
            // of different codes for the same errors.
            if (!Strings.isNullOrEmpty(errorMsg)
                    && errorMsg.toLowerCase().contains("ett tekniskt fel")) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception("Message:" + errorMsg);
            }

            throw new HttpStatusCodeErrorException(
                    response,
                    "Request status code " + response.getStatus() + ": '" + errorMsg + "'");
        }
    }

    private void debug(CredentialsRequest request, String message) {
        if (request.getCredentials().isDebug()) {
            log.info(message);
        }
    }

    private Map<Account, List<TransferDestinationPattern>> getPaymentAccountDestinations(
            List<AccountEntity> accountEntities, List<Account> accounts)
            throws HttpStatusCodeErrorException {
        if (accountEntities.isEmpty()) {
            log.info("Payment accounts not updated: The user has no accounts.");
            return Collections.emptyMap();
        }

        List<AccountEntity> paymentAccounts = fetchPaymentAccounts();

        RecipientsResponse recipientsResponse =
                createGetRequest(SAVED_RECIPIENTS_URL, RecipientsResponse.class);

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
     *
     * @return Non-null list of account entities
     * @throws HttpStatusCodeErrorException If the client threw from bad status code others than
     *     "missing service"
     */
    private List<AccountEntity> fetchPaymentAccounts() throws HttpStatusCodeErrorException {
        try {
            PaymentAccountsResponse paymentAccounts =
                    createGetRequest(PAYMENT_ACCOUNTS_URL, PaymentAccountsResponse.class);

            return paymentAccounts.getPaymentAccounts();
        } catch (HttpStatusCodeErrorException e) {
            // Error-Message: "Du saknar den här tjänsten. Kontakta oss för att få hjälp att komma
            // igång."
            if (e.hasErrorCode(12051)) {
                log.info(e.getMessage(), e);
                return Lists.newArrayList();
            }

            throw e;
        }
    }

    private Map<Account, List<TransferDestinationPattern>> getTransferAccountDestinations(
            List<AccountEntity> accountEntities, List<Account> updatedAccounts)
            throws HttpStatusCodeErrorException {
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

    private boolean hasPendingEInvoices() throws HttpStatusCodeErrorException {
        try {
            EInvoiceAndCreditAlertsResponse alertsResponse =
                    createGetRequest(
                            EINVOICE_AND_CREDIT_ALERTS_URL, EInvoiceAndCreditAlertsResponse.class);

            return alertsResponse.getNumberOfNewInvoices() > 0;
        } catch (HttpStatusCodeErrorException e) {
            // Error-Message: "Du har inte behörighet att använda denna tjänst."
            if (e.hasErrorCode(12231)) {
                log.info(e.getMessage(), e);
                return false;
            }

            throw e;
        }
    }

    private TransferrableResponse fetchTransferDestinationAccounts()
            throws HttpStatusCodeErrorException {
        ClientResponse clientResponse = createGetRequest(TRANSFER_DESTINATIONS_URL);

        if (clientResponse.getStatus() != HttpStatus.SC_OK) {
            return null;
        }

        return clientResponse.getEntity(TransferrableResponse.class);
    }

    private List<UpcomingTransactionEntity> fetchUpcomingTransactions(String accountNumber) {
        ClientResponse clientResponse =
                createPostRequest(
                        FETCH_UPCOMING_TRANSACTIONS_URL,
                        new ListUpcomingTransactionRequest(accountNumber));

        if (Objects.equal(clientResponse.getStatus(), HttpStatus.SC_OK)) {
            UpcomingTransactionListResponse upcomingTransactionListResponse =
                    clientResponse.getEntity(UpcomingTransactionListResponse.class);

            return upcomingTransactionListResponse.getUpcomingTransactions();

        } else {
            log.warn(
                    "Could not fetch upcoming transactions (status: "
                            + clientResponse.getStatus()
                            + ")");
        }

        return Lists.newArrayList();
    }

    private Transfer createTransferForUpcomingPayment(UpcomingTransactionEntity transactionEntity) {
        Transfer transfer = null;
        if (transactionEntity.getPaymentInfo() != null
                && transactionEntity.getPaymentInfo().isModificationAllowed()
                && Objects.equal(
                        transactionEntity.getPaymentInfo().getPaymentType(),
                        EINVOICE_DESCRIPTION)) {

            transfer = transactionEntity.toTransfer();
        }
        return transfer;
    }

    @Override
    public void attachHttpFilters(ClientFilterFactory filterFactory) {
        filterFactory.addClientFilter(client);
    }

    @Override
    public void execute(final Transfer transfer) throws Exception {
        if (transfer.getType() == TransferType.BANK_TRANSFER) {
            executeBankTransfer(transfer);
        } else if (transfer.getType() == TransferType.PAYMENT) {
            executePayment(transfer);
        } else {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("Not implemented.")
                    .setEndUserMessage("Not implemented.")
                    .build();
        }
    }

    private void cancelUnsignedPayment(String uniqueId, Exception e)
            throws TransferExecutionException {
        log.info("Removing e-invoice/payment from inbox since signing failed.", e);
        CancelPaymentRequest request = new CancelPaymentRequest();
        request.setUniqueId(uniqueId);

        ClientResponse cancelPaymentResponse =
                createPostRequest(DELETE_UNSIGNED_PAYMENT_URL, request);

        validateTransactionClientResponse(cancelPaymentResponse);
    }

    private AccountEntity validatePaymentSourceAccount(AccountIdentifier source) throws Exception {
        Optional<AccountEntity> sourceAccount = GeneralUtils.find(source, fetchPaymentAccounts());

        if (!sourceAccount.isPresent()) {
            throw cancelTransfer(
                    TransferExecutionException.EndUserMessage.SOURCE_NOT_FOUND,
                    InternalStatus.INVALID_SOURCE_ACCOUNT);
        }

        return sourceAccount.get();
    }

    private void executePayment(Transfer transfer) throws Exception {
        AccountIdentifier source = transfer.getSource();
        AccountIdentifier destination = transfer.getDestination();

        validatePaymentSourceAccount(source);

        String formattedDestination = destination.getIdentifier(GIRO_FORMATTER);

        ClientResponse recipientNameClientResponse =
                fetchRecipientNameAndValidateResponse(formattedDestination);
        RecipientEntity recipientNameResponse =
                recipientNameClientResponse.getEntity(RecipientEntity.class);

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(transfer.getAmount().getValue());
        paymentRequest.setToBgPg(formattedDestination);

        paymentRequest.setElectronicInvoiceId("");
        paymentRequest.setFromAccount(source.getIdentifier(DEFAULT_FORMATTER));
        paymentRequest.setPaymentDate(
                LansforsakringarDateUtil.getNextPossiblePaymentDateForBgPg(transfer.getDueDate()));
        if (Objects.equal(recipientNameResponse.getOcrType(), "OCR_REQUIRED")) {
            if (!LFUtils.isValidOCR(transfer.getRemittanceInformation().getValue())) {
                cancelTransfer(
                        TransferExecutionException.EndUserMessage.INVALID_OCR,
                        InternalStatus.INVALID_OCR);
            } else {
                paymentRequest.setReferenceType("OCR");
            }
        } else {
            paymentRequest.setReferenceType("MESSAGE");
        }
        paymentRequest.setReference(transfer.getRemittanceInformation().getValue());

        validatePaymentAndValidateResponse(paymentRequest);
        createPaymentAndValidateResponse(paymentRequest);

        try {
            signAndValidatePayment(paymentRequest);
        } catch (Exception initialException) {
            boolean cancelled = cancelFailedPayment(paymentRequest, initialException);
            boolean deleted = deleteSignedTransaction(paymentRequest);
            if (cancelled || deleted) {
                throw initialException;
            }

            // if we fail to remove a payment after
            throw failTransfer(TransferExecutionException.EndUserMessage.SIGN_AND_REMOVAL_FAILED);
        }
    }

    private ClientResponse fetchRecipientNameAndValidateResponse(String formattedDestination) {
        ClientResponse recipientNameClientResponse =
                postRequestAndValidateResponse(
                        RECIPIENT_NAME_URL, new RecipientRequest(formattedDestination));
        validateTransactionClientResponse(recipientNameClientResponse);

        return recipientNameClientResponse;
    }

    private boolean deleteSignedTransaction(Object deleteRequest) {
        if (deleteRequest instanceof PaymentRequest) {
            PaymentRequest paymentRequest = (PaymentRequest) deleteRequest;
            Optional<String> uniqueId = findFailedPaymentInSignedList(paymentRequest);

            if (!uniqueId.isPresent()) {
                return true;
            }

            return deleteAndValidateRemovalOfTransaction(
                    paymentRequest.getFromAccount(), uniqueId.get());
        } else if (deleteRequest instanceof TransferRequest) {
            TransferRequest transferRequest = (TransferRequest) deleteRequest;
            Optional<String> uniqueId = findFailedTransferInSignedList(transferRequest);

            if (!uniqueId.isPresent()) {
                return false;
            }

            return deleteAndValidateRemovalOfTransaction(
                    transferRequest.getFromAccount(), uniqueId.get());
        } else {
            log.warn(
                    String.format(
                            "Got unexpected delete request object: %s",
                            deleteRequest.getClass().getSimpleName()));
        }

        return false;
    }

    private boolean deleteAndValidateRemovalOfTransaction(String fromAccount, String uniqueId) {
        try {
            log.info("Removing payment/transfer from signed since there was an exception.");
            DeleteSignedTransactionRequest request =
                    createDeleteSignedTransactionRequest(false, fromAccount, uniqueId);

            ClientResponse deleteTransactionResponse =
                    createPostRequest(DELETE_SIGNED_TRANSACTION_URL, request);
            validateTransactionClientResponse(deleteTransactionResponse);

            return true;
        } catch (Exception e) {
            log.error("Was expecting to delete signed payment/transfer but failed.", e);
        }

        return false;
    }

    private DeleteSignedTransactionRequest createDeleteSignedTransactionRequest(
            boolean isTransfer, String fromAccount, String uniqueId) {
        DeleteSignedTransactionRequest request = new DeleteSignedTransactionRequest();
        request.setFromAccountNumber(fromAccount);

        if (isTransfer) {
            request.setTransferId(uniqueId);
        } else {
            request.setPaymentId(uniqueId);
        }

        return request;
    }

    private Optional<String> findFailedPaymentInSignedList(PaymentRequest paymentRequest) {
        for (UpcomingTransactionEntity transaction :
                fetchUpcomingTransactions(paymentRequest.getFromAccount())) {
            if (LFUtils.isSamePayment(paymentRequest, transaction)) {
                return Optional.of(transaction.getId());
            }
        }

        return Optional.empty();
    }

    private Optional<String> findFailedTransferInSignedList(TransferRequest transferRequest) {
        for (UpcomingTransactionEntity transaction :
                fetchUpcomingTransactions(transferRequest.getFromAccount())) {
            if (LFUtils.isSameTransfer(transferRequest, transaction)) {
                return Optional.of(transaction.getId());
            }
        }

        return Optional.empty();
    }

    private boolean cancelFailedPayment(PaymentRequest paymentRequest, Exception initialException)
            throws TransferExecutionException, HttpStatusCodeErrorException {
        List<PaymentEntity> unsignedPayments = fetchUnsignedPaymentsAndTransfers();

        Optional<String> uniqueId =
                findFailedPaymentInUnsignedList(paymentRequest, unsignedPayments);

        if (!uniqueId.isPresent()) {
            return false;
        }

        try {
            cancelUnsignedPayment(uniqueId.get(), initialException);
            return true;
        } catch (TransferExecutionException deleteException) {
            log.warn(
                    "Could not delete unsigned transfer from outbox but was expecting it to be possible.",
                    deleteException);
        }

        return false;
    }

    private Optional<String> findFailedPaymentInUnsignedList(
            PaymentRequest paymentRequest, List<PaymentEntity> unsignedPayments) {
        for (PaymentEntity payment : unsignedPayments) {
            if (LFUtils.isSamePayment(paymentRequest, payment)) {
                return Optional.of(payment.getUniqueId());
            }
        }

        return Optional.empty();
    }

    private List<PaymentEntity> fetchUnsignedPaymentsAndTransfers()
            throws HttpStatusCodeErrorException {
        PaymentsListResponse paymentsList =
                createGetRequest(
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
        supplementalRequester.openBankId();

        collectTransferResponse(SEND_PAYMENT_URL, paymentRequest);
    }

    private void collectTransferResponse(String url, Object transferRequest)
            throws BankIdException {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            ClientResponse clientResponse = createPostRequest(url, transferRequest);

            int status = clientResponse.getStatus();

            if (status == HttpStatus.SC_OK) {
                return;
            } else if (status == HttpStatus.SC_UNAUTHORIZED
                    || status == HttpStatus.SC_BAD_REQUEST) {
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
                    case "12251007":
                        throw BankIdError.CANCELLED.exception();
                    case "00159":
                    case "001512":
                        throw BankIdError.TIMEOUT.exception();
                    case "12251006":
                        throw BankIdError.UNKNOWN.exception();
                    case "00151":
                        throw BankServiceError.BANK_SIDE_FAILURE.exception();
                    default:
                        if (clientResponse.getHeaders().getFirst("Error-Message") != null) {
                            throw failTransferWithMessage(
                                    String.format(
                                            "Error code: %s, error message: %s",
                                            clientResponse.getHeaders().getFirst("Error-Code"),
                                            clientResponse.getHeaders().getFirst("Error-Message")),
                                    TransferExecutionException.EndUserMessage
                                            .BANKID_TRANSFER_FAILED);
                        } else {
                            throw failTransfer(
                                    TransferExecutionException.EndUserMessage
                                            .BANKID_TRANSFER_FAILED);
                        }
                }
            } else if (status == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception();
            } else {
                throw failTransfer(TransferExecutionException.EndUserMessage.BANKID_FAILED);
            }
            Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
        }

        throw BankIdError.TIMEOUT.exception();
    }

    private ClientResponse postRequestAndValidateResponse(String url, Object requestEntity) {
        ClientResponse clientResponse = createPostRequest(url, requestEntity);
        if (shouldRetry(clientResponse)) {
            clientResponse = retryPostRequest(clientResponse, url, requestEntity);
        }
        validateTransactionClientResponse(clientResponse);

        return clientResponse;
    }

    private ClientResponse retryPostRequest(
            ClientResponse clientResponse, String url, Object requestEntity) {
        long maxSleepMilliseconds;
        for (int retryCount = 0;
                shouldRetry(clientResponse) && (retryCount <= maxNumRetries);
                retryCount++) {
            maxSleepMilliseconds = (retryCount + 1) * retrySleepMilliseconds;
            log.warn("Received Error-Code: 12231, retrying....");
            Uninterruptibles.sleepUninterruptibly(maxSleepMilliseconds, TimeUnit.MILLISECONDS);
            clientResponse = createPostRequest(url, requestEntity);
        }
        return clientResponse;
    }

    private boolean shouldRetry(ClientResponse clientResponse) {
        return LFUtils.isClientResponseCancel(clientResponse)
                && "12231".equals(clientResponse.getHeaders().getFirst("Error-Code"));
    }

    /** Helper method to validate a client response in the payment process. */
    private void validateTransactionClientResponse(ClientResponse clientResponse)
            throws TransferExecutionException {
        if (LFUtils.isClientResponseCancel(clientResponse)) {
            switch (clientResponse.getHeaders().getFirst("Error-Code")) {
                case "122111":
                    throw cancelTransfer(
                            EndUserMessage.EXCESS_AMOUNT, InternalStatus.INSUFFICIENT_FUNDS);
                case "99351":
                    throw cancelTransfer(
                            EndUserMessage.INVALID_DUEDATE_TOO_SOON_OR_NOT_BUSINESSDAY,
                            InternalStatus.INVALID_DUE_DATE);
                case "12215":
                    throw cancelTransfer(EndUserMessage.INVALID_OCR, InternalStatus.INVALID_OCR);
                case "122422":
                    throw BankServiceError.BANK_SIDE_FAILURE.exception();
                case "12231":
                    throw cancelTransfer(
                            EndUserMessage.USER_UNAUTHORIZED, InternalStatus.USER_UNAUTHORIZED);
                default:
                    throw cancelTransferWithMessage(
                            String.format(
                                    "Error code: %s, error message: %s",
                                    clientResponse.getHeaders().getFirst("Error-Code"),
                                    clientResponse.getHeaders().getFirst("Error-Message")),
                            TransferExecutionException.EndUserMessage.TRANSFER_EXECUTE_FAILED,
                            InternalStatus.BANK_ERROR_CODE_NOT_HANDLED_YET);
            }
        } else if (clientResponse.getStatus() != HttpStatus.SC_OK) {
            throw failTransfer(TransferExecutionException.EndUserMessage.TRANSFER_EXECUTE_FAILED);
        }
    }

    private TransferrableResponse fetchTransferSourceAccounts()
            throws HttpStatusCodeErrorException {
        return createGetRequest(FETCH_TRANSFER_SOURCE_ACCOUNTS, TransferrableResponse.class);
    }

    private void executeBankTransfer(final Transfer transfer)
            throws HttpStatusCodeErrorException, BankIdException {
        AccountIdentifier source = transfer.getSource();
        AccountIdentifier destination = transfer.getDestination();

        if (!destination.is(AccountIdentifier.Type.SE)) {
            throw cancelTransferWithMessage(
                    "Transfer account identifiers other than Swedish are not supported.",
                    EndUserMessage.INVALID_DESTINATION,
                    InternalStatus.INVALID_DESTINATION_ACCOUNT);
        }

        TransferrableResponse sourceAccounts = fetchTransferSourceAccounts();
        validateSourceAccount(source, sourceAccounts);

        // Find the destination account or the bank of the destination account.
        TransferrableResponse destinationAccounts = fetchTransferDestinationAccounts();

        Preconditions.checkState(
                destinationAccounts != null, "Could not collect transfer accounts");

        // Ensure correctly formatted transfer messages
        boolean isBetweenSameUserAccounts =
                LFUtils.find(destination, sourceAccounts.getAccounts()).isPresent();
        TransferMessageFormatter.Messages formattedMessages =
                transferMessageFormatter.getMessagesFromRemittanceInformation(
                        transfer, isBetweenSameUserAccounts);

        TransferRequest transferRequest =
                TransferRequest.create(
                        findBankName(destination, destinationAccounts),
                        transfer.getAmount().getValue(),
                        formattedMessages,
                        source.getIdentifier(DEFAULT_FORMATTER),
                        LFUtils.getApiAdaptedToAccount(destination.to(SwedishIdentifier.class)));

        if (Strings.isNullOrEmpty(transferRequest.getBankName())) {
            // Local transfer, no need for signing.
            createPostRequest(INTERNAL_TRANSFER_URL, transferRequest);
        } else {
            executeExternalBankTransfer(transferRequest);
        }
    }

    private void executeExternalBankTransfer(TransferRequest transferRequest)
            throws BankIdException {
        ClientResponse createTransferResponse =
                createPostRequest(CREATE_BANKID_REFERENCE_URL, transferRequest);

        if (createTransferResponse.getStatus() != HttpStatus.SC_OK) {
            String errorCode = createTransferResponse.getHeaders().getFirst("Error-Code");

            if (!Strings.isNullOrEmpty(errorCode)) {
                switch (errorCode) {
                    case "502203":
                        throw cancelTransfer(
                                EndUserMessage.INVALID_DESTINATION,
                                InternalStatus.INVALID_DESTINATION_ACCOUNT);
                    case "502204":
                    case "99142":
                        throw cancelTransfer(
                                EndUserMessage.EXCESS_AMOUNT, InternalStatus.INSUFFICIENT_FUNDS);
                    default:
                        throw failTransferWithMessage(
                                String.format(
                                        "Transfer failed with error code: %s and message: %s",
                                        errorCode,
                                        createTransferResponse
                                                .getHeaders()
                                                .getFirst("Error-Message")),
                                EndUserMessage.TRANSFER_EXECUTE_FAILED);
                }
            }

            throw failTransfer(EndUserMessage.TRANSFER_EXECUTE_FAILED);
        }

        supplementalRequester.openBankId();

        try {
            collectTransferResponse(BANKID_COLLECT_DIRECT_TRANSFER_URL, transferRequest);
        } catch (Exception initialException) {
            deleteSignedTransaction(transferRequest);
            throw initialException;
        }
    }

    private void validateSourceAccount(
            AccountIdentifier source, TransferrableResponse sourceAccounts) {
        Optional<AccountEntity> fromAccountDetails =
                LFUtils.find(source, sourceAccounts.getAccounts());

        if (!fromAccountDetails.isPresent()) {
            throw cancelTransfer(
                    TransferExecutionException.EndUserMessage.SOURCE_NOT_FOUND,
                    InternalStatus.INVALID_SOURCE_ACCOUNT);
        }
    }

    private String findBankName(
            AccountIdentifier destination, TransferrableResponse destinationAccounts)
            throws HttpStatusCodeErrorException {
        Optional<AccountEntity> destinationAccount =
                LFUtils.find(destination, destinationAccounts.getAccounts());
        if (destinationAccount.isPresent()) {
            if (destinationAccount.get().isLocalAccount()) {
                return "";
            } else {
                return destinationAccount.get().getBankName();
            }
        } else {
            BankListResponse bankListResponse =
                    createGetRequest(FETCH_ALL_BANKNAMES_URL, BankListResponse.class);

            Optional<BankEntity> bankEntity =
                    findBankForAccountNumber(
                            destination.getIdentifier(DEFAULT_FORMATTER),
                            bankListResponse.getAllBankNames());

            if (bankEntity.isPresent()) {
                return bankEntity.get().getBankName();
            } else {
                // If not found, let's try anyway to create the transaction (should work for major
                // banks at least).
                if (destination.is(AccountIdentifier.Type.SE)) {
                    SwedishIdentifier swedishDestination = destination.to(SwedishIdentifier.class);
                    ClearingNumber.Details clearingNumber =
                            LFUtils.getClearingNumberDetails(swedishDestination);
                    return clearingNumber.getBankName();
                } else {
                    throw cancelTransfer(
                            TransferExecutionException.EndUserMessage.INVALID_DESTINATION,
                            InternalStatus.INVALID_DESTINATION_ACCOUNT);
                }
            }
        }
    }

    private String fetchToken() {
        TokenResponse tokenResponse;
        try {
            tokenResponse = createGetRequest(FETCH_TOKEN_URL, TokenResponse.class);
        } catch (HttpStatusCodeErrorException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        String tokenChallangeHash =
                new String(
                        Hex.encodeHex(
                                StringUtils.hashSHA1(
                                        Integer.toHexString(tokenResponse.getNumber() + 4112))));

        TokenChallengeResponse tokenChallengeResponse =
                createPostRequest(
                                FETCH_TOKEN_URL,
                                new TokenChallengeRequest(
                                        tokenResponse.getNumber(),
                                        tokenChallangeHash,
                                        tokenResponse.getNumberPair()))
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
        credentials.setSensitivePayload(Key.ACCESS_TOKEN, ticket);

        return true;
    }

    @Override
    public void logout() throws Exception {
        lansforsakringarBaseApiClient
                .createClientRequest(PASSWORD_LOGIN_URL, token, ticket)
                .delete();
    }

    private <T> T createGetRequestFromUrlAndDepotNumber(
            Class<T> responseClass, String url, String depotNumber)
            throws HttpStatusCodeErrorException, URISyntaxException {
        String requestUrl =
                new URIBuilder(url).addParameter("depotNumber", depotNumber).build().toString();
        return createGetRequest(requestUrl, responseClass);
    }

    private InstrumentDetailsResponse getInstrumentDetails(String depotNumber, String isin)
            throws HttpStatusCodeErrorException {
        String stockDetailsUrl;
        try {
            stockDetailsUrl =
                    new URIBuilder(STOCK_DETAILS_URL)
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

    private List<CardEntity> fetchCardEntities() throws HttpStatusCodeErrorException {
        return createGetRequest(OVERVIEW_URL, OverviewEntity.class).getCards();
    }

    private List<AccountEntity> fetchAccountEntities() throws HttpStatusCodeErrorException {
        return createGetRequest(OVERVIEW_URL, OverviewEntity.class).getAccountEntities();
    }

    @Override
    public boolean isLoggedIn() throws Exception {
        return keepAlive();
    }

    @Override
    public boolean keepAlive() throws Exception {
        try {
            return createGetRequest(OVERVIEW_URL).getStatus() == HttpStatus.SC_OK;
        } catch (ClientHandlerException exception) {
            // There are various messages in this exception like 'Connection reset', 'Connection
            // timed out', 'No route to host', 'Temporary failure in name resolution' etc and in
            // most of cases we didn't receive any response from LF's API. So, in any case instead
            // of failing the refresh entirely, is better to send false in case of exception.
            return false;
        }
    }

    @Override
    public void persistLoginSession() {
        Session session = new Session();
        session.setToken(token);
        session.setTicket(ticket);
        session.setLoginName(loginName);
        session.setLoginSsn(loginSsn);
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
        loginName = session.getLoginName();
        loginSsn = session.getLoginSsn();

        addSessionCookiesToClient(client, session);
    }

    @Override
    public void clearLoginSession() {
        // Clean the session in memory
        token = null;
        ticket = null;
        loginName = null;
        loginSsn = null;

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

    ///// Refresh Executor Refactor /////
    @Override
    public FetchEInvoicesResponse fetchEInvoices() {
        try {
            List<AccountEntity> accountEntities = fetchAccountEntities();

            if (accountEntities.isEmpty()) {
                log.info("EInvoices not updated: The user has no accounts.");
                return new FetchEInvoicesResponse(Collections.emptyList());
            }

            if (!hasPendingEInvoices()) {
                return new FetchEInvoicesResponse(Collections.emptyList());
            }

            EInvoicesListResponse invoicesListResponse =
                    createGetRequest(FETCH_EINVOICES_URL, EInvoicesListResponse.class);

            List<EInvoice> eInvoiceEntities = invoicesListResponse.getElectronicInvoices();
            if (eInvoiceEntities.isEmpty()) {
                log.error("User should have eInvoices, but we got no transfers.");
                return new FetchEInvoicesResponse(Collections.emptyList());
            }

            List<Transfer> eInvoices = Lists.newArrayList();
            for (EInvoice eInvoice : eInvoiceEntities) {
                eInvoices.add(eInvoice.toTransfer());
            }
            return new FetchEInvoicesResponse(eInvoices);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(
            List<Account> updatedAccounts) {
        try {
            List<AccountEntity> accountEntities = fetchAccountEntities();

            TransferDestinationsResponse response = new TransferDestinationsResponse();

            response.addDestinations(
                    getTransferAccountDestinations(accountEntities, updatedAccounts));
            response.addDestinations(
                    getPaymentAccountDestinations(accountEntities, updatedAccounts));

            return new FetchTransferDestinationsResponse(response.getDestinations());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return this.refreshTransactionalAccounts(
                se.tink.libraries.credentials.service.RefreshableItem.CHECKING_ACCOUNTS);
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return this.refreshTransactionalAccounts(SAVING_ACCOUNTS);
    }

    private FetchAccountsResponse refreshTransactionalAccounts(RefreshableItem type) {
        List<Account> refreshedAccounts =
                getAccounts().entrySet().stream()
                        .filter(set -> type.isAccountType(set.getValue().getType()))
                        .map(Map.Entry::getValue)
                        .collect(Collectors.toList());
        return new FetchAccountsResponse(refreshedAccounts);
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return this.refreshTransactionalAccountTransactions(CHECKING_TRANSACTIONS);
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return this.refreshTransactionalAccountTransactions(SAVING_TRANSACTIONS);
    }

    private FetchTransactionsResponse refreshTransactionalAccountTransactions(
            RefreshableItem type) {
        Map<Account, List<Transaction>> accountTransactions = new HashMap<>();
        getAccounts().entrySet().stream()
                .filter(set -> type.isAccountType(set.getValue().getType()))
                .forEach(
                        set -> {
                            AccountEntity accountEntity = set.getKey();
                            Account account = set.getValue();

                            List<Transaction> transactions = Lists.newArrayList();
                            String accountNumber = accountEntity.getAccountNumber();
                            // Fetch upcoming transactions.

                            for (UpcomingTransactionEntity transactionEntity :
                                    fetchUpcomingTransactions(accountNumber)) {
                                Transfer transfer =
                                        createTransferForUpcomingPayment(transactionEntity);
                                Transaction transaction = transactionEntity.toTransaction();

                                if (transfer != null) {
                                    transfer.setId(UUIDUtils.fromTinkUUID(transaction.getId()));

                                    try {
                                        transaction.setPayload(
                                                TransactionPayloadTypes
                                                        .EDITABLE_TRANSACTION_TRANSFER,
                                                MAPPER.writeValueAsString(transfer));
                                    } catch (Exception e) {
                                        throw new IllegalStateException(e);
                                    }
                                    transaction.setPayload(
                                            TransactionPayloadTypes
                                                    .EDITABLE_TRANSACTION_TRANSFER_ID,
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
                                    transactionListResponse =
                                            createPostRequestWithResponseHandling(
                                                    TRANSACTIONS_URL,
                                                    DebitTransactionListResponse.class,
                                                    new ListAccountTransactionRequest(
                                                            currentPage, accountNumber));
                                } catch (Exception e) {
                                    throw new IllegalStateException(e);
                                }

                                debug(
                                        request,
                                        "Requested page size "
                                                + MoreObjects.firstNonNull(
                                                                transactionListResponse
                                                                        .getTransactions(),
                                                                Lists.newArrayList())
                                                        .size());

                                for (TransactionEntity transactionEntity :
                                        transactionListResponse.getTransactions()) {
                                    transactions.add(transactionEntity.toTransaction());
                                }

                                if (isContentWithRefresh(account, transactions)) {
                                    hasMoreTransactions = false;
                                } else {
                                    hasMoreTransactions = transactionListResponse.getHasMore();
                                    currentPage = transactionListResponse.getNextSequenceNumber();
                                }
                            } while (hasMoreTransactions);
                            accountTransactions.put(account, transactions);
                        });
        return new FetchTransactionsResponse(accountTransactions);
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        List<Account> cards = Lists.newArrayList();
        try {
            List<CardEntity> cardEntities = fetchCardEntities();

            for (CardEntity cardEntity : cardEntities) {
                if (cardEntity.getCardType().equals("DEBIT")) {
                    /*
                     * Cards of type DEBIT are connected to an account
                     *  Handled by: cacheAccounts()
                     */
                    continue;
                }
                cards.add(cardEntity.getAccount());
            }
            return new FetchAccountsResponse(cards);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        Map<Account, List<Transaction>> accountTransactions = new HashMap<>();
        try {
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

                    CreditTransactionListResponse transactionListResponse =
                            createPostRequestWithResponseHandling(
                                    FETCH_CARD_TRANSACTIONS_URL,
                                    CreditTransactionListResponse.class,
                                    new ListCardTransactionRequest(
                                            currentPage, account.getAccountNumber()));

                    debug(
                            request,
                            "Requested page size "
                                    + MoreObjects.firstNonNull(
                                                    transactionListResponse.getTransactions(),
                                                    Lists.newArrayList())
                                            .size());

                    for (CardTransactionEntity transactionEntity :
                            transactionListResponse.getTransactions()) {
                        transactions.add(transactionEntity.toTransaction());
                    }

                    if (isContentWithRefresh(account, transactions)) {
                        hasMoreTransactions = false;
                    } else {
                        hasMoreTransactions = transactionListResponse.hasMorePages();
                        currentPage++;
                    }
                } while (hasMoreTransactions);

                accountTransactions.put(account, transactions);
            }
            return new FetchTransactionsResponse(accountTransactions);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public FetchLoanAccountsResponse fetchLoanAccounts() {
        Map<Account, AccountFeatures> loanAccounts = new HashMap<>();

        LoanListResponse loans = null;

        try {
            loans = createGetRequest(FETCH_LOANS_URL, LoanListResponse.class);
        } catch (Exception e) {
            // Seeing LF return 400 and 500 responses from time to time, therefore this try-catch
            log.warn("Was not able to retrieve loans from Lansforsakringar: " + e.getMessage(), e);
        }

        if (loans == null) {
            return new FetchLoanAccountsResponse(Collections.emptyMap());
        }

        for (LoanEntity le : loans.getLoans()) {
            try {
                String detailsString =
                        createPostRequestWithResponseHandling(
                                FETCH_LOAN_DETAILS_URL,
                                String.class,
                                new LoanDetailsRequest(le.getLoanNumber()));

                LoanDetailsEntity details =
                        MAPPER.readValue(detailsString, LoanDetailsEntity.class);

                Account account = details.toAccount();
                Loan loan = details.toLoan(detailsString);

                loanAccounts.put(account, AccountFeatures.createForLoan(loan));
            } catch (Exception e) {
                log.warn("Was not able to retrieve loan: " + e.getMessage(), e);
            }
        }
        return new FetchLoanAccountsResponse(loanAccounts);
    }

    @Override
    public FetchTransactionsResponse fetchLoanTransactions() {
        return new FetchTransactionsResponse(Collections.emptyMap());
    }

    @Override
    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        Map<Account, AccountFeatures> investmentAccounts = new HashMap<>();

        try {
            Map<Account, AccountFeatures> iskAccounts = refreshIskAccountsCopy();
            if (iskAccounts != null) {
                investmentAccounts.putAll(iskAccounts);
            }

            Optional<Pair<Account, AccountFeatures>> fundDepot = refreshFundDepotCopy();
            fundDepot.ifPresent(pair -> investmentAccounts.put(pair.first, pair.second));

            Optional<Pair<Account, AccountFeatures>> stockDepot = refreshStockDepotCopy();
            stockDepot.ifPresent(pair -> investmentAccounts.put(pair.first, pair.second));
        } catch (Exception e) {
            // Just catch and exit gently
            log.warn("Caught exception while logging investment data", e);
        }
        return new FetchInvestmentAccountsResponse(investmentAccounts);
    }

    @Override
    public FetchTransactionsResponse fetchInvestmentTransactions() {
        return new FetchTransactionsResponse(Collections.emptyMap());
    }

    private Map<Account, AccountFeatures> refreshIskAccountsCopy()
            throws HttpStatusCodeErrorException {

        Map<Account, AccountFeatures> iskAccounts = new HashMap<>();

        String investmentSavingsDepotResponseString = createGetRequest(ISK_URL, String.class);

        InvestmentSavingsDepotResponse investmentSavingsDepotResponse;

        try {
            investmentSavingsDepotResponse =
                    MAPPER.readValue(
                            investmentSavingsDepotResponseString,
                            InvestmentSavingsDepotResponse.class);
        } catch (IOException e) {
            log.warn(
                    "#lf - investment savings deserialization failed - "
                            + investmentSavingsDepotResponseString,
                    e);
            return null;
        }

        InvestmentSavingsDepotEntity investmentSavingsDepotWrapper =
                investmentSavingsDepotResponse.getInvestmentSavingsDepotWrapper();

        if (investmentSavingsDepotWrapper == null
                || investmentSavingsDepotWrapper.getInvestmentSavingsDepotWrappers() == null
                || investmentSavingsDepotWrapper.getInvestmentSavingsDepotWrappers().isEmpty()) {
            return null;
        }

        for (InvestmentSavingsDepotWrappersEntity investmentDepotWrapper :
                investmentSavingsDepotWrapper.getInvestmentSavingsDepotWrappers()) {
            Double totalValue = investmentDepotWrapper.getDepot().getTotalValue();
            Double balance = investmentDepotWrapper.getAccount().getBalance();
            Double marketValue =
                    totalValue != null && balance != null ? (totalValue - balance) : null;

            Account account = investmentDepotWrapper.getAccount().toAccount(totalValue);
            String depotNumber = investmentDepotWrapper.getDepot().getDepotNumber();
            Double cashValue = null;

            try {
                CashBalanceResponse cashBalanceResponse =
                        createGetRequestFromUrlAndDepotNumber(
                                CashBalanceResponse.class, ISK_CASH_BALANCE_URL, depotNumber);
                cashValue = cashBalanceResponse.getCashValue();
            } catch (URISyntaxException e) {
                log.warn("Could not build URI", e);
            }

            Portfolio portfolio =
                    investmentDepotWrapper
                            .getDepot()
                            .toPortfolio(marketValue, cashValue, Portfolio.Type.ISK);

            SecurityHoldingsResponse funds;
            SecurityHoldingsResponse stocks;
            try {
                funds =
                        createGetRequestFromUrlAndDepotNumber(
                                SecurityHoldingsResponse.class, FUND_SECURITIES_URL, depotNumber);
                stocks =
                        createGetRequestFromUrlAndDepotNumber(
                                SecurityHoldingsResponse.class, STOCK_SECURITIES_URL, depotNumber);
            } catch (URISyntaxException e) {
                return null;
            }

            List<Instrument> instruments = Lists.newArrayList();
            // Add funds
            funds.getSecurityHoldings()
                    .getFunds()
                    .forEach(fundEntity -> fundEntity.toInstrument().ifPresent(instruments::add));

            // Add stocks
            stocks.getSecurityHoldings()
                    .getShares()
                    .forEach(
                            shareEntity -> {
                                InstrumentDetailsResponse instrumentDetails;
                                try {
                                    instrumentDetails =
                                            getInstrumentDetails(
                                                    depotNumber, shareEntity.getIsinCode());
                                    if (instrumentDetails == null) {
                                        return;
                                    }
                                } catch (HttpStatusCodeErrorException e) {
                                    // If the user don't have an fund depot this request will get a
                                    // response with status code 400.
                                    // Just return and don't do anything.
                                    return;
                                }

                                shareEntity
                                        .toInstrument(instrumentDetails.getInstrument())
                                        .ifPresent(instruments::add);
                            });

            // Add bonds
            stocks.getSecurityHoldings()
                    .getBonds()
                    .forEach(
                            bondEntity -> {
                                InstrumentDetailsResponse instrumentDetails;
                                try {
                                    instrumentDetails =
                                            getInstrumentDetails(
                                                    depotNumber, bondEntity.getIsinCode());
                                    if (instrumentDetails == null) {
                                        return;
                                    }
                                } catch (HttpStatusCodeErrorException e) {
                                    // If the user don't have an fund depot this request will get a
                                    // response with status code 400.
                                    // Just return and don't do anything.
                                    return;
                                }
                                bondEntity.toInstrument().ifPresent(instruments::add);
                            });

            portfolio.setInstruments(instruments);

            iskAccounts.put(account, AccountFeatures.createForPortfolios(portfolio));
        }
        return iskAccounts;
    }

    private Optional<Pair<Account, AccountFeatures>> refreshFundDepotCopy() {
        FundHoldingsResponse fundHoldingsResponse;
        try {
            fundHoldingsResponse = createGetRequest(FUND_DEPOT_URL, FundHoldingsResponse.class);
        } catch (HttpStatusCodeErrorException e) {
            // If the user don't have an fund depot this request will get a response with status
            // code 400.
            // Just return and don't do anything.
            return Optional.empty();
        }

        Account account = fundHoldingsResponse.toAccount();
        Portfolio portfolio = fundHoldingsResponse.toPortfolio();

        List<Instrument> instruments = Lists.newArrayList();

        fundHoldingsResponse
                .getFunds()
                .forEach(
                        fundEntity -> {
                            String fundInformationUrlWithFundId;
                            try {
                                fundInformationUrlWithFundId =
                                        new URIBuilder(FUND_INFORMATION_URL)
                                                .addParameter(
                                                        "fundid",
                                                        String.valueOf(fundEntity.getFundId()))
                                                .build()
                                                .toString();
                            } catch (URISyntaxException e) {
                                log.error("failed to create uri builder", e);
                                return;
                            }

                            FundInformationWrapper fundInformationWrapper;
                            try {
                                fundInformationWrapper =
                                        createGetRequest(
                                                fundInformationUrlWithFundId,
                                                FundInformationWrapper.class);
                            } catch (HttpStatusCodeErrorException e) {
                                log.warn("failed to fetch fund details", e);
                                return;
                            }

                            fundEntity
                                    .toInstrument(fundInformationWrapper.getFundInformation())
                                    .ifPresent(instruments::add);
                        });
        portfolio.setInstruments(instruments);
        return Optional.of(new Pair<>(account, AccountFeatures.createForPortfolios(portfolio)));
    }

    private Optional<Pair<Account, AccountFeatures>> refreshStockDepotCopy() {
        ShareDepotResponse shareDepotResponse;
        SecurityHoldingsResponse securityHoldingsResponse;
        CashBalanceResponse cashBalanceResponse;
        try {
            shareDepotResponse = createGetRequest(STOCK_DEPOT_URL, ShareDepotResponse.class);
            if (shareDepotResponse.getShareDepotWrapper() == null
                    || shareDepotResponse.getShareDepotWrapper().getDepot() == null
                    || shareDepotResponse.getShareDepotWrapper().getDepot().getDepotNumber()
                            == null) {
                return Optional.empty();
            }

            String depotNumber =
                    shareDepotResponse.getShareDepotWrapper().getDepot().getDepotNumber();
            securityHoldingsResponse =
                    createGetRequestFromUrlAndDepotNumber(
                            SecurityHoldingsResponse.class, STOCK_DEPOT_HOLDINGS_URL, depotNumber);
            cashBalanceResponse =
                    createGetRequestFromUrlAndDepotNumber(
                            CashBalanceResponse.class, STOCK_DEPOT_CASH_BALANCE_URL, depotNumber);
        } catch (HttpStatusCodeErrorException e) {
            // If the user don't have an fund depot this request will get a response with status
            // code 400.
            // Just return and don't do anything.
            if (e.getResponse().getStatus() != 400) {
                log.error("could not fetch fund depot", e);
            }

            return Optional.empty();
        } catch (URISyntaxException e) {
            log.warn("failed to build url", e);
            return Optional.empty();
        }

        Optional<Account> account =
                securityHoldingsResponse.toShareDepotAccount(
                        shareDepotResponse.getShareDepotWrapper());

        if (!account.isPresent()) {
            return Optional.empty();
        }

        Portfolio portfolio =
                securityHoldingsResponse.toShareDepotPortfolio(
                        shareDepotResponse.getShareDepotWrapper(),
                        cashBalanceResponse.getCashValue());

        List<Instrument> instruments = Lists.newArrayList();

        String depotNumber = shareDepotResponse.getShareDepotWrapper().getDepot().getDepotNumber();
        securityHoldingsResponse
                .getSecurityHoldings()
                .getShares()
                .forEach(
                        shareEntity -> {
                            InstrumentDetailsResponse instrumentDetails;

                            try {
                                instrumentDetails =
                                        getInstrumentDetails(
                                                depotNumber, shareEntity.getIsinCode());
                                if (instrumentDetails == null) {
                                    return;
                                }
                            } catch (HttpStatusCodeErrorException e) {
                                // If the user don't have an fund depot this request will get a
                                // response with status code 400.
                                // Just return and don't do anything.
                                return;
                            }

                            shareEntity
                                    .toInstrument(instrumentDetails.getInstrument())
                                    .ifPresent(instruments::add);
                        });

        securityHoldingsResponse
                .getSecurityHoldings()
                .getBonds()
                .forEach(
                        bondEntity -> {
                            InstrumentDetailsResponse instrumentDetails;
                            try {
                                instrumentDetails =
                                        getInstrumentDetails(depotNumber, bondEntity.getIsinCode());
                                if (instrumentDetails == null) {
                                    return;
                                }
                            } catch (HttpStatusCodeErrorException e) {
                                // If the user don't have an fund depot this request will get a
                                // response with status code 400.
                                // Just return and don't do anything.
                                return;
                            }
                            bondEntity.toInstrument().ifPresent(instruments::add);
                        });
        portfolio.setInstruments(instruments);
        return Optional.of(
                new Pair<>(account.get(), AccountFeatures.createForPortfolios(portfolio)));
    }
    /////////////////////////////////////

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        if (Strings.isNullOrEmpty(loginName)) {
            throw new NoSuchElementException("Could not find user name from login response.");
        }
        if (Strings.isNullOrEmpty(loginSsn)) {
            throw new NoSuchElementException("Could not find SSN from login response.");
        }
        IdentityData identityData = SeIdentityData.of(loginName, loginSsn);
        return new FetchIdentityDataResponse(identityData);
    }

    private TransferExecutionException cancelTransfer(
            TransferExecutionException.EndUserMessage endUserMessage,
            InternalStatus internalStatus) {
        return cancelTransferWithMessage(
                endUserMessage.getKey().get(), endUserMessage, internalStatus);
    }

    private TransferExecutionException cancelTransferWithMessage(
            String message,
            TransferExecutionException.EndUserMessage endUserMessage,
            InternalStatus internalStatus) {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(message)
                .setEndUserMessage(catalog.getString(endUserMessage))
                .setInternalStatus(internalStatus.toString())
                .build();
    }

    private TransferExecutionException failTransfer(
            TransferExecutionException.EndUserMessage endUserMessage) {
        return failTransferWithMessage(endUserMessage.getKey().get(), endUserMessage);
    }

    private TransferExecutionException failTransferWithMessage(
            String message, TransferExecutionException.EndUserMessage endUserMessage) {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(message)
                .setEndUserMessage(catalog.getString(endUserMessage))
                .build();
    }
}
