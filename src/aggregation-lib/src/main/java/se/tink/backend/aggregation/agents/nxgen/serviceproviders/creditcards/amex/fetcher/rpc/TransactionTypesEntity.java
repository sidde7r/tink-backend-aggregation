package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionTypesEntity {
    private String type;
    private String label;
}
