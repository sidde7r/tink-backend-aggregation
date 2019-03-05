package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.transactional.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionTypeEntity {
    private String name;
    private String id;
    private InternalCodeEntity internalCode;
}
