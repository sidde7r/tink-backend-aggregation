package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LimitsEntity {
    private String intraDayLimit;
    private String intraDayLimitDate;
    private String endOfDayLimit;
    private String endOfDayLimitDate;
}
