package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.transaction.model;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DetailsEntity {

    private String dataType;

    private String dataValue;

    private int ordinal;
}
