package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities;

import javax.xml.bind.annotation.XmlElement;

public class ServiceRequestEntity {
    private String serverSessionId;
    private Context context;
    private String actionCall;
    private String params;

    @XmlElement(name = "ServerSessionID")
    public void setServerSessionId(String serverSessionId) {
        this.serverSessionId = serverSessionId;
    }

    public String getServerSessionId() {
        return serverSessionId;
    }

    @XmlElement(name = "Context")
    public void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    @XmlElement(name = "ActionCall")
    public void setActionCall(String actionCall) {
        this.actionCall = actionCall;
    }

    public String getActionCall() {
        return actionCall;
    }

    @XmlElement(name = "Params")
    public void setParams(String params) {
        this.params = params;
    }

    public String getParams() {
        return params;
    }
}
