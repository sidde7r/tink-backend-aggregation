package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.fetcher.transactionalaccounts.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class OperationEntity {

    public Transaction toTinkTransaction() {
        return null;
    }
}
