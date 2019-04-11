package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.creditcard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.BanquePopulaireApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.BanquePopulaireConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.transactionalaccounts.rpc.BanquePopulaireTransactionsResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BanquePopulaireCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>,
                TransactionKeyPaginator<CreditCardAccount, String> {
    private static final AggregationLogger LOGGER =
            new AggregationLogger(BanquePopulaireCreditCardFetcher.class);

    private final BanquePopulaireApiClient apiClient;

    public BanquePopulaireCreditCardFetcher(BanquePopulaireApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        List<CreditCardAccount> cards = new ArrayList<>();

        try {
            String cardsResponse = apiClient.getAllCards();
            LOGGER.infoExtraLong(
                    SerializationUtils.serializeToString(cardsResponse),
                    BanquePopulaireConstants.LogTags.CREDIT_CARD);
        } catch (Exception e) {
            LOGGER.info("Failed to fetch cards");
        }

        return cards;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            CreditCardAccount account, String key) {
        return BanquePopulaireTransactionsResponse.empty();
    }
}
