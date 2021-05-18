package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.fetcher.transactionalaccount;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.QueryKeys.BOOKING_STATUS;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.QueryValues.BOOKED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.QueryValues.BOTH;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
@RequiredArgsConstructor
public class SdcTransactionalAccountTransactionFetcher
        implements TransactionDatePaginator<TransactionalAccount> {

    private final SdcApiClient apiClient;
    private final String providerMarket;
    private final PersistentStorage persistentStorage;
    private final HashMap<String, LocalDateTime> bookingStatusUpdateTimeMap = new HashMap<>();
    private final String TIMESTAMP = "timestamp";

    @Override
    public TransactionsResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {

        if (bookingStatusUpdateTimeMap.isEmpty()
                || LocalDateTime.now()
                        .isAfter(bookingStatusUpdateTimeMap.get(TIMESTAMP).plusHours(24))) {
            bookingStatusUpdateTimeMap.put(TIMESTAMP, LocalDateTime.now());
            persistentStorage.put(BOOKING_STATUS, BOTH, false);
        }

        try {
            return apiClient.getTransactionsFor(
                    account.getApiIdentifier(),
                    fromDate,
                    toDate,
                    providerMarket,
                    persistentStorage.get(BOOKING_STATUS));
        } catch (Exception e) {
            log.error(
                    "Unable to fetch both pending and booked transactions. Re-trying only booked.",
                    e);
            bookingStatusUpdateTimeMap.put(TIMESTAMP, LocalDateTime.now());
            persistentStorage.put(BOOKING_STATUS, BOOKED, false);
            return apiClient.getTransactionsFor(
                    account.getApiIdentifier(),
                    fromDate,
                    toDate,
                    providerMarket,
                    persistentStorage.get(BOOKING_STATUS));
        }
    }
}
