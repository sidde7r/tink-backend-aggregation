
package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher;

import com.google.common.collect.Lists;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities.OpBankCardEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities.OpBankCreditCardTransaction;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities.OpBankCreditEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.rpc.FetchCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.rpc.FetchCreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.rpc.FetchCreditsResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class OpBankCreditCardFetcher implements AccountFetcher<CreditCardAccount>,
        TransactionDatePaginator<CreditCardAccount> {
    private static final AggregationLogger LOGGER = new AggregationLogger(OpBankCreditCardFetcher.class);

    private final OpBankApiClient client;
    private final Credentials credentials;
    private List<OpBankCardEntity> creditCards;
    private final Map<String, List<OpBankCreditCardTransaction>> creditCardTransactions;

    public OpBankCreditCardFetcher(OpBankApiClient client, Credentials credentials) {
        this.client = client;
        this.credentials = credentials;
        this.creditCardTransactions = new HashMap<>();
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        List<CreditCardAccount> creditCardAccounts = Lists.newArrayList();

        FetchCardsResponse fetchCardsResponse = client.fetchCards();
        if(fetchCardsResponse.getCardInfoList() != null){
            creditCards = fetchCardsResponse.getCreditCardInfoList();
            creditCardAccounts.addAll(fetchCardsResponse.getTinkCreditCards());
            // continuing credit is not really a credit card, but it is handled here
            creditCardAccounts.addAll(fetchContinuingCreditAccounts());
        }

        return creditCardAccounts;
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, Date fromDate, Date toDate) {
        List<CreditCardTransaction> creditTransactions = Lists.newArrayList();

        Optional<OpBankCardEntity> currentCard = getCurrentCard(account);
        if (!currentCard.isPresent()) {
            return PaginatorResponseImpl.createEmpty(false);
        }

        OpBankCardEntity card = currentCard.get();
        if (isFirstFetch(toDate)) {
            fromDate = calculateFromDate();
            toDate = null;
        }

        // this is to fetch all transactions for the current date interval, logic: fetch until we have seen all transactions before
        List<CreditCardTransaction> moreTransactions = null;
        do {
            moreTransactions = fetchMoreTransactions(account, card, fromDate, toDate);
            creditTransactions.addAll(moreTransactions);
        } while (moreTransactions.size() > 0);

        return PaginatorResponseImpl.create(creditTransactions);
    }

    private List<CreditCardTransaction> fetchMoreTransactions(CreditCardAccount account, OpBankCardEntity card, Date fromDate, Date toDate) {

        // handle the case when we have multiple credit cards for a credential
        // reset processed transactions to not hog memory
        if (!creditCardTransactions.containsKey(card.getCardNumber())) {
            creditCardTransactions.clear();
            creditCardTransactions.put(card.getCardNumber(), Lists.newArrayList());
        }

        List<OpBankCreditCardTransaction> processedTransactions = creditCardTransactions.get(card.getCardNumber());

        FetchCreditCardTransactionsResponse transactionsResponse = client
                .fetchCreditCardTransactions(card, fromDate, toDate, processedTransactions.isEmpty());

        List<OpBankCreditCardTransaction> unprocessedTransactions = transactionsResponse.filterTransactions(processedTransactions);

        // log fetch and results of fetch
        LOGGER.infoExtraLong(String.format("Creditcard transactions, card %s, date: %s, fetched %d, processed %d, after filter %d left.",
                card.getCardNumberMasked(),
                formatDate(fromDate),
                transactionsResponse.size(),
                processedTransactions.size(),
                unprocessedTransactions.size())
                , (OpBankConstants.Fetcher.CREDITCARD_LOGGING));

        if (unprocessedTransactions.isEmpty()) {
            return Collections.emptyList();
        }

        processedTransactions.addAll(unprocessedTransactions);

        return unprocessedTransactions.stream()
                .map(opTrans -> opTrans.toTinkCreditCardTransaction(account))
                .collect(Collectors.toList());
    }

    private String formatDate(Date date) {
        return ThreadSafeDateFormat.FORMATTER_INTEGER_DATE.format(date);
    }

    private boolean isFirstFetch(Date toDate) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, OpBankConstants.ONE_WEEK_AGO_IN_DAYS);
        return toDate.after(c.getTime());
    }

    private Date calculateFromDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, 1);
        calendar.add(Calendar.DATE, -90);
        calendar.set(Calendar.DATE, 1);
        return calendar.getTime();
    }

    // we need many fields from the OP Bank card object to fetch transactions
    // it is stored when fetching cards to be used when fetching tx
    private Optional<OpBankCardEntity> getCurrentCard(Account account) {
        if (creditCards == null) {
            return Optional.empty();
        }

        return creditCards.stream()
                .filter(c -> account.getBankIdentifier().equalsIgnoreCase(c.getCardNumber()))
                .findFirst();
    }

    private Collection<CreditCardAccount> fetchContinuingCreditAccounts() {
        List<CreditCardAccount> creditAccounts = Lists.newArrayList();

        try {
            FetchCreditsResponse fetchCreditsResponse = client.fetchCredits();

            for (OpBankCreditEntity credit : fetchCreditsResponse.getCredits()) {

                if (OpBankConstants.Fetcher.CONTINUING_CREDIT
                        .equalsIgnoreCase(credit.getCreditType())) {
                    creditAccounts.add(credit.toTinkCreditAccount());
                    LOGGER.infoExtraLong(
                            "CONTINUING CREDIT TX: " + client
                                    .fetchContinuingCreditTransactions(credit.getEncryptedAgreementNumber()),
                            OpBankConstants.Fetcher.CREDIT_LOGGING);
                }
            }
        } catch (Exception e) {
            LOGGER.warnExtraLong("Could not fetch continuing credit ",
                    OpBankConstants.Fetcher.CREDIT_LOGGING, e);
        }

        return creditAccounts;
    }

}


