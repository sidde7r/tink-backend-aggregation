package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DistributorAuthenticationMeansEntity {
    private List<DistributionChannelIdsEntity> distributionChannelIds;
    private String dacLevel;
    private String authenticationMeanId;
}
