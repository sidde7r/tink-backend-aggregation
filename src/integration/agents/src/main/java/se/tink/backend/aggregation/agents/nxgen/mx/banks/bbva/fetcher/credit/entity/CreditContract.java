package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.credit.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditContract {
    private String number;
    private Product product;
    private NumberType numberType;
    private String id;
}
