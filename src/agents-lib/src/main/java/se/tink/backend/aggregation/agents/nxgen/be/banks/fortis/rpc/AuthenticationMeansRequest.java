package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticationMeansRequest {
    private String targetActivities;
    private String distributionChannelId;
    private String minimumDacLevel;
    private String distributorId;

    public AuthenticationMeansRequest(String targetActivities, String distributionChannelId,
            String minimumDacLevel, String distributorId) {
        this.targetActivities = targetActivities;
        this.distributionChannelId = distributionChannelId;
        this.minimumDacLevel = minimumDacLevel;
        this.distributorId = distributorId;
    }

}
