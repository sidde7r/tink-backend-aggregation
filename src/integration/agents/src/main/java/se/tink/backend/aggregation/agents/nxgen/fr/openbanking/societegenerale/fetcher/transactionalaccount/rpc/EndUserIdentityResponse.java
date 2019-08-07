package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EndUserIdentityResponse {

    private String connectedPsu;

    public String getConnectedPsu() {
        return connectedPsu;
    }
}
