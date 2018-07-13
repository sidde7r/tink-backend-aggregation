package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GenericGroupedMovementEntity {
    private boolean moreElements;
    private Object periodMovementModelList;

    public boolean hasMoreElements() {
        return moreElements;
    }

    public Object getPeriodMovementModelList() {
        return periodMovementModelList;
    }
}
