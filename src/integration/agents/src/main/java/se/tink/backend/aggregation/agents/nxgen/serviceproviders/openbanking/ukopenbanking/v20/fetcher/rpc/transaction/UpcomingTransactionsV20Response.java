package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.fetcher.rpc.transaction;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.fetcher.entities.upcomingtransaction.ScheduledPaymentEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

@JsonObject
public class UpcomingTransactionsV20Response extends BaseResponse<List<ScheduledPaymentEntity>> {

    public static List<UpcomingTransaction> toUpcomingTransactions(
            UpcomingTransactionsV20Response response) {

        // TODO: Ukob test data has an error in it which makes some transactions impossible to
        // parse.
        // TODO: This combined with the try/catch in UkOpenBankingApiClient discards those
        // transactions to prevents crash.
        if (response == null) {
            return Collections.emptyList();
        }

        return response.getData()
                .stream()
                .map(ScheduledPaymentEntity::toTinkUpcomingTransaction)
                .collect(Collectors.toList());
    }
}
