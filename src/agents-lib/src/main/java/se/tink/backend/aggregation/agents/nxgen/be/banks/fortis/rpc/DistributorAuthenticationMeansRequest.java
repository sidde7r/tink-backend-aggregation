package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DistributorAuthenticationMeansRequest {
    private String targetActivities;
    private String distributionChannelId;
    private String minimumDacLevel;
    private String distributorId;
}
