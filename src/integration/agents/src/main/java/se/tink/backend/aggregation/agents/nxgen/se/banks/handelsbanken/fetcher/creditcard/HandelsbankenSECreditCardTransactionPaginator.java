package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.creditcard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.HandelsbankenCreditCardTransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class HandelsbankenSECreditCardTransactionPaginator extends
        HandelsbankenCreditCardTransactionPaginator<HandelsbankenSEApiClient> {

    public HandelsbankenSECreditCardTransactionPaginator(
            HandelsbankenSEApiClient client,
            HandelsbankenSessionStorage sessionStorage) {
        super(client, sessionStorage);
    }

    @Override
    protected List<Transaction> fetchAccountTransactions(
            HandelsbankenAccount handelsbankenAccount) {

        Collection<? extends Transaction> fetched = client.transactions(handelsbankenAccount).getTinkTransactions();

        return new ArrayList<>(fetched);
    }
}
