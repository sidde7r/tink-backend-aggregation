package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PeriodMovementEntity {
    private String periodType;
    private String periodDate;
    private MovementWrapperListEntity genericMovementWrapperList;

    public String getPeriodType() {
        return periodType;
    }

    public String getPeriodDate() {
        return periodDate;
    }

    public MovementWrapperListEntity getGenericMovementWrapperList() {
        return genericMovementWrapperList;
    }
}
