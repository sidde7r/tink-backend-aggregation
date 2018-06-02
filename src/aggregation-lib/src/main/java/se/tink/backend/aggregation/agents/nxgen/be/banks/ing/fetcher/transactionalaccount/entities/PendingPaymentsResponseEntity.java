package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.entities;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.json.BaseMobileResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PendingPaymentsResponseEntity extends BaseMobileResponseEntity {
    private List<PendingPaymentEntity> pendingPayments;

    public List<PendingPaymentEntity> getPendingPayments() {
        return pendingPayments;
    }
}
