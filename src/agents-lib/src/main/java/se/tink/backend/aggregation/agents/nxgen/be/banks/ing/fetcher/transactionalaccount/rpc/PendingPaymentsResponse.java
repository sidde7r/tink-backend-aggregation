package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.rpc;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.entities.PendingPaymentsResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PendingPaymentsResponse {
    private PendingPaymentsResponseEntity mobileResponse;

    public PendingPaymentsResponseEntity getMobileResponse() {
        return Preconditions.checkNotNull(mobileResponse);
    }
}
