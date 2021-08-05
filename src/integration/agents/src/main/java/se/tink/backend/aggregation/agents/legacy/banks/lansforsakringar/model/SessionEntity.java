package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class SessionEntity {
    protected String name;
    protected String userId;
    protected String lfCompanyBelonging;
    protected String ticket;
    protected int ticketLifetime;
    protected String pinPadAvailable;
    protected String enterpriseServicesPrimarySession;
}
