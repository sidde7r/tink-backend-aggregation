package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CompleteEnrollResponse {
    private String customerName;
    private String nemIDNo;
    private String installId;

    public String getCustomerName() {
        return customerName;
    }

    public String getNemIDNo() {
        return nemIDNo;
    }

    public String getInstallId() {
        return installId;
    }
}
