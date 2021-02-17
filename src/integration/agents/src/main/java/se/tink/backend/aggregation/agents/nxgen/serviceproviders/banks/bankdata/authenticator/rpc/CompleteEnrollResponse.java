package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class CompleteEnrollResponse {
    private String cprNo;
    private String customerName;
    private String nemIDNo;
    private String installId;
}
