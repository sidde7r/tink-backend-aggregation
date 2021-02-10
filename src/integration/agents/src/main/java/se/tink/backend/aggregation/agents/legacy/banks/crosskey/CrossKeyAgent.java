package se.tink.backend.aggregation.agents.banks.crosskey;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.joda.time.DateTime;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.DeprecatedRefreshExecutor;
import se.tink.backend.aggregation.agents.banks.crosskey.responses.AccountResponse;
import se.tink.backend.aggregation.agents.banks.crosskey.responses.AccountsResponse;
import se.tink.backend.aggregation.agents.banks.crosskey.responses.BaseResponse;
import se.tink.backend.aggregation.agents.banks.crosskey.responses.CrossKeyConfig;
import se.tink.backend.aggregation.agents.banks.crosskey.responses.CrossKeyLoanDetails;
import se.tink.backend.aggregation.agents.banks.crosskey.utils.CrossKeyUtils;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Loan;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.constants.CommonHeaders;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class CrossKeyAgent extends AbstractAgent implements DeprecatedRefreshExecutor {
    protected final CrossKeyApiClient apiClient;
    private final Credentials credentials;

    // Custom Exceptions
    private class FetchAccountsException extends Exception {}

    private class FetchTransactionsException extends Exception {}

    // Sensitive payload keys
    private static final String DEVICE_ID_PAYLOAD_KEY = "deviceId";
    private static final String DEVICE_TOKEN_PAYLOAD_KEY = "deviceToken";

    private final CrossKeyConfig config;
    private boolean hasRefreshed = false;

    public CrossKeyAgent(
            CredentialsRequest request,
            AgentContext context,
            SignatureKeyPair signatureKeyPair,
            CrossKeyConfig config) {
        super(request, context);

        this.config = config;
        credentials = request.getCredentials();
        apiClient =
                new CrossKeyApiClient(
                        clientFactory.createCookieClient(context.getLogOutputStream()),
                        credentials,
                        log,
                        CommonHeaders.DEFAULT_USER_AGENT);
    }

    /** Login functions */
    public boolean login() throws Exception {
        apiClient.systemStatus();

        if (credentials.getType().equals(CredentialsTypes.MOBILE_BANKID)) {
            loginWithBankId();
        } else {
            loginWithCredentials();
        }
        return true;
    }

    private void loginWithCredentials() throws Exception {
        String deviceId = credentials.getSensitivePayload(DEVICE_ID_PAYLOAD_KEY);
        String deviceToken = credentials.getSensitivePayload(DEVICE_TOKEN_PAYLOAD_KEY);

        if (deviceId != null && deviceToken != null) {
            loginExistingUser(deviceId, deviceToken);
        } else {
            loginNewUser();
        }
    }

    private void loginExistingUser(String deviceId, String deviceToken) throws Exception {
        BaseResponse response = apiClient.loginWithToken(deviceId, deviceToken);
        updateCredentialsDeviceInfo(response.getDeviceToken());
    }

    private void loginNewUser() throws Exception {
        loginWithCredentialsAndOneTimeCode();
        generateTokens();
    }

    private void loginWithCredentialsAndOneTimeCode() throws Exception {
        String oneTimeCodePosition = apiClient.getTanPosition();
        String oneTimeCode = requestOneTimeCodeFromUser(oneTimeCodePosition);
        apiClient.loginWithOneTimeCode(oneTimeCode);
    }

    private void loginWithBankId() throws Exception {
        BaseResponse response = apiClient.autoStartBankId();
        openBankIdAppWith(response.getAutoStartToken());
        apiClient.collectBankId();
    }

    private void generateTokens() throws Exception {
        String udId = CrossKeyUtils.generateUdIdFor(credentials.getUserId());
        BaseResponse response = apiClient.addDevice(udId);

        String deviceId = response.getDeviceId();
        response = apiClient.generateToken(deviceId);
        updateCredentialsDeviceInfo(deviceId, response.getDeviceToken());
    }

    private void openBankIdAppWith(String token) {
        supplementalInformationController.openMobileBankIdAsync(Preconditions.checkNotNull(token));
    }

    /** Refresh functions */
    @Override
    public void refresh() throws Exception {
        // The refresh command will call refresh multiple times.
        // This check ensures the refresh only runs once.
        if (hasRefreshed) {
            return;
        }
        hasRefreshed = true;

        statusUpdater.updateStatus(CredentialsStatus.UPDATING);
        List<Account> accounts;

        try {
            accounts = refreshAccounts();
            for (Account account : accounts) {
                refreshTransactionsFor(account);
            }
        } catch (FetchAccountsException e) {
            logAndReThrow("Error while fetching accounts", e);
        } catch (FetchTransactionsException e) {
            logAndReThrow("Error while fetching transactions", e);
        }
    }

    private List<Account> refreshAccounts() throws Exception {
        AccountsResponse accountsResponse = apiClient.getAccounts();

        List<Account> tinkAccounts = new ArrayList<Account>();
        for (AccountResponse account : accountsResponse.getAccounts()) {
            Account tinkAccount = account.toTinkAccount(config);

            AccountTypes accountType =
                    config.getAccountType(account.getAccountGroup(), account.getUsageType());
            if (accountType == AccountTypes.LOAN) {
                CrossKeyLoanDetails loanDetails;
                try {
                    loanDetails = apiClient.getLoanDetails(account.getAccountId());
                } catch (Exception e) {
                    loanDetails = null;
                    log.warn(
                            String.format("CrossKeyApi/getLoanDetails exception: %s", e.toString()),
                            e);
                }
                if (loanDetails == null) {
                    // something went wrong when we tried to fetch loanDetails
                    financialDataCacher.cacheAccount(tinkAccount);
                    continue;
                }
                Loan loan = loanDetails.toTinkLoan();
                financialDataCacher.cacheAccount(tinkAccount, AccountFeatures.createForLoan(loan));
            } else {
                tinkAccounts.add(tinkAccount);
                financialDataCacher.cacheAccount(tinkAccount);
            }
        }
        return tinkAccounts;
    }

    private void refreshTransactionsFor(Account account) throws Exception {
        List<Transaction> mostRecentTransactions =
                apiClient.getTransactionsFor(account.getBankId(), null, null).toTinkTransactions();

        if (mostRecentTransactions.isEmpty()) {
            return;
        }

        List<Transaction> transactions;

        if (Objects.equals(config.getPaginationType(), PaginationTypes.NONE)
                || isContentWithRefresh(account, mostRecentTransactions)) {
            // If latest transactions are enough or it's not a paged API, just update with latest
            transactions = mostRecentTransactions;
        } else {
            // The transactions we've seen in first request can be used to determine paging
            Range<DateTime> knownTransactionsDateRange =
                    CrossKeyUtils.getDateRange(mostRecentTransactions);

            // Throw away the latest so that we avoid duplicates. So just replace latest with paged
            // transactions
            transactions = getTransactionsDatePaged(account, knownTransactionsDateRange);
        }

        updateTransactionsFor(account, transactions);
    }

    private List<Transaction> getTransactionsDatePaged(
            Account account, Range<DateTime> knownTransactionsDateRange) throws Exception {
        List<Transaction> transactions = Lists.newArrayList();

        Range<DateTime> page =
                CrossKeyUtils.getFirstPage(knownTransactionsDateRange.upperEndpoint());
        DateTime oldestKnownTransactionDate = knownTransactionsDateRange.lowerEndpoint();

        DateTime maxPagingDistance = DateTime.now().minusYears(2);

        do {
            List<Transaction> newTransactions =
                    apiClient
                            .getTransactionsFor(
                                    account.getBankId(), page.lowerEndpoint(), page.upperEndpoint())
                            .toTinkTransactions();

            if (newTransactions.size() == 0
                    && !oldestKnownTransactionDate.isBefore(page.lowerEndpoint())) {
                break;
            } else if (newTransactions.size() > 0) {
                transactions.addAll(newTransactions);
            }

            // Page two months in history at a time
            page = CrossKeyUtils.getNextPage(page.lowerEndpoint());

            // Never go back further than maxPagingDistance, to ensure an end of looping
            if (maxPagingDistance.isAfter(page.lowerEndpoint())) {
                break;
            }
        } while (!isContentWithRefresh(account, transactions));

        return transactions;
    }

    /** Logout */
    public void logout() throws Exception {
        apiClient.logout();
    }

    /** Update context functions */
    private void updateCredentialsDeviceInfo(String deviceId, String deviceToken) {
        credentials.setSensitivePayload(DEVICE_ID_PAYLOAD_KEY, deviceId);
        updateCredentialsDeviceInfo(deviceToken);
    }

    private void updateCredentialsDeviceInfo(String deviceToken) {
        credentials.setSensitivePayload(DEVICE_TOKEN_PAYLOAD_KEY, deviceToken);
    }

    private void updateTransactionsFor(Account account, List<Transaction> transactions) {
        financialDataCacher.updateTransactions(account, transactions);
    }

    private String requestOneTimeCodeFromUser(String oneTimeCodePosition) throws Exception {
        log.info("Requesting one time code from user with position: " + oneTimeCodePosition);

        Field[] fields = CrossKeyUtils.createOneTimeCodeChallengeFields(oneTimeCodePosition);

        Map<String, String> supplementalInformation =
                supplementalInformationController.askSupplementalInformationSync(fields);
        Optional<String> oneTimeCode =
                Optional.ofNullable(
                        supplementalInformation.get(CrossKeyUtils.SUPPLEMENTAL_RESPONSE_NAME));

        if (!oneTimeCode.isPresent()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception(
                    CrossKeyMessage.ERR_PIN_MISSING.getKey());
        }

        log.info("One time code provided by the user");

        return oneTimeCode.get();
    }

    private void logAndReThrow(String message, Exception e) throws Exception {
        log.error(message, e);
        throw e;
    }
}
