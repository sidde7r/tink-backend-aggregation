package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.creditcards;

import com.google.common.collect.Lists;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.creditcards.rpc.CardsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities.OpBankCreditEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.rpc.FetchCreditsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class OpBankCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>,
                TransactionKeyPaginator<CreditCardAccount, String> {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            CreditCardAccount account, String key) {
        // TODO: Implement credit card transaction fetching. We need credentials for that.

        //        if (key == null) {
        //            apiClient.fetchCreditCardTransactions(account);
        //        } else {
        //            apiClient.fetchCreditCardTransactions(account, key);
        //        }

        //        try {
        //            String response = apiClient.fetchCreditCardTransactions(account);
        //            LOGGER.infoExtraLong(response,
        // OpBankConstants.Fetcher.CREDIT_CARD_TRX_LOGGING_NEW);
        //        } catch (Exception e){
        //            LOGGER.warn(OpBankConstants.Fetcher.CREDIT_CARD_TRX_FAILED + " new endpoint");
        //            tryFetchFromOldEndpoint(account);
        //        }

        return TransactionKeyPaginatorResponseImpl.createEmpty();
    }

    private Collection<CreditCardAccount> fetchContinuingCreditAccounts() {
        List<CreditCardAccount> creditAccounts = Lists.newArrayList();

        try {
            FetchCreditsResponse fetchCreditsResponse = apiClient.fetchCredits();

            for (OpBankCreditEntity credit : fetchCreditsResponse.getCredits()) {

                if (OpBankConstants.Fetcher.CONTINUING_CREDIT.equalsIgnoreCase(
                        credit.getCreditType())) {
                    creditAccounts.add(credit.toTinkCreditAccount());
                    logger.info(
                            "tag={} CONTINUING CREDIT TX: {}",
                            OpBankConstants.Fetcher.CREDIT_LOGGING,
                            apiClient.fetchContinuingCreditTransactions(
                                    credit.getEncryptedAgreementNumber()));
                }
            }
        } catch (Exception e) {
            logger.warn(
                    "tag={} Could not fetch continuing credit",
                    OpBankConstants.Fetcher.CREDIT_LOGGING,
                    e);
        }

        return creditAccounts;
    }
}
