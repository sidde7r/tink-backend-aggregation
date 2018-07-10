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
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class SdcCreditCardFetcher extends SdcAgreementFetcher implements AccountFetcher<CreditCardAccount>,
        TransactionDatePaginator<CreditCardAccount> {

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
            SdcServiceConfigurationEntity serviceConfigurationEntity = selectAgreement(agreement, agreements);

            // different provider service config use different endpoints
            if (serviceConfigurationEntity.isCreditCard()) {
                fetchCreditCardProviderAccountList(creditCards, agreement);
            }

            if (serviceConfigurationEntity.isBlockCard()) {
                fetchCreditCardList(creditCards, agreement);
            }

        }

        // store updated agreements
        setAgreements(agreements);

        return creditCards;
    }

    @Override
    public Collection<? extends Transaction> getTransactionsFor(CreditCardAccount account, Date fromDate, Date toDate) {
        SessionStorageAgreements agreements = getAgreements();
        SessionStorageAgreement agreement = agreements.findAgreementForAccountBankId(account.getBankIdentifier());

        selectAgreement(agreement, agreements);

        SdcAccountKey creditCardAccountId = this.creditCardAccounts.get(account.getBankIdentifier());
        SearchTransactionsRequest searchTransactionsRequest = new SearchTransactionsRequest()
                .setAccountId(creditCardAccountId.getAccountId())
                .setAgreementId(creditCardAccountId.getAgreementId())
                .setIncludeReservations(shouldIncludeReservations(toDate))
                .setTransactionsFrom(formatDate(fromDate))
                .setTransactionsTo(formatDate(toDate));

        SearchTransactionsResponse creditCardTransactions = this.bankClient
                .searchCreditCardTransactions(searchTransactionsRequest);

        return creditCardTransactions.getTinkCreditCardTransactions(account, this.transactionParser);
    }

    private void fetchCreditCardProviderAccountList(List<CreditCardAccount> creditCards, SessionStorageAgreement agreement) {
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
        ListCreditCardsResponse creditAndDebetCards = this.bankClient.listCreditCards();
        FilterAccountsResponse accounts = retrieveAccounts();

        for (SdcCreditCardEntity creditCardEntity : creditAndDebetCards.getCreditCards()) {
            SdcAccount sdcAccount = accounts.findAccount(creditCardEntity);


            // TODO: this is temporary just to be able to log credit card transaction fetching for "no-storebrand" = "9680"
            CreditCardAccount creditCard = creditCardEntity.toTinkCard(sdcAccount, this.agentConfiguration.getBankCode());

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
}
