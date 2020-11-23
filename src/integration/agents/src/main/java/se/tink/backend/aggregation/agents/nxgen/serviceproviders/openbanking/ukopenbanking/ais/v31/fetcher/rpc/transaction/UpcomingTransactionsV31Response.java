package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.rpc.transaction;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.rpc.BaseV31Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.entities.upcomingtransaction.ScheduledPaymentEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

@JsonObject
public class UpcomingTransactionsV31Response extends BaseV31Response<List<ScheduledPaymentEntity>> {
    public static List<UpcomingTransaction> toUpcomingTransactions(
            UpcomingTransactionsV31Response response) {

        return response.getData().orElse(Collections.emptyList()).stream()
                .map(ScheduledPaymentEntity::toTinkUpcomingTransaction)
                .collect(Collectors.toList());
    }
}
