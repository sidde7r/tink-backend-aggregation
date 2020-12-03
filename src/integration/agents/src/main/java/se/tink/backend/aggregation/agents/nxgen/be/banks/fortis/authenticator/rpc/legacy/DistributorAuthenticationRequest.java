package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.legacy;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@RequiredArgsConstructor
public class DistributorAuthenticationRequest {
    private final String targetActivities;
    private final String distributionChannelId;
    private final String minimumDacLevel;
    private final String distributorId;
}
