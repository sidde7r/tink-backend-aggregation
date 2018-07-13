package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities;

public class SdcAgreement {
    private SdcAgreementEntityKey entityKey;
    private String userName;
    private String agreementName;
    private String primaryLabel;
    private String secondaryLabel;
    private String lastLogon;

    public SdcAgreementEntityKey getEntityKey() {
        return entityKey;
    }

    public String getUserName() {
        return userName;
    }

    public String getAgreementName() {
        return agreementName;
    }

    public String getPrimaryLabel() {
        return primaryLabel;
    }

    public String getSecondaryLabel() {
        return secondaryLabel;
    }

    public String getLastLogon() {
        return lastLogon;
    }
}
