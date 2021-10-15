package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.transactionalaccount.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class DateEntity {
    private long epoch;
    private String utc;
}
