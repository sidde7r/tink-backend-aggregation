package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BankdataApiConfiguration {
    private String baseUrl;
    private String baseAuthUrl;
    private String userIp;
    private boolean isUserPresent;
}
