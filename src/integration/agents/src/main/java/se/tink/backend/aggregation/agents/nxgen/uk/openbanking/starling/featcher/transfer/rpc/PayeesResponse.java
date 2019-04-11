package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transfer.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transfer.entity.PayeeEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PayeesResponse {

    List<PayeeEntity> payees;

    public List<PayeeEntity> getPayees() {
        return payees;
    }
}
