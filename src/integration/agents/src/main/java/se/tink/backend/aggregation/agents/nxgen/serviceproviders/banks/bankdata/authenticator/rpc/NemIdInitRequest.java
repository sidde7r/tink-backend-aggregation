package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@RequiredArgsConstructor
public class NemIdInitRequest {

    private final String data;
    private final String rsalabel;
}
