package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transferdestinations.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InterestsEntity {
    private String calculationBase;
    private String interestRateType;
    private String interestType;
    private String pdAccountability;
    private List<TiersEntity> tiers;
}
