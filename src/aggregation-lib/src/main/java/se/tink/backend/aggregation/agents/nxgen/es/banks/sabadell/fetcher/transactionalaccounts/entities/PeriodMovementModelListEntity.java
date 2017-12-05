package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PeriodMovementModelListEntity {
    private String periodType;
    private String periodDate;
    private GenericMovementWrapperListEntity genericMovementWrapperList;

    public String getPeriodType() {
        return periodType;
    }

    public String getPeriodDate() {
        return periodDate;
    }

    public GenericMovementWrapperListEntity getGenericMovementWrapperList() {
        return genericMovementWrapperList;
    }
}
