package se.tink.backend.aggregation.agents.banks.uk.barclays.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceMessage {
    private String svc_msg;
    public String getSvc_msg() {
        return svc_msg;
    }
}
