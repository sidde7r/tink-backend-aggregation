package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.legacy;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@RequiredArgsConstructor
public class CheckForcedUpgradeRequest {
    private final String distributorId;
}
