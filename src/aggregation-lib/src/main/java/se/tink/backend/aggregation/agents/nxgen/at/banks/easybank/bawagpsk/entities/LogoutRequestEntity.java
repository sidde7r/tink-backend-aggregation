package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities;

import javax.xml.bind.annotation.XmlElement;

public class LogoutRequestEntity {
    private String serverSessionID;
    private Context context;

    @XmlElement(name = "ServerSessionID")
    public void setServerSessionID(String serverSessionID) {
        this.serverSessionID = serverSessionID;
    }

    public String getServerSessionID() {
        return serverSessionID;
    }

    @XmlElement(name = "Context")
    public void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }
}
