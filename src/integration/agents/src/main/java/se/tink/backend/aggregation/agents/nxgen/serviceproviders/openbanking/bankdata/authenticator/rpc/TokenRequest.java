package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public interface TokenRequest {

    String toData();
}
