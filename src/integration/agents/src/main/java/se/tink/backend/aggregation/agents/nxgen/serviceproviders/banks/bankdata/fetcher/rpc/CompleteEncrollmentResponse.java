package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc;

public class CompleteEncrollmentResponse {

    private String customName;
    private String crpNo;
    private String nemIDNo;
    private String installId;

    public String getCustomName() {
        return customName;
    }

    public String getCrpNo() {
        return crpNo;
    }

    public String getNemIDNo() {
        return nemIDNo;
    }

    public String getInstallId() {
        return installId;
    }
}
