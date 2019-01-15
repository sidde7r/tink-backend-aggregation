package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.creditcard;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.creditcard.entities.CreditCardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.creditcard.rpc.CardsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.entities.CrossKeyCard;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.rpc.CardsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CrossKeyCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionDatePaginator<CreditCardAccount> {
    private static final Logger LOG = LoggerFactory.getLogger(
            CrossKeyCreditCardFetcher.class);

    private final CrossKeyApiClient client;
    private final CrossKeyPersistentStorage persistentStorage;

    public CrossKeyCreditCardFetcher(CrossKeyApiClient client, CrossKeyPersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        try {
            String deviceId = persistentStorage.getDeviceId();
            CardsResponse cardsResponse = client.fetchCards(new CardsRequest(deviceId));
            if (cardsResponse != null &&
                    (cardsResponse.getCards().size() > 0 || cardsResponse.hasData())
                    ) {
                LOG.info("User has some kind of card ");
                LOG.info(SerializationUtils.serializeToString(cardsResponse));
            }
            return cardsResponse.getCards().stream()
                    .filter(CrossKeyCard::isCreditCard)
                    .map(CrossKeyCard::toCreditCardAccount)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOG.info("Error fetching credit cards: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, Date fromDate, Date toDate) {
        Collection<? extends Transaction> transactions = this.client.fetchCreditCardTransactions(
                account.getBankIdentifier(), fromDate, toDate).stream()
                .map(CreditCardTransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());

        return PaginatorResponseImpl.create(transactions);
    }
}
