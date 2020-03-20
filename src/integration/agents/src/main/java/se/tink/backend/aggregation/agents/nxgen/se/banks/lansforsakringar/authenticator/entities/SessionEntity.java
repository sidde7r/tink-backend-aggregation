package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SessionEntity {
    private String ssn;
    private String name;
    private String ticket;
    private String lfCompanyBelonging;
    private int ticketLifetime;
    private boolean pinPadAvailable;
    private String enterpriseServicesPrimarySession;

    public String getSsn() {
        return ssn;
    }

    public String getName() {
        return name;
    }

    public String getTicket() {
        return ticket;
    }

    public String getLfCompanyBelonging() {
        return lfCompanyBelonging;
    }

    public int getTicketLifetime() {
        return ticketLifetime;
    }

    public boolean isPinPadAvailable() {
        return pinPadAvailable;
    }

    public String getEnterpriseServicesPrimarySession() {
        return enterpriseServicesPrimarySession;
    }
}
