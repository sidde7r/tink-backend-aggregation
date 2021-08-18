package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {
    protected String name;
    protected String ssn;
    protected String lfCompanyBelonging;
    protected String ticket;
    protected int ticketLifetime;
    protected String pinPadAvailable;
    protected String enterpriseServicesPrimarySession;

    public String getPinPadAvailable() {
        return pinPadAvailable;
    }

    public void setPinPadAvailable(String pinPadAvailable) {
        this.pinPadAvailable = pinPadAvailable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public String getLfCompanyBelonging() {
        return lfCompanyBelonging;
    }

    public String getEnterpriseServicesPrimarySession() {
        return enterpriseServicesPrimarySession;
    }

    public void setLfCompanyBelonging(String lfCompanyBelonging) {
        this.lfCompanyBelonging = lfCompanyBelonging;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public int getTicketLifetime() {
        return ticketLifetime;
    }

    public void setTicketLifetime(int ticketLifetime) {
        this.ticketLifetime = ticketLifetime;
    }
}
