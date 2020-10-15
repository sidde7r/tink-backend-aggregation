package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entities.TransactionsResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProxyGetTransactionsResponse
        extends ProxyResponseMessage<TransactionsResponseEntity> {}
