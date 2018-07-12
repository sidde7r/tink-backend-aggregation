package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities;

import javax.xml.bind.annotation.XmlElement;

public class ServiceResponseEntity {
    private String actionCall;
    private int countNewDisposerMails;

    @XmlElement(name = "ActionCall")
    public void setActionCall(String actionCall) {
        this.actionCall = actionCall;
    }

    public String getActionCall() {
        return actionCall;
    }

    @XmlElement(name = "countNewDisposerMails")
    public void setCountNewDisposerMails(int countNewDisposerMails) {
        this.countNewDisposerMails = countNewDisposerMails;
    }

    public int getCountNewDisposerMails() {
        return countNewDisposerMails;
    }
}
