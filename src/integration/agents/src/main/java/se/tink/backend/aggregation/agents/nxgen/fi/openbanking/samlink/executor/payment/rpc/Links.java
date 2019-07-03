package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.executor.payment.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Links {

    private Href scaStatus;
    private Href self;
    private Href updatePsuAuthentication;

    public Href getScaStatus() {
        return scaStatus;
    }

    public void setScaStatus(Href scaStatus) {
        this.scaStatus = scaStatus;
    }

    public Href getSelf() {
        return self;
    }

    public void setSelf(Href self) {
        this.self = self;
    }

    public Href getUpdatePsuAuthentication() {
        return updatePsuAuthentication;
    }

    public void setUpdatePsuAuthentication(Href updatePsuAuthentication) {
        this.updatePsuAuthentication = updatePsuAuthentication;
    }
}
