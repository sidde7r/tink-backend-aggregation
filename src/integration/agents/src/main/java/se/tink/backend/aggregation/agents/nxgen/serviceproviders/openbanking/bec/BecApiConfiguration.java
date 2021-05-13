package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BecApiConfiguration {
    private String url;
    private String userIp;
    private boolean isUserPresent;
}
