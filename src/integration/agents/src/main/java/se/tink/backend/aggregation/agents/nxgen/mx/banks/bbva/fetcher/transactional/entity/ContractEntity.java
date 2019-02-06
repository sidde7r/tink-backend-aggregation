package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.transactional.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ContractEntity {
    private String number;
    private ProductEntity product;
    private NumberTypeEntity numberType;
    private String alias;
    private String id;
}
