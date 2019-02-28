
package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.creditcards;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.creditcards.rpc.CardsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities.OpBankCreditEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.rpc.FetchCreditsResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class OpBankCreditCardFetcher implements AccountFetcher<CreditCardAccount>,
        TransactionKeyPaginator<CreditCardAccount, String> {

    private static final AggregationLogger LOGGER = new AggregationLogger(OpBankCreditCardFetcher.class);

    private final OpBankApiClient apiClient;

    public OpBankCreditCardFetcher(OpBankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        List<CreditCardAccount> creditCardAccounts = Lists.newArrayList();

        CardsResponse cardsResponse = apiClient.fetchCards();

        creditCardAccounts.addAll(cardsResponse.getTinkCreditCards());
        // continuing credit is not really a credit card, but it is handled here
        creditCardAccounts.addAll(fetchContinuingCreditAccounts());

        return creditCardAccounts;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(CreditCardAccount account, String key) {
        // This is how I believe the credit card transaction fetching will work (same as for transactional accounts,
        // but commented it out for testing endpoints

//        if (key == null) {
//            apiClient.fetchCreditCardTransactions(account);
//        } else {
//            apiClient.fetchCreditCardTransactions(account, key);
//        }

        try {
            String response = apiClient.fetchCreditCardTransactions(account);
            LOGGER.infoExtraLong(response, OpBankConstants.Fetcher.CREDIT_CARD_TRX_LOGGING_NEW);
        } catch (Exception e){
            LOGGER.warn(OpBankConstants.Fetcher.CREDIT_CARD_TRX_FAILED + " new endpoint");
            tryFetchFromOldEndpoint(account);
        }

        return TransactionKeyPaginatorResponseImpl.createEmpty();
    }

    private void tryFetchFromOldEndpoint(CreditCardAccount account) {
        try {
            String response = apiClient.fetchCreditCardTransactionsOldEndpoint(account);
            LOGGER.infoExtraLong(response, OpBankConstants.Fetcher.CREDIT_CARD_TRX_LOGGING_OLD);
        } catch (Exception e) {
            LOGGER.warn(OpBankConstants.Fetcher.CREDIT_CARD_TRX_FAILED + " old endpoint");
        }
    }

    private Collection<CreditCardAccount> fetchContinuingCreditAccounts() {
        List<CreditCardAccount> creditAccounts = Lists.newArrayList();

        try {
            FetchCreditsResponse fetchCreditsResponse = apiClient.fetchCredits();

            for (OpBankCreditEntity credit : fetchCreditsResponse.getCredits()) {

                if (OpBankConstants.Fetcher.CONTINUING_CREDIT
                        .equalsIgnoreCase(credit.getCreditType())) {
                    creditAccounts.add(credit.toTinkCreditAccount());
                    LOGGER.infoExtraLong(
                            "CONTINUING CREDIT TX: " + apiClient
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


