package se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.userregistration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.Response;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInformationResponse extends Response {
    private String aid;

    public String getAid() {
        return aid;
    }
}
