package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcConstants;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class KbcCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionFetcher<CreditCardAccount> {
    private static final AggregationLogger LOGGER =
            new AggregationLogger(KbcCreditCardFetcher.class);

    private final KbcApiClient apiClient;
    private final SessionStorage sessionStorage;

    public KbcCreditCardFetcher(KbcApiClient apiClient, final SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        final byte[] cipherKey =
                EncodingUtils.decodeBase64String(
                        sessionStorage.get(KbcConstants.Encryption.AES_SESSION_KEY_KEY));

        String cards = apiClient.fetchCards(cipherKey);
        LOGGER.infoExtraLong("cards: " + cards, KbcConstants.LogTags.CREDIT_CARDS);
        return Collections.emptyList();
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(CreditCardAccount account) {
        return Collections.emptyList();
    }
}
