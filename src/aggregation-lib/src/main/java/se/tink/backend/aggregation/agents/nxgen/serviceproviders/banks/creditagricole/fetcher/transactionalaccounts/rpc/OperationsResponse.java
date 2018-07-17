package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.fetcher.transactionalaccounts.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.fetcher.transactionalaccounts.entities.OperationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.rpc.DefaultResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OperationsResponse extends DefaultResponse {
    private List<OperationEntity> operation;

    public List<OperationEntity> getOperation() {
        return operation;
    }
}
