package se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.entities;

public class RtaMessage {

    private final String rtaMessageID;

    public RtaMessage(String rtaMessageID) {
        this.rtaMessageID = rtaMessageID;
    }

    public String getRtaMessageID() {
        return rtaMessageID;
    }
}
