package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EndUserIdentityResponse {

    private String connectedPsu;

    public String getConnectedPsu() {
        return connectedPsu;
    }
}
