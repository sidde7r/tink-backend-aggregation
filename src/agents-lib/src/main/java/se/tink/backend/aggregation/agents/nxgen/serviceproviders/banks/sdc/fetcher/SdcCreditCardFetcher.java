package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher;

import com.google.common.collect.Lists;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SdcServiceConfigurationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SessionStorageAgreement;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SessionStorageAgreements;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities.SdcAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities.SdcAccountKey;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities.SdcCreditCardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.FilterAccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.FilterAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.ListCreditCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.SearchTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.SearchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.parser.SdcTransactionParser;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SdcCreditCardFetcher extends SdcAgreementFetcher implements AccountFetcher<CreditCardAccount>,
        TransactionDatePaginator<CreditCardAccount> {
    private static final Logger log = LoggerFactory.getLogger(SdcCreditCardFetcher.class);

    private static final int ONE_WEEK_AGO_IN_DAYS = -7;

    private final SdcTransactionParser transactionParser;
    private final SdcConfiguration agentConfiguration;
    private final Map<String, SdcAccountKey> creditCardAccounts = new HashMap<>();

    public SdcCreditCardFetcher(SdcApiClient bankClient, SdcSessionStorage sessionStorage,
            SdcTransactionParser transactionParser, SdcConfiguration agentConfiguration) {
        super(bankClient, sessionStorage);
        this.transactionParser = transactionParser;
        this.agentConfiguration = agentConfiguration;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        List<CreditCardAccount> creditCards = Lists.newArrayList();

        SessionStorageAgreements agreements = getAgreements();
        for (SessionStorageAgreement agreement : agreements) {
            Optional<SdcServiceConfigurationEntity> serviceConfigurationEntity = selectAgreement(agreement, agreements);

            serviceConfigurationEntity.ifPresent(configurationEntity -> {

                // different provider service config use different endpoints
                if (configurationEntity.isCreditCard()) {
                    log.info("Fetch credit cards using: fetchCreditCardProviderAccountList");
                    fetchCreditCardProviderAccountList(creditCards, agreement);
                }

                if (SdcConstants.BANK_CODE_SPARBANKEN_SYD.equals(agentConfiguration.getBankCode())) {
                    getCreditCardAccountsForSparbankenSyd(creditCards, agreement);
                } else if (configurationEntity.isBlockCard()) {
                    try {
                        log.info("Fetch credit cards using: fetchCreditCardList");
                        fetchCreditCardList(creditCards, agreement);
                    } catch (Exception e) {
                        log.info("Failed to fetch credit card for user", e);
                    }
                }
            });
        }

        // store updated agreements
        setAgreements(agreements);

        return creditCards;
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, Date fromDate, Date toDate) {
        SessionStorageAgreements agreements = getAgreements();
        SessionStorageAgreement agreement = agreements.findAgreementForAccountBankId(account.getBankIdentifier());

        Optional<SdcServiceConfigurationEntity> serviceConfigurationEntity = selectAgreement(agreement, agreements);

        if (!serviceConfigurationEntity.isPresent()) {
            return PaginatorResponseImpl.createEmpty(false);
        }

        Collection<? extends Transaction> transactions = Collections.emptyList();

        SdcAccountKey creditCardAccountId = this.creditCardAccounts.get(account.getBankIdentifier());
        SearchTransactionsRequest searchTransactionsRequest = new SearchTransactionsRequest()
                .setAccountId(creditCardAccountId.getAccountId())
                .setAgreementId(creditCardAccountId.getAgreementId())
                .setIncludeReservations(shouldIncludeReservations(toDate))
                .setTransactionsFrom(formatDate(fromDate))
                .setTransactionsTo(formatDate(toDate));

        if (SdcConstants.BANK_CODE_SPARBANKEN_SYD.equals(agentConfiguration.getBankCode())) {
            transactions = getSparbankenSydTransactions(account,
                    searchTransactionsRequest);

        } else {
            SearchTransactionsResponse creditCardTransactions = this.bankClient
                    .searchCreditCardTransactions(searchTransactionsRequest);

            transactions = creditCardTransactions.getTinkCreditCardTransactions(account,
                    this.transactionParser);
        }

        return PaginatorResponseImpl.create(transactions);
    }

    private void fetchCreditCardProviderAccountList(List<CreditCardAccount> creditCards,
            SessionStorageAgreement agreement) {
        FilterAccountsResponse creditCardProviderAccounts = this.bankClient.listCreditCardProviderAccounts();

        for (SdcAccount creditCardProviderAccount : creditCardProviderAccounts) {
            if (creditCardProviderAccount.isCreditCardAccount()) {
                CreditCardAccount creditCardAccount = creditCardProviderAccount.toTinkCreditCardAccount(
                        this.agentConfiguration);

                // return value
                creditCards.add(creditCardAccount);

                String creditCardAccountId = creditCardAccount.getBankIdentifier();
                // keep BankIdentifier to be able to fetch transactions
                this.creditCardAccounts.put(creditCardAccountId, creditCardProviderAccount.getEntityKey());
                // store BankIdentifier to be able to find agreement for transactions
                agreement.addAccountBankId(creditCardAccountId);
            }
        }
    }

    private void fetchCreditCardList(List<CreditCardAccount> creditCards, SessionStorageAgreement agreement) {
        FilterAccountsResponse accounts = retrieveAccounts();

        // if no credit card account do not try to fetch any
        Optional<SdcAccount> creditCardAccount = accounts.stream()
                .filter(SdcAccount::isCreditCardAccount)
                .findFirst();
        if (!creditCardAccount.isPresent()) {
            return;
        }

        ListCreditCardsResponse creditAndDebetCards = this.bankClient.listCreditCards();

        for (SdcCreditCardEntity creditCardEntity : creditAndDebetCards.getCreditCards()) {
            SdcAccount sdcAccount = accounts.findAccount(creditCardEntity);

            CreditCardAccount creditCard = creditCardEntity.toTinkCard(sdcAccount);

            String creditCardBankIdentifier = creditCard.getBankIdentifier();

            creditCards.add(creditCard);
            this.creditCardAccounts.put(creditCardBankIdentifier, creditCardEntity.getAttachedAccount().getEntityKey());
            // add credit card bankId to agreement for fetching transactions later
            agreement.addAccountBankId(creditCardBankIdentifier);
        }
    }

    private FilterAccountsResponse retrieveAccounts() {
        FilterAccountsRequest request = new FilterAccountsRequest()
                .setOnlyFavorites(false)
                .setIncludeCreditAccounts(true)
                .setIncludeDebitAccounts(true)
                .setIncludeLoans(true)
                .setOnlyQueryable(true);

        return this.bankClient.filterAccounts(request);
    }

    // only fetch reservations when asking for most current transactions
    private boolean shouldIncludeReservations(Date toDate) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, ONE_WEEK_AGO_IN_DAYS);
        return toDate.after(c.getTime());
    }

    private String formatDate(Date aDate) {
        LocalDate date = new java.sql.Date(aDate.getTime()).toLocalDate();
        return date.format(DateTimeFormatter.ISO_DATE);
    }


    ////////////////// code to see what works for sparbanken syd when fetching credit cards. Earlier the used to work
    ////////////////// using "BlockCard"

    // sweden used to use BlockCard style, but it doesn't work This is a test and code cleanup will
    // be done when the correct way defined
    private void getCreditCardAccountsForSparbankenSyd(List<CreditCardAccount> creditCards,
            SessionStorageAgreement agreement) {
        List<CreditCardAccount> creditCardAccounts = new ArrayList<>();

        try {
            log.info("Fetch credit cards using: fetchCreditCardAccounts");
            fetchCreditCardAccounts(creditCardAccounts, agreement);
            if (creditCardAccounts.size() > 0) {
                log.info("Sparbanken syd - Fetch credit cards using: fetchCreditCardAccounts: "
                        + creditCardAccounts.size());
            }
        } catch (Exception e) {
            log.info("Failed to fetch credit card for user", e);
        }

        creditCards.addAll(creditCardAccounts);
    }

    // Sparbanken syd uses standard account as credit card account
    private void fetchCreditCardAccounts(List<CreditCardAccount> creditCards, SessionStorageAgreement agreement) {
        FilterAccountsResponse accounts = retrieveAccounts();

        // Convert accounts of type credit card to credit card
        accounts.stream()
                .filter(SdcAccount::isCreditCardAccount)
                .forEach(account -> {
                    CreditCardAccount creditCardAccount = account.toTinkCreditCardAccount(this.agentConfiguration);

                    // return value
                    creditCards.add(creditCardAccount);

                    String creditCardAccountId = creditCardAccount.getBankIdentifier();
                    // keep BankIdentifier to be able to fetch transactions
                    this.creditCardAccounts.put(creditCardAccountId, account.getEntityKey());
                    // add credit card bankId to agreement for fetching transactions later
                    agreement.addAccountBankId(creditCardAccountId);
                });
    }

    // credit cards are not working for sparbanken syd, Try fetch transactions in different ways and see what works
    private Collection<? extends Transaction> getSparbankenSydTransactions(CreditCardAccount account,
            SearchTransactionsRequest searchTransactionsRequest) {
        Collection<? extends Transaction> transactions = Collections.emptyList();

        try {
            SearchTransactionsResponse creditCardTransactions = this.bankClient
                    .searchCreditCardTransactions(searchTransactionsRequest);

            if (creditCardTransactions != null &&
                    creditCardTransactions.getTransactions() != null &&
                    creditCardTransactions.getTransactions().size() > 0) {
                log.info("SparbankenSyd - credit card transactions " + SerializationUtils.serializeToString(creditCardTransactions));
                transactions = creditCardTransactions.getTinkCreditCardTransactions(account, this.transactionParser);
            }
        } catch (Exception e) {
            log.info("SparbankenSyd - Failed to fetch credit card transactions", e);
        }

        try {
            SearchTransactionsResponse creditCardTransactions = this.bankClient
                    .searchTransactions(searchTransactionsRequest);

            if (creditCardTransactions != null &&
                    creditCardTransactions.getTransactions() != null &&
                    creditCardTransactions.getTransactions().size() > 0) {
                log.info("SparbankenSyd - account transactions for credit card " + SerializationUtils.serializeToString(creditCardTransactions));
                if (transactions.size() == 0) {
                    transactions = creditCardTransactions.getTinkCreditCardTransactions(account, this.transactionParser);
                }
            }
        } catch (Exception e) {
            log.info("SparbankenSyd - Failed to fetch account transactions for credit card", e);
        }

        return transactions;
    }
}
