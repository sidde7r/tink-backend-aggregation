package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher;

import com.google.common.collect.Lists;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConfiguration;
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
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@Slf4j
public class SdcCreditCardFetcher extends SdcAgreementFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionDatePaginator<CreditCardAccount> {

    private static final int ONE_WEEK_AGO_IN_DAYS = -7;

    private final SdcTransactionParser transactionParser;
    protected final SdcConfiguration agentConfiguration;
    protected final Map<String, SdcAccountKey> creditCardAccounts = new HashMap<>();

    public SdcCreditCardFetcher(
            SdcApiClient bankClient,
            SdcSessionStorage sessionStorage,
            SdcTransactionParser transactionParser,
            SdcConfiguration agentConfiguration) {
        super(bankClient, sessionStorage);
        this.transactionParser = transactionParser;
        this.agentConfiguration = agentConfiguration;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        List<CreditCardAccount> creditCards = Lists.newArrayList();

        SessionStorageAgreements agreements = getAgreements();
        for (SessionStorageAgreement agreement : agreements) {
            Optional<SdcServiceConfigurationEntity> serviceConfigurationEntity =
                    selectAgreement(agreement, agreements);

            serviceConfigurationEntity.ifPresent(
                    configurationEntity -> {

                        // different provider service config use different endpoints:
                        // SparbankenSyd accounts (of type KKPD) handled by SdcSeCreditcardFetcher
                        // Eika(NO) provider list (config isCreditcard)
                        // Storebrand(NO) and DK credit card list (config isBlockcard)
                        if (configurationEntity.isCreditCard()) {
                            fetchCreditCardProviderAccountList(creditCards, agreement);
                            if (creditCards.size() > 0) {
                                log.info(
                                        "Fetch credit cards using: fetchCreditCardProviderAccountList: "
                                                + creditCards.size());
                            }
                        } else if (configurationEntity.isBlockCard()) {
                            fetchCreditCardList(creditCards, agreement);
                            if (creditCards.size() > 0) {
                                log.info(
                                        "Fetch credit cards using: fetchCreditCardList: "
                                                + creditCards.size());
                            }
                        }
                    });
        }

        // store updated agreements
        setAgreements(agreements);

        return creditCards;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            CreditCardAccount account, Date fromDate, Date toDate) {
        SessionStorageAgreements agreements = getAgreements();
        SessionStorageAgreement agreement =
                agreements.findAgreementForAccountBankId(account.getApiIdentifier());

        Optional<SdcServiceConfigurationEntity> serviceConfigurationEntity =
                selectAgreement(agreement, agreements);

        if (!serviceConfigurationEntity.isPresent()) {
            return PaginatorResponseImpl.createEmpty(false);
        }

        Collection<? extends Transaction> transactions;

        SdcAccountKey creditCardAccountId = this.creditCardAccounts.get(account.getApiIdentifier());
        SearchTransactionsRequest searchTransactionsRequest =
                new SearchTransactionsRequest()
                        .setAccountId(creditCardAccountId.getAccountId())
                        .setAgreementId(creditCardAccountId.getAgreementId())
                        .setIncludeReservations(shouldIncludeReservations(toDate))
                        .setTransactionsFrom(formatDate(fromDate))
                        .setTransactionsTo(formatDate(toDate));

        SearchTransactionsResponse creditCardTransactions =
                this.bankClient.searchCreditCardTransactions(searchTransactionsRequest);

        transactions =
                creditCardTransactions.getTinkCreditCardTransactions(
                        account, this.transactionParser);

        // keep an eye on number of transactions returned for credit cards for Sdc clients
        if (transactions != null && !transactions.isEmpty()) {
            log.debug("Num credit cards transaction: {}", transactions.size());
        }

        return PaginatorResponseImpl.create(transactions);
    }

    // isCreditcard
    private void fetchCreditCardProviderAccountList(
            List<CreditCardAccount> creditCards, SessionStorageAgreement agreement) {
        FilterAccountsResponse creditCardProviderAccounts =
                this.bankClient.listCreditCardProviderAccounts();

        for (SdcAccount creditCardProviderAccount : creditCardProviderAccounts) {
            if (creditCardProviderAccount.isCreditCardAccount()) {
                CreditCardAccount creditCardAccount =
                        creditCardProviderAccount.toTinkCreditCardAccount();

                // return value
                creditCards.add(creditCardAccount);

                String creditCardAccountId = creditCardAccount.getApiIdentifier();
                // keep BankIdentifier to be able to fetch transactions
                this.creditCardAccounts.put(
                        creditCardAccountId, creditCardProviderAccount.getEntityKey());
                // store BankIdentifier to be able to find agreement for transactions
                agreement.addAccountBankId(creditCardAccountId);
            }
        }
    }

    // isBlockcard
    private void fetchCreditCardList(
            List<CreditCardAccount> creditCards, SessionStorageAgreement agreement) {
        FilterAccountsResponse accounts = retrieveAccounts();

        // if no credit card account do not try to fetch any
        Optional<SdcAccount> creditCardAccount =
                accounts.stream().filter(SdcAccount::isCreditCardAccount).findFirst();
        if (!creditCardAccount.isPresent()) {
            return;
        }

        ListCreditCardsResponse creditAndDebetCards = this.bankClient.listCreditCards();

        for (SdcCreditCardEntity creditCardEntity : creditAndDebetCards.getCreditCards()) {
            SdcAccount sdcAccount = accounts.findAccount(creditCardEntity);

            CreditCardAccount creditCard = creditCardEntity.toTinkCard(sdcAccount);

            String creditCardBankIdentifier = creditCard.getApiIdentifier();

            creditCards.add(creditCard);
            this.creditCardAccounts.put(
                    creditCardBankIdentifier, creditCardEntity.getAttachedAccount().getEntityKey());
            // add credit card bankId to agreement for fetching transactions later
            agreement.addAccountBankId(creditCardBankIdentifier);
        }
    }

    protected FilterAccountsResponse retrieveAccounts() {
        FilterAccountsRequest request =
                new FilterAccountsRequest()
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
}
