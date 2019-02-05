package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DistributorAuthenticationRequest {
    private String targetActivities;
    private String distributionChannelId;
    private String minimumDacLevel;
    private String distributorId;

    public DistributorAuthenticationRequest(String targetActivities, String distributionChannelId,
            String minimumDacLevel, String distributorId) {
        this.targetActivities = targetActivities;
        this.distributionChannelId = distributionChannelId;
        this.minimumDacLevel = minimumDacLevel;
        this.distributorId = distributorId;
    }
}
