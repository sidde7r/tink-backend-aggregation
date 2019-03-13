package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transfer.rpc;

import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transfer.entity.PayeeEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class PayeesResponse {

    List<PayeeEntity> payees;

    public List<PayeeEntity> getPayees() {
        return payees;
    }
}
