package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.payment.rpc;

import se.tink.backend.aggregation.agents.Href;
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
}
