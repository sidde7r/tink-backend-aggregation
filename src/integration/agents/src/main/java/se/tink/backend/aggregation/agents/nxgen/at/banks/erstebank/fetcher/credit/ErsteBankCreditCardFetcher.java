package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.credit;

import com.google.common.base.Strings;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.ErsteBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.ErsteBankConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class ErsteBankCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionPagePaginator<CreditCardAccount> {

    private final ErsteBankApiClient client;
    private Logger logger = LoggerFactory.getLogger(ErsteBankCreditCardFetcher.class);

    public ErsteBankCreditCardFetcher(ErsteBankApiClient client) {
        this.client = client;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return client.fetchAccounts().toCreditCardAccounts();
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        String creditUrl = account.getFromTemporaryStorage(ErsteBankConstants.STORAGE.CREDITURL);

        if (Strings.isNullOrEmpty(creditUrl)) {
            return PaginatorResponseImpl.createEmpty(false);
        }

        try {
            return client.fetchTransactions(page, creditUrl);
        } catch (Exception e) {
            logger.error(
                    String.format(
                            "%s: %s",
                            ErsteBankConstants.LOGTAG.CREDIT_TRANSACTIONS_ERROR.toString(),
                            e.toString()));
            return PaginatorResponseImpl.createEmpty(false);
        }
    }
}
