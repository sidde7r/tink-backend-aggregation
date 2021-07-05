package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ChosenScaMethod {
    private String authenticationType;
    private String authenticationMethodId;
}
