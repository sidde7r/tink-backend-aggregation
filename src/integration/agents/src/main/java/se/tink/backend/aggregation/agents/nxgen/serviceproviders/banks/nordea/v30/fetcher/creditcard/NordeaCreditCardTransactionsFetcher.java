package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.creditcard;

import static io.vavr.Predicates.not;

import java.util.Optional;
import org.assertj.core.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class NordeaCreditCardTransactionsFetcher
        implements TransactionPagePaginator<CreditCardAccount> {
    private static final Logger logger =
            LoggerFactory.getLogger(NordeaCreditCardTransactionsFetcher.class);
    private final NordeaBaseApiClient apiClient;

    public NordeaCreditCardTransactionsFetcher(NordeaBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        try {
            return Optional.ofNullable(account)
                    .map(CreditCardAccount::getApiIdentifier)
                    .filter(not(Strings::isNullOrEmpty))
                    .map(accId -> (PaginatorResponse) apiClient.fetchCardTransactions(page, accId))
                    .orElse(PaginatorResponseImpl.createEmpty(false));
        } catch (Exception e) {
            logger.error(
                    String.format(
                            "%s: %s",
                            NordeaBaseConstants.LogTags.CREDIT_TRANSACTIONS_ERROR.toString(),
                            e.toString()),
                    e);
            return PaginatorResponseImpl.createEmpty(false);
        }
    }
}
