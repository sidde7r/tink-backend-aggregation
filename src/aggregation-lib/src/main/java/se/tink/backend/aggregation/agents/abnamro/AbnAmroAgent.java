package se.tink.backend.aggregation.agents.abnamro;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.RefreshableItemExecutor;
import se.tink.backend.aggregation.agents.abnamro.converters.AccountConverter;
import se.tink.backend.aggregation.agents.abnamro.ics.mappers.AccountMapper;
import se.tink.backend.aggregation.agents.abnamro.ics.mappers.TransactionMapper;
import se.tink.backend.aggregation.agents.abnamro.utils.AbnAmroAgentUtils;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.RefreshableItem;
import se.tink.backend.aggregation.rpc.User;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.system.rpc.Transaction;
import se.tink.libraries.abnamro.client.EnrollmentClient;
import se.tink.libraries.abnamro.client.IBSubscriptionClient;
import se.tink.libraries.abnamro.client.exceptions.IcsException;
import se.tink.libraries.abnamro.client.model.PfmContractEntity;
import se.tink.libraries.abnamro.client.model.creditcards.CreditCardAccountContainerEntity;
import se.tink.libraries.abnamro.client.model.creditcards.CreditCardAccountEntity;
import se.tink.libraries.abnamro.client.model.creditcards.TransactionContainerEntity;
import se.tink.libraries.abnamro.client.rpc.enrollment.CollectEnrollmentResponse;
import se.tink.libraries.abnamro.client.rpc.enrollment.InitiateEnrollmentResponse;
import se.tink.libraries.abnamro.config.AbnAmroConfiguration;
import se.tink.libraries.abnamro.utils.AbnAmroUtils;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.phonenumbers.InvalidPhoneNumberException;
import se.tink.libraries.phonenumbers.utils.PhoneNumberUtils;

/**
 * This is the new AbnAmroAgent that will be used for Grip 3.0. It also includes ICS.
 */
public class AbnAmroAgent extends AbstractAgent implements RefreshableItemExecutor {
    private static final Minutes AUTHENTICATION_TIMEOUT = Minutes.minutes(5);

    private final Credentials credentials;
    private final Catalog catalog;
    private final User user;

    private IBSubscriptionClient subscriptionClient;
    private EnrollmentClient enrollmentService;
    private AbnAmroConfiguration abnAmroConfiguration;
    private List<Account> accounts = null;

    public AbnAmroAgent(CredentialsRequest request, AgentContext context) {
        super(request, context);

        this.user = request.getUser();
        this.credentials = request.getCredentials();
        this.catalog = Catalog.getCatalog(user.getLocale());
    }

    @Override
    public void setConfiguration(ServiceConfiguration configuration) {
        abnAmroConfiguration = configuration.getAbnAmro();

        this.subscriptionClient = new IBSubscriptionClient(abnAmroConfiguration, context.getMetricRegistry());

        this.enrollmentService = new EnrollmentClient(
                clientFactory.createBasicClient(context.getLogOutputStream()),
                abnAmroConfiguration.getEnrollmentConfiguration(),
                context.getMetricRegistry());
    }

    @Override
    public boolean login() throws Exception {
        return isAuthenticated() || authenticateWithMobileBanking();
    }

    /**
     * User is authenticated if we have a the customer number stored in the payload.
     */
    private boolean isAuthenticated() {
        return AbnAmroUtils.isValidBcNumberFormat(credentials.getPayload());
    }

    /**
     * Authenticate with the Mobile Banking App. The authentication is performed in four steps
     * 1) Initiate the enrollment for Grip against ABN AMRO.
     * 2) Let the Grip app redirect to Mobile Banking app.
     * 3) User signs/accept the T&C in the Mobile Banking app.
     * 4) Collect/poll the status of the signing.
     */
    private boolean authenticateWithMobileBanking() throws InvalidPhoneNumberException {
        final String phoneNumber = PhoneNumberUtils.normalize(user.getUsername());

        InitiateEnrollmentResponse response = enrollmentService.initiate(phoneNumber);

        openThirdPartyApp(MobileBankingAuthenticationPayload.create(catalog, response.getToken()));

        Optional<String> bcNumber = collect(response.getToken());

        // Reset supplemental and status payload
        credentials.setSupplementalInformation(null);
        credentials.setStatusPayload(null);

        if (bcNumber.isPresent()) {
            credentials.setPayload(bcNumber.get());
            credentials.setStatus(CredentialsStatus.UPDATING);
        } else {
            credentials.setStatus(CredentialsStatus.AUTHENTICATION_ERROR);
        }

        context.updateCredentialsExcludingSensitiveInformation(credentials);

        return bcNumber.isPresent();
    }

    /**
     * Collect/poll the enrollment service until is is completed or timed out.
     */
    private Optional<String> collect(String signingToken) {
        final DateTime start = new DateTime();

        int retry = 0;

        while (start.plus(AUTHENTICATION_TIMEOUT).isAfterNow()) {
            CollectEnrollmentResponse response = enrollmentService.collect(signingToken);

            if (response.isCompleted()) {
                log.info("User enrolled in Mobile Banking.");
                return Optional.ofNullable(response.getBcNumber());
            }

            log.debug(String.format("User enrollment in progress (Retry = '%d').", retry));

            // Check every 10s if the credential is deleted during the long polling
            if (retry % 5 == 0 && context.isCredentialDeleted(credentials.getId())) {
                log.warn("Credential deleted during long poll. Aborting.");
                return Optional.empty();
            }

            retry++;
            Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
        }

        log.warn("User enrollment timed out.");
        return Optional.empty();
    }

    @Override
    public void refresh(RefreshableItem item) {
        switch (item) {
        case CHECKING_ACCOUNTS:
        case SAVING_ACCOUNTS:
        case CREDITCARD_ACCOUNTS:
            updateAccountPerType(item);
            break;

        case CHECKING_TRANSACTIONS:
        case SAVING_TRANSACTIONS:
        case CREDITCARD_TRANSACTIONS:
            refreshTransactionsPerType(item);
            break;
        }
    }

    private void updateAccountPerType(RefreshableItem type) {
        getAccounts().stream()
                .filter(account -> type.isAccountType(account.getType()))
                .forEach(context::updateAccount);
    }

    private List<Account> getAccounts() {
        if (accounts != null) {
            return accounts;
        }

        final String bcNumber = credentials.getPayload();

        Preconditions.checkState(AbnAmroUtils.isValidBcNumberFormat(bcNumber));
        try {
            List<PfmContractEntity> contracts = subscriptionClient.getContracts(bcNumber);
            accounts = new AccountConverter(AbnAmroUtils.shouldUseNewIcsAccountFormat(user.getFlags()))
                    .convert(contracts);
            return accounts;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void refreshTransactionsPerType(RefreshableItem type) {
        final String bcNumber = credentials.getPayload();

        getAccounts().stream()
                .filter(account -> type.isAccountType(account.getType()))
                .forEach(account ->
        {
            // Update the account. This will call system which will subscribe the account towards ABN AMRO if it is
            // a new account.
            account = context.updateAccount(account);

            if (account.getType() == AccountTypes.CREDIT_CARD) {
                // Refresh credit cards and credit cards transactions. They are both fetched from ABN AMRO.
                try {
                    refreshCreditCard(bcNumber, account);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            } else if (AbnAmroAgentUtils.isSubscribed(account)) {
                // This is a new account that was subscribed towards ABN AMRO. Tell aggregation that we are waiting
                // on getting transactions ingested in the connector.
                if (!context.isWaitingOnConnectorTransactions()) {
                    context.setWaitingOnConnectorTransactions(true);
                }
            }
        });
    }

    /**
     * First need to update the account & subscribe the account towards ABN AMRO. If it is a credit card then
     * the agent will fetch/pull transactions. If it is a non credit card then the transactions will be pushed
     * to the ABN AMRO connector.
     */
    private void refreshCreditCard(String bcNumber, Account account) throws IcsException {
        try {
            updateCreditCardAccountAndTransactions(bcNumber, account.getBankId());
        } catch (IcsException e) {
            // The connection to ICS is returning a lot of errors in the test environments.
            if (abnAmroConfiguration.shouldIgnoreCreditCardErrors()) {
                log.warn("Ignoring error from ICS.", e);
            } else {
                throw e;
            }
        }
    }

    private void updateCreditCardAccountAndTransactions(String bcNumber, String bankId) throws IcsException {
        List<CreditCardAccountContainerEntity> entities = subscriptionClient
                .getCreditCardAccountAndTransactions(bcNumber, Long.valueOf(bankId));

        for (CreditCardAccountContainerEntity entity : entities) {
            CreditCardAccountEntity creditCardAccount = entity.getCreditCardAccount();

            Account account = AccountMapper.toAccount(creditCardAccount);

            List<Transaction> transactions = creditCardAccount.getTransactions().stream()
                    .filter(TransactionContainerEntity::isInEUR)
                    .map(TransactionMapper::toTransaction)
                    .collect(Collectors.toList());

            context.updateTransactions(account, transactions);
        }
    }

    @Override
    public void logout() throws Exception {
        // NOP
    }
}
