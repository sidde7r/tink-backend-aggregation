package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc;

public class ListSecurityDetailsRequest {
    private final String securityId;

    private ListSecurityDetailsRequest(String securityId) {
        this.securityId = securityId;
    }

    public static ListSecurityDetailsRequest createFromSecurityId(String securityId) {
        return new ListSecurityDetailsRequest(securityId);
    }

    public String getSecurityId() {
        return securityId;
    }
}
