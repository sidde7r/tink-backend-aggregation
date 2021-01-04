package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transferdestinations.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TiersEntity {
    private String absoluteRate;
    private String dayCountConvention;
    private String effectiveDate;
    private int interestCapitalizationFrequence;
    private String interestRate;
    private String pegAmount;
    private String referenceRateType;
    private int upperBalance;
}
