package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30.fetcher.rpc.transaction;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30.fetcher.entities.upcomingtransaction.ScheduledPaymentEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

@JsonObject
public class UpcomingTransactionsV30Response extends BaseResponse<List<ScheduledPaymentEntity>> {
    public static List<UpcomingTransaction> toUpcomingTransactions(
            UpcomingTransactionsV30Response response) {

        return response.getData().stream()
                .map(ScheduledPaymentEntity::toTinkUpcomingTransaction)
                .collect(Collectors.toList());
    }
}
