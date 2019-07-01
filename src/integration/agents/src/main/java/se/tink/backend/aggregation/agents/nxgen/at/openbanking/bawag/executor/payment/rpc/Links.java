package se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.executor.payment.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Links {

    private ScaStatus scaStatus;
    private Self self;
    private UpdatePsuAuthentication updatePsuAuthentication;

    public ScaStatus getScaStatus() {
        return scaStatus;
    }

    public void setScaStatus(ScaStatus scaStatus) {
        this.scaStatus = scaStatus;
    }

    public Self getSelf() {
        return self;
    }

    public void setSelf(Self self) {
        this.self = self;
    }

    public UpdatePsuAuthentication getUpdatePsuAuthentication() {
        return updatePsuAuthentication;
    }

    public void setUpdatePsuAuthentication(UpdatePsuAuthentication updatePsuAuthentication) {
        this.updatePsuAuthentication = updatePsuAuthentication;
    }
}
