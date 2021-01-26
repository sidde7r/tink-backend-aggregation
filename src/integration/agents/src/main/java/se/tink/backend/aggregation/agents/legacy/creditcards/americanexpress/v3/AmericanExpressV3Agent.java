package se.tink.backend.aggregation.agents.creditcards.americanexpress.v3;

import com.google.api.client.util.Lists;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.DeprecatedRefreshExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model.ActivityEntity;
import se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model.BillingInfoDetailsEntity;
import se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model.CardDetailsEntity;
import se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model.CardEntity;
import se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model.LoginResponse;
import se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model.StatusEntity;
import se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model.TimelineEntity;
import se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model.TimelineItemEntity;
import se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model.TimelineItemGroupEntity;
import se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model.TransactionDetailsEntity;
import se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model.TransactionEntity;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.constants.CommonHeaders;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.credentials.service.CredentialsRequest;

@AgentCapabilities(generateFromImplementedExecutors = true)
public final class AmericanExpressV3Agent extends AbstractAgent
        implements DeprecatedRefreshExecutor {

    private final AmericanExpressV3ApiClient apiClient;

    private final Credentials credentials;
    private final Set<String> mainCardNumbers = Sets.newHashSet();
    private final Map<String, SubCard> subCardsByCardNumber = Maps.newHashMap();
    private boolean hasRefreshed = false;

    public AmericanExpressV3Agent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context);

        this.credentials = request.getCredentials();
        this.apiClient =
                new AmericanExpressV3ApiClient(
                        clientFactory.createCustomClient(context.getLogOutputStream()),
                        request.getProvider().getMarket(),
                        CommonHeaders.DEFAULT_USER_AGENT,
                        credentials);

        // client.addFilter(new LoggingFilter(System.out));
    }

    public AmericanExpressV3Agent(
            CredentialsRequest request,
            AgentContext context,
            SignatureKeyPair signatureKeyPair,
            AmericanExpressV3ApiClient apiClient) {
        super(request, context);

        this.credentials = request.getCredentials();
        this.apiClient = apiClient;
        // client.addFilter(new LoggingFilter(System.out));
    }

    @Override
    public void refresh() throws Exception {
        // The refresh command will call refresh multiple times.
        // This check ensures the refresh only runs once.
        if (hasRefreshed) {
            return;
        }
        hasRefreshed = true;

        // Create the accounts and transaction mappings.

        for (CardDetailsEntity cardEntity : apiClient.getCardList()) {
            mainCardNumbers.add(cardEntity.getCardNumberDisplay());
        }

        for (CardDetailsEntity cardEntity : apiClient.getCardList()) {
            Account account = cardEntity.toAccount(apiClient.getMarketParameters());
            List<Transaction> transactions = Lists.newArrayList();

            if (!getPendingTransactions(cardEntity, transactions)) {
                continue;
            }

            getTransactions(cardEntity, transactions, account);

            financialDataCacher.updateTransactions(account, transactions);
        }

        for (SubCard subCard : subCardsByCardNumber.values()) {
            financialDataCacher.updateTransactions(subCard.getAccount(), subCard.getTransactions());
        }
    }

    private boolean isResponseError(StatusEntity response) {
        if (response.getStatus() == 0) {
            return false;
        }

        log.warn(
                String.format(
                        "#provider-error - Response error status: %d, type: %s, message: %s",
                        response.getStatus(), response.getMessageType(), response.getMessage()));
        return true;
    }

    private boolean getPendingTransactions(
            CardDetailsEntity cardEntity, List<Transaction> transactions) throws Exception {

        TimelineEntity timeline = apiClient.getTimeLine(cardEntity);
        if (isResponseError(timeline)) {
            // Observed errors:
            // - "Kortet har annullerats"
            // - "Det gick inte att läsa in innehållet. Försök igen senare."
            // - "Tyvärr kan vi inte hämta dina transaktioner just nu. Försök igen om några
            // minuter."
            return false;
        }
        createSubCards(timeline.getCardList());

        Map<String, String> cardNumberBySuppIndex =
                apiClient.createCardNumberBySuppIndexMap(timeline.getCardList());

        Map<String, TransactionEntity> timelineTransactionsMap = timeline.getTransactionMap();

        if (timeline.getTimelineItems() != null) {
            for (TimelineItemGroupEntity timelineItemGroup : timeline.getTimelineItems()) {
                if (timelineItemGroup.getSubItems() != null) {
                    for (TimelineItemEntity timelineItem : timelineItemGroup.getSubItems()) {
                        if (!Objects.equal(timelineItem.getType(), "pendingTransaction")) {
                            continue;
                        }

                        TransactionEntity transactionEntity =
                                timelineTransactionsMap.get(timelineItem.getId());
                        String transactionCardNumber =
                                cardNumberBySuppIndex.get(transactionEntity.getSuppIndex());

                        Transaction transaction = transactionEntity.toTransaction();
                        transaction.setPending(true);

                        // Only add transaction if its related card number equals this card's number
                        if (Objects.equal(
                                transactionCardNumber, cardEntity.getCardNumberDisplay())) {
                            transactions.add(transaction);

                            // Otherwise it should be related to one of the sub cards
                        } else if (subCardsByCardNumber.containsKey(transactionCardNumber)) {
                            subCardsByCardNumber.get(transactionCardNumber).add(transaction);
                        }
                    }
                }
            }
        }
        return true;
    }

    private void getTransactions(
            CardDetailsEntity cardEntity, List<Transaction> transactions, Account account)
            throws Exception {

        // Loop through the available statements.

        int availableBillingPeriods = 0;

        for (int billingIndex = 0; billingIndex <= availableBillingPeriods; billingIndex++) {

            TransactionDetailsEntity transactionDetails =
                    apiClient.getTransactionDetails(billingIndex, cardEntity);
            if (isResponseError(transactionDetails)) {
                // Observed errors:
                // - "Tyvärr kan vi inte hämta dina transaktioner just nu. Försök igen om några
                // minuter."
                continue;
            }

            createSubCards(transactionDetails.getCardList());
            // Detect the maximum available billing periods for this card.

            if (billingIndex == 0) {
                for (BillingInfoDetailsEntity billingInfoDetailsEntity :
                        transactionDetails.getBillingInfo().getBillingInfoDetails()) {
                    availableBillingPeriods =
                            Math.max(
                                    availableBillingPeriods,
                                    Integer.parseInt(billingInfoDetailsEntity.getBillingIndex()));
                }
            }

            Map<String, String> cardNumberBySuppIndex =
                    apiClient.createCardNumberBySuppIndexMap(transactionDetails.getCardList());

            // Add the transactions.

            for (ActivityEntity activityList : transactionDetails.getActivityList()) {
                if (activityList.getTransactionList() == null) // No recent transactions.
                {
                    continue;
                }

                for (TransactionEntity transactionEntity : activityList.getTransactionList()) {
                    String transactionCardNumber =
                            cardNumberBySuppIndex.get(transactionEntity.getSuppIndex());

                    Transaction transaction = transactionEntity.toTransaction();
                    // Only add transaction if its related card number equals this card's number
                    if (Objects.equal(transactionCardNumber, cardEntity.getCardNumberDisplay())) {
                        transactions.add(transaction);

                        // Otherwise it should be related to one of the sub cards
                    } else if (subCardsByCardNumber.containsKey(transactionCardNumber)) {
                        subCardsByCardNumber.get(transactionCardNumber).add(transaction);
                    }
                }
            }

            if (isContentWithRefresh(account, transactions)) {
                break;
            }

            statusUpdater.updateStatus(CredentialsStatus.UPDATING, account, transactions);
        }
    }

    private void createSubCards(List<CardEntity> cardEntities) {
        for (CardEntity card : cardEntities) {
            String cardNumber = apiClient.productNameToCardNumber(card.getCardProductName());

            // NOTE: In the nxgen, we don't track "sub-account" here, because these accounts are
            // probably "INACTIVE",
            // but for now, make it compliant with Account format.
            if (!mainCardNumbers.contains(cardNumber)
                    && !subCardsByCardNumber.containsKey(cardNumber)) {
                Account subAccount = new Account();
                String productName = card.getCardProductName();
                subAccount.setAccountNumber(
                        "XXX-" + productName.substring(productName.length() - 5));
                subAccount.setName(card.getCardProductName());
                subAccount.setBankId(cardNumber.replaceAll("[^\\dA-Za-z]", ""));
                subAccount.setType(AccountTypes.CREDIT_CARD);
                subAccount.setBalance(0d);

                subCardsByCardNumber.put(cardNumber, new SubCard(subAccount));
            }
        }
    }

    private static class SubCard {
        private final Account account;
        private final List<Transaction> transactions;

        private SubCard(Account account) {
            this.account = account;
            this.transactions = Lists.newArrayList();
        }

        private void add(Transaction transaction) {
            transactions.add(transaction);
        }

        private Account getAccount() {
            return account;
        }

        private List<Transaction> getTransactions() {
            return transactions;
        }
    }

    @Override
    public boolean login() throws AuthenticationException, AuthorizationException {
        LoginResponse loginResponse = apiClient.login();

        if (loginResponse.getLogonData().getStatus() != -1) {
            return true;
        }

        /*
        Amex will in some cases, incorrectly, respond with the wrong password error code (even though the usr/pw was
        correct).
        This causes users to have to re-enter their username/password in the app.

        String statusCode = loginResponse.getLogonData().getStatusCode();
        if (statusCode != null && (statusCode.equals("incorrect") || statusCode.equals("secondAttempt"))) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        */

        String statusCode = loginResponse.getLogonData().getStatusCode();
        String message = loginResponse.getLogonData().getMessage();

        if (statusCode != null) {
            if (Objects.equal("revoked", statusCode.toLowerCase())) {
                throw AuthorizationError.ACCOUNT_BLOCKED.exception();
            }

            if (message.toLowerCase().contains("inaktiv längre än 10 minuter")) {
                throw SessionError.SESSION_EXPIRED.exception();
            }

            if (message.toLowerCase()
                    .contains("fel inträffade tyvärr vid laddning av innehållet")) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception(
                        "Message: " + message + ", status: " + statusCode);
            }
        }

        throw new IllegalStateException(
                String.format(
                        "#login-refactoring - AMEXv3 - Login failed with message: (%s) %s",
                        statusCode, message));
    }

    @Override
    public void logout() throws Exception {
        // TODO Implement.
    }
}
